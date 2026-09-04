package com.seven7codex.presidentpreview;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;

public class MainActivity extends Activity {
    private static final String PREFS = "president_preview";
    private static final String KEY_URL = "server_url";
    private static final String DEFAULT_URL = "http://192.168.1.10:4173/?mobile=1&apk=1";
    private WebView webView;
    private boolean addressDialogShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        enterImmersive();

        webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setLoadWithOverviewMode(false);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setSupportZoom(false);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) showAddressDialog();
            }
        });

        webView.setOnLongClickListener(v -> {
            showAddressDialog();
            return true;
        });

        loadSavedAddress();
    }

    private void enterImmersive() {
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
            View.SYSTEM_UI_FLAG_FULLSCREEN |
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }

    private String normalizedUrl(String raw) {
        String value = raw == null ? "" : raw.trim();
        if (value.isEmpty()) return DEFAULT_URL;
        if (!value.startsWith("http://") && !value.startsWith("https://")) value = "http://" + value;
        if (!value.contains("?")) value += "/?mobile=1&apk=1";
        return value;
    }

    private void loadSavedAddress() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        webView.loadUrl(normalizedUrl(prefs.getString(KEY_URL, DEFAULT_URL)));
    }

    private void showAddressDialog() {
        if (addressDialogShown || isFinishing()) return;
        addressDialogShown = true;
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setText(getSharedPreferences(PREFS, MODE_PRIVATE).getString(KEY_URL, DEFAULT_URL));
        input.setSelectAllOnFocus(true);

        new AlertDialog.Builder(this)
            .setTitle("عنوان سيرفر اللعبة")
            .setMessage("اكتب IP الكمبيوتر والبورت، مثال: 192.168.1.10:4173")
            .setView(input)
            .setPositiveButton("فتح", (dialog, which) -> {
                String url = normalizedUrl(input.getText().toString());
                getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(KEY_URL, url).apply();
                addressDialogShown = false;
                webView.loadUrl(url);
            })
            .setNegativeButton("إلغاء", (dialog, which) -> addressDialogShown = false)
            .setOnCancelListener(dialog -> addressDialogShown = false)
            .show();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) enterImmersive();
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }
}
