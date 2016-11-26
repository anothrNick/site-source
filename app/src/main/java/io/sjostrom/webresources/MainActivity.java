package io.sjostrom.webresources;

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
import android.widget.TextView;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

import org.json.JSONArray;

import java.util.Date;


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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSearchView = (FloatingSearchView) findViewById(R.id.floating_search_view);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mResources = (RecyclerView) findViewById(R.id.resource_results);
        mResourceWeirdness = (TextView) findViewById(R.id.resource_weirdness);
        webView = (WebView) findViewById(R.id.webview);

        // Setup RecyclerView
        mResources.setHasFixedSize(true);
        mAdapter = new ResourceListAdapter(this);
        mResources.setAdapter(mAdapter);
        mLayoutManager = new LinearLayoutManager(this);
        mResources.setLayoutManager(mLayoutManager);

        // Setup WebView
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebChromeClient(new MyWebChromeClient());
        webView.setWebViewClient(new MyWebViewClient());

        // Setup SearchView
        ((TextView) mSearchView.findViewById(R.id.search_bar_text)).setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        mSearchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(final SearchSuggestion searchSuggestion) {

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

        setupToolbar();

        // Create Loading Dialog
        mLoadingDialog = LoadResourceDialog.newInstance();
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

    public void setupToolbar() {
        setSupportActionBar(mToolbar);
    }

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private class MyWebViewClient extends WebViewClient {

        @Override
        public void onPageFinished(WebView view, String url) {
            setLoadingDialogText("Success!\nGathering resources...");
            webView.loadUrl("javascript:alert(JSON.stringify(performance.getEntriesByType('resource')))");
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            mSearchView.setSearchText(url.split("\\?")[0]);
            setLoadingDialogText("Requesting\n'"+url.split("\\?")[0]+"'...");
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
