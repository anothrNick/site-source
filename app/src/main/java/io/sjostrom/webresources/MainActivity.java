package io.sjostrom.webresources;

import android.graphics.Picture;
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
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

import org.json.JSONArray;

import java.util.Date;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity {
    private String TAG = MainActivity.class.getSimpleName();

    FloatingSearchView mSearchView;
    Toolbar mToolbar;
    ProgressBar mProgressBar;
    WebView webView;

    RecyclerView mResources;
    private ResourceListAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSearchView = (FloatingSearchView) findViewById(R.id.floating_search_view);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        mResources = (RecyclerView) findViewById(R.id.resource_results);
        mResources.setHasFixedSize(true);

        mAdapter = new ResourceListAdapter(this);
        mResources.setAdapter(mAdapter);

        mLayoutManager = new LinearLayoutManager(this);
        mResources.setLayoutManager(mLayoutManager);

        webView = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebChromeClient(new MyWebChromeClient());
        webView.setWebViewClient(new MyWebViewClient());

        // set search ime
        ((TextView) mSearchView.findViewById(R.id.search_bar_text)).setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        mSearchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(final SearchSuggestion searchSuggestion) {

            }

            @Override
            public void onSearchAction(String query) {
                mAdapter.clearList();
                mProgressBar.setVisibility(View.VISIBLE);
                mResources.setVisibility(View.GONE);
                webView.loadUrl(query);
                Log.d(TAG, "onSearchAction(): " + query);
            }
        });

        setupToolbar();
        mProgressBar.setVisibility(GONE);
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
            webView.loadUrl("javascript:alert(JSON.stringify(performance.getEntriesByType('resource')))");
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Log.d(TAG, errorCode + " " + description);
            mProgressBar.setVisibility(View.GONE);
            mResources.setVisibility(View.VISIBLE);
        }
    }

    private class MyWebChromeClient extends WebChromeClient {
        private int retry = 0;

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            Log.d(TAG, message);
            try {
                JSONArray resources = new JSONArray(message);
                if(mAdapter.getItemCount() < resources.length()) {
                    mAdapter.setList(resources);
                    mAdapter.notifyDataSetChanged();

                    new android.os.Handler().postDelayed(
                            new Runnable() {
                                public void run() {
                                    Log.d(TAG, "checking for more resources");
                                    webView.loadUrl("javascript:alert(JSON.stringify(performance.getEntriesByType('resource')))");
                                }
                            },
                            5000);
                }
                else {
                    Log.d(TAG, "resource count is the same. stopping tests.");
                }
                Log.d(TAG, "resource count: " + resources.length());
            } catch(Exception e) {
                Log.w(TAG, e);
            }
            mProgressBar.setVisibility(View.GONE);
            mResources.setVisibility(View.VISIBLE);
            result.confirm();
            return true;
        }
    }
}
