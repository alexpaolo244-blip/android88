package com.shofyou.app;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
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
    private ImageView splashLogo;

    private final String HOME_URL = "https://shofyou.com";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setStatusBarColor(Color.TRANSPARENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            if (nightModeFlags != Configuration.UI_MODE_NIGHT_YES) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }

        webView = findViewById(R.id.webview);
        swipe = findViewById(R.id.swipe);
        splashLogo = findViewById(R.id.splashLogo);

        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setAllowFileAccess(true);
        ws.setAllowContentAccess(true);
        ws.setMediaPlaybackRequiresUserGesture(false);
        ws.setCacheMode(WebSettings.LOAD_DEFAULT);

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (splashLogo.getVisibility() == View.VISIBLE) {
                    splashLogo.setVisibility(View.GONE);
                }
                swipe.setRefreshing(false);

                if (url != null && url.contains("/reels/")) {
                    swipe.setEnabled(false);
                } else {
                    swipe.setEnabled(true);
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (url.contains("shofyou.com")) {
                    view.loadUrl(url);
                    return true;
                }
                startActivity(new Intent(MainActivity.this, PopupActivity.class).putExtra("url", url));
                return true;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress > 80) {
                    splashLogo.setVisibility(View.GONE);
                }
            }

            // ✅ التعديل هنا فقط
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> callback, FileChooserParams params) {

                if (fileCallback != null) {
                    fileCallback.onReceiveValue(null);
                }
                fileCallback = callback;

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);

                String[] acceptTypes = params.getAcceptTypes();
                String mimeType = "*/*";

                if (acceptTypes != null && acceptTypes.length > 0) {
                    String type = acceptTypes[0];

                    if (type.contains("image")) {
                        mimeType = "image/*";
                    } else if (type.contains("video")) {
                        mimeType = "video/*";
                    }
                }

                intent.setType(mimeType);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, params.getMode() == FileChooserParams.MODE_OPEN_MULTIPLE);

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if (fileCallback == null) return;

            Uri[] results = null;
            if (resultCode == RESULT_OK && data != null) {
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    results = new Uri[count];
                    for (int i = 0; i < count; i++) {
                        results[i] = data.getClipData().getItemAt(i).getUri();
                    }
                } else if (data.getData() != null) {
                    results = new Uri[]{data.getData()};
                }
            }
            fileCallback.onReceiveValue(results);
            fileCallback = null;
        }
    }

    private void handleBack() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (webView.canGoBack())
                    webView.goBack();
                else
                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage("Exit app?")
                            .setPositiveButton("Yes", (d, i) -> finish())
                            .setNegativeButton("No", null)
                            .show();
            }
        });
    }
}
