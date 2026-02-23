package com.shofyou.app;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceError;
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int nightModeFlags =
                    getResources().getConfiguration().uiMode
                            & Configuration.UI_MODE_NIGHT_MASK;

            if (nightModeFlags != Configuration.UI_MODE_NIGHT_YES) {
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }

        webView = findViewById(R.id.webview);
        swipe = findViewById(R.id.swipe);
        ImageView splashLogo = findViewById(R.id.splashLogo);

        fileUploadHelper = new FileUploadHelper(this);

        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        webView.resumeTimers();

        WebSettings ws = webView.getSettings();

        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setDatabaseEnabled(true);
        ws.setAllowFileAccess(true);
        ws.setMediaPlaybackRequiresUserGesture(false);
        ws.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        ws.setLoadsImagesAutomatically(true);
        ws.setSupportMultipleWindows(false);
        ws.setBuiltInZoomControls(false);
        ws.setDisplayZoomControls(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ws.setOffscreenPreRaster(true);
        }

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                splashLogo.setVisibility(View.GONE);
                swipe.setRefreshing(false);

                if (url != null && url.contains("/reels/")) {
                    swipe.setEnabled(false);
                } else {
                    swipe.setEnabled(true);
                }
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

            @Override
            public void onReceivedError(WebView view,
                                        WebResourceRequest request,
                                        WebResourceError error) {

                if (request.isForMainFrame()) {
                    showNoInternetPage();
                }
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

        swipe.setOnRefreshListener(() -> {

            String current = webView.getUrl();

            if (current != null && current.contains("/reels/")) {
                swipe.setRefreshing(false);
            } else {
                webView.reload();
            }
        });

        if (isConnected()) {
            webView.loadUrl(HOME_URL);
        } else {
            showNoInternetPage();
        }

        handleBack();
    }

    private void showNoInternetPage() {

        splashLogoHide();

        String html = "<html><head><meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<style>" +
                "body{display:flex;justify-content:center;align-items:center;height:100vh;margin:0;" +
                "font-family:sans-serif;background:#f0f2f5;color:#1c1e21;text-align:center;padding:30px;}" +
                ".box{max-width:400px;}" +
                "h2{font-size:22px;margin-bottom:10px;}" +
                "p{color:#606770;font-size:15px;}" +
                "button{margin-top:20px;padding:10px 20px;border:none;border-radius:6px;" +
                "background:#1877f2;color:white;font-size:14px;}" +
                "</style></head><body>" +
                "<div class='box'>" +
                "<h2>No Internet Connection</h2>" +
                "<p>Please check your connection and try again.</p>" +
                "<button onclick='window.location.reload()'>Retry</button>" +
                "</div></body></html>";

        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
    }

    private boolean isConnected() {

        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm == null) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            Network network = cm.getActiveNetwork();
            if (network == null) return false;

            NetworkCapabilities capabilities =
                    cm.getNetworkCapabilities(network);

            return capabilities != null &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        } else {

            android.net.NetworkInfo info = cm.getActiveNetworkInfo();
            return info != null && info.isConnected();
        }
    }

    private void splashLogoHide() {
        ImageView splashLogo = findViewById(R.id.splashLogo);
        splashLogo.setVisibility(View.GONE);
        swipe.setRefreshing(false);
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
