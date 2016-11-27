package io.sjostrom.sitesource;

import android.graphics.Bitmap;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.SearchSuggestionsAdapter;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

import org.json.JSONArray;

import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.sjostrom.sitesource.model.History;
import io.sjostrom.sitesource.utils.RecyclerItemClickListener;


public class MainActivity extends AppCompatActivity {
    private String TAG = MainActivity.class.getSimpleName();

    FloatingSearchView mSearchView;
    Toolbar mToolbar;
    WebView webView;
    TextView mResourceWeirdness;

    RecyclerView mResources;
    private ResourceListAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private LoadResourceDialog mLoadingDialog;
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        realm = Realm.getDefaultInstance();

        // inflate (FloatingSearchView doesn't seem to support Butterknife)
        mSearchView = (FloatingSearchView) findViewById(R.id.floating_search_view);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mResources = (RecyclerView) findViewById(R.id.resource_results);
        mResourceWeirdness = (TextView) findViewById(R.id.resource_weirdness);
        webView = (WebView) findViewById(R.id.webview);
        // set keyboard submit button to be search icon
        ((TextView) mSearchView.findViewById(R.id.search_bar_text)).setImeOptions(EditorInfo.IME_ACTION_SEARCH);

        // setup RecyclerView
        setupResourceList();

        // setup WebView
        setupWebview();

        // setup SearchView
        setupSearchView();

        // setup Toolbar
        setupToolbar();

        // create loading dialog
        mLoadingDialog = LoadResourceDialog.newInstance();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void startingNewTest() {
        // clear adapter
        mAdapter.clearList();
        // hide recyclerview
        mResources.setVisibility(View.GONE);
        // hide resource weirdness (error)
        mResourceWeirdness.setVisibility((View.GONE));
        // show loading dialog
        showLoadingDialog();
    }

    private void showLoadingDialog() {
        FragmentManager fm = getSupportFragmentManager();
        mLoadingDialog.show(fm, "load_resource_dialog_fragment");
    }

    private void hideLoadingDialog() {
        mLoadingDialog.dismiss();
    }

    private void setLoadingDialogText(String text) {
        mLoadingDialog.setLoadingText(text);
    }

    private void setupToolbar() {
        setSupportActionBar(mToolbar);
    }

    private void setupSearchView() {
        mSearchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener(){
            @Override
            public void onSearchTextChanged(String oldQuery, final String newQuery) {
                if (!oldQuery.equals("") && newQuery.equals("")) {
                    mSearchView.clearSuggestions();
                } else {
                    mSearchView.showProgress();

                    RealmResults<History> results = realm.where(History.class)
                                                         .contains("url", newQuery)
                                                         .findAll();
                    List<History> lstResults = realm.copyFromRealm(results);
                    mSearchView.swapSuggestions(lstResults);

                    mSearchView.hideProgress();
                }
            }
        });

        mSearchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(final SearchSuggestion searchSuggestion) {
                startTest(searchSuggestion.getBody());
                Log.d(TAG, "onSuggestionClicked(): " + searchSuggestion.getBody());
            }

            @Override
            public void onSearchAction(String query) {
                startTest(query);
                Log.d(TAG, "onSearchAction(): " + query);
            }

            private void startTest(String query) {
                startingNewTest();
                if(query.indexOf("http") < 0) {
                    query = "http://" + query;
                }
                webView.loadUrl(query + "?" + new Date().getTime());
            }
        });

        mSearchView.setOnBindSuggestionCallback(new SearchSuggestionsAdapter.OnBindSuggestionCallback() {
            @Override
            public void onBindSuggestion(View suggestionView, ImageView leftIcon, TextView textView, SearchSuggestion item, int itemPosition) {
                leftIcon.setBackgroundResource(R.drawable.ic_access_time);
            }

        });
    }

    private void setupWebview() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebChromeClient(new MyWebChromeClient());
        webView.setWebViewClient(new MyWebViewClient());
    }

    private void setupResourceList() {
        mResources.setHasFixedSize(true);
        mAdapter = new ResourceListAdapter(this);
        mResources.setAdapter(mAdapter);
        mLayoutManager = new LinearLayoutManager(this);
        mResources.setLayoutManager(mLayoutManager);
        mResources.addOnItemTouchListener(new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        String resource = mAdapter.getItem(position);
                        if(!resource.isEmpty()) {
                            FragmentManager fm = getSupportFragmentManager();
                            ResourceDetailDialog detailDlg = ResourceDetailDialog.newInstance(resource);
                            detailDlg.show(fm, "fragment_resource_detail_dialog");
                        }
                    }
                })
        );
    }

    private class MyWebViewClient extends WebViewClient {

        @Override
        public void onPageFinished(WebView view, String url) {
            webView.loadUrl("javascript:alert(JSON.stringify(performance.getEntriesByType('resource')))");

            String trueUrl = url.split("\\?")[0];
            RealmResults<History> results = realm.where(History.class)
                    .equalTo("url", trueUrl)
                    .findAll();

            if(results.size() == 0) {
                realm.beginTransaction();
                History user = realm.createObject(History.class); // Create a new object
                user.url = trueUrl;
                user.lastsearched = new Date();
                realm.commitTransaction();
            }
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            String trueUrl = url.split("\\?")[0];
            mSearchView.setSearchText(trueUrl);
            setLoadingDialogText("Requesting\n'"+trueUrl+"'...");
        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            Log.w(TAG, "Error: " + errorResponse.getReasonPhrase());
        }
    }

    private class MyWebChromeClient extends WebChromeClient {
        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            Log.d(TAG, message);
            try {
                JSONArray resources = new JSONArray(message);
                setLoadingDialogText("Success!\nGathering resources (" + resources.length() + ")...");
                if(mAdapter.getItemCount() < resources.length()) {
                    mAdapter.setList(resources);
                    mAdapter.notifyDataSetChanged();

                    new android.os.Handler().postDelayed(
                            new Runnable() {
                                public void run() {
                                    Log.d(TAG, "checking for more resources");
                                    webView.loadUrl("javascript:alert(JSON.stringify(performance.getEntriesByType('resource').sort(function(a,b){return a.initiatorType.localeCompare(b.initiatorType);})))");
                                }
                            },
                            2000);
                }
                else {
                    mAdapter.setList(resources);
                    mAdapter.notifyDataSetChanged();

                    hideLoadingDialog();

                    if(mAdapter.getItemCount() > 0)
                        mResources.setVisibility(View.VISIBLE);
                    else
                        mResourceWeirdness.setVisibility((View.VISIBLE));

                    Log.d(TAG, "resource count is the same. stopping tests.");
                }
                Log.d(TAG, "resource count: " + resources.length());
            } catch(Exception e) {
                Log.w(TAG, e);
            }

            result.confirm();
            return true;
        }
    }
}
