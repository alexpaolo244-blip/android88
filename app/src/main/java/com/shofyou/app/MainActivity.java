package com.shofyou.app;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
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
    private ValueCallback<Uri[]> fileCallback;

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

        WebSettings ws = webView.getSettings();

        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setAllowFileAccess(true);
        ws.setMediaPlaybackRequiresUserGesture(false);

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
                        new android.content.Intent(MainActivity.this,
                                PopupActivity.class)
                                .putExtra("url", url)
                );

                return true;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onShowFileChooser(WebView webView,
                                             ValueCallback<Uri[]> callback,
                                             FileChooserParams params) {

                fileCallback = callback;

                android.content.Intent intent =
                        new android.content.Intent(
                                android.content.Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                intent.setType("*/*");
                intent.putExtra(android.content.Intent.EXTRA_MIME_TYPES,
                        new String[]{"image/*", "video/*"});
                intent.putExtra(android.content.Intent.EXTRA_ALLOW_MULTIPLE, true);

                startActivityForResult(intent, 100);

                return true;
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

        webView.loadUrl(HOME_URL);

        handleBack();
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    android.content.Intent data) {

        if (fileCallback == null) return;

        Uri[] result = null;

        if (resultCode == RESULT_OK && data != null) {

            result = new Uri[]{data.getData()};
        }

        fileCallback.onReceiveValue(result);
        fileCallback = null;
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
