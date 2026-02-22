package com.shofyou.app;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private SwipeRefreshLayout swipe;
    private FileUploadHelper fileUploadHelper;

    private final String HOME_URL = "https://shofyou.com";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setStatusBarColor(Color.TRANSPARENT);

        webView = findViewById(R.id.webview);
        swipe = findViewById(R.id.swipe);
        ImageView splashLogo = findViewById(R.id.splashLogo);

        // تسريع الرندر
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        fileUploadHelper = new FileUploadHelper(this);

        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setAllowFileAccess(true);
        ws.setCacheMode(WebSettings.LOAD_DEFAULT);

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                splashLogo.setVisibility(View.GONE);
                swipe.setRefreshing(false);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view,
                                                    WebResourceRequest request) {

                String url = request.getUrl().toString();

                if (url.contains("shofyou.com")) {
                    view.loadUrl(url);
                    return true;
                }

                startActivity(
                        new Intent(MainActivity.this,
                                PopupActivity.class)
                                .putExtra("url", url)
                );

                return true;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView webView,
                                             android.webkit.ValueCallback<android.net.Uri[]> callback,
                                             FileChooserParams params) {
                return fileUploadHelper.handleFileChooser(callback, params);
            }
        });

        swipe.setOnRefreshListener(() -> webView.reload());

        // تحميل الموقع فوراً بدون تأخير
        webView.post(() -> webView.loadUrl(HOME_URL));

        handleBack();
    }

    private void handleBack() {

        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {

                    @Override
                    public void handleOnBackPressed() {

                        if (webView.canGoBack())
                            webView.goBack();
                        else
                            new AlertDialog.Builder(MainActivity.this)
                                    .setMessage("Exit app?")
                                    .setPositiveButton("Yes",
                                            (d, i) -> finish())
                                    .setNegativeButton("No", null)
                                    .show();
                    }
                });
    }
}
