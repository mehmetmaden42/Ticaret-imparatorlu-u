package com.maden.ticaretimparatorlugu.live;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import android.view.WindowManager;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Separate test shell: always loads the deployed GitHub Pages game.
 * Does not replace the original V53 APK and does not reuse its localStorage.
 */
public class MainActivity extends Activity {
    private static final String SITE = "https://mehmetmaden42.github.io/Ticaret-imparatorlu-u/";
    private static final int OPEN_JSON = 1401;
    private static final int SAVE_JSON = 1402;
    private WebView webView;
    private ValueCallback<Uri[]> fileChooser;
    private String pendingExport;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);

        webView = new WebView(this);
        setContentView(webView);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        // These are used by the V53 HTML, which checks for these exact names.
        webView.addJavascriptInterface(new UpdateBridge(), "AndroidGameUpdater");
        webView.addJavascriptInterface(new SaveBridge(), "AndroidGameSave");
        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest req) {
                Uri u = req.getUrl();
                boolean isGame = "https".equals(u.getScheme())
                    && "mehmetmaden42.github.io".equals(u.getHost())
                    && u.getPath() != null && u.getPath().startsWith("/Ticaret-imparatorlu-u/");
                if (isGame) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, u)); }
                catch (Exception ignored) { toast("Bağlantı açılamadı"); }
                return true;
            }
            @Override public void onPageFinished(WebView v, String url) {
                // Original V53 button is visible only in a native app.
                v.evaluateJavascript("document.body.classList.add('ti-native-app')", null);
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView v, ValueCallback<Uri[]> callback,
                                                        FileChooserParams params) {
                if (fileChooser != null) fileChooser.onReceiveValue(null);
                fileChooser = callback;
                Intent open = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                open.addCategory(Intent.CATEGORY_OPENABLE);
                open.setType("application/json");
                try { startActivityForResult(open, OPEN_JSON); }
                catch (ActivityNotFoundException error) {
                    fileChooser = null;
                    callback.onReceiveValue(null);
                    toast("Dosya seçici bulunamadı");
                }
                return true;
            }
        });
        webView.loadUrl(SITE);
    }

    private void toast(String msg) {
        runOnUiThread(() -> Toast.makeText(this, msg, Toast.LENGTH_LONG).show());
    }

    public final class UpdateBridge {
        @JavascriptInterface public void check() {
            runOnUiThread(() -> {
                toast("GitHub'daki güncel sürüm açılıyor...");
                // Clear HTTP cache, NOT cookies or localStorage/game save.
                webView.clearCache(true);
                webView.loadUrl(SITE + "?kontrol=" + System.currentTimeMillis());
            });
        }
    }

    public final class SaveBridge {
        @JavascriptInterface public void saveBase64(String filename, String base64) {
            if (base64 == null || base64.length() > 20_000_000) {
                toast("Yedek dosyası çok büyük veya boş");
                return;
            }
            String clean = filename == null ? "ticaret-imparatorlugu-kayit.json"
                : filename.replaceAll("[^A-Za-z0-9._-]", "_");
            if (!clean.endsWith(".json")) clean += ".json";
            final String name = clean;
            final String encoded = base64;
            runOnUiThread(() -> {
                try {
                    if (Build.VERSION.SDK_INT >= 29) {
                        byte[] data = Base64.decode(encoded, Base64.DEFAULT);
                        ContentValues values = new ContentValues();
                        values.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
                        values.put(MediaStore.MediaColumns.MIME_TYPE, "application/json");
                        values.put(MediaStore.MediaColumns.RELATIVE_PATH,
                                   Environment.DIRECTORY_DOWNLOADS + "/TicaretImparatorlugu");
                        Uri dst = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                        if (dst == null) throw new IllegalStateException("Downloads açılamadı");
                        try (OutputStream stream = getContentResolver().openOutputStream(dst)) {
                            if (stream == null) throw new IllegalStateException("Kayıt dosyası açılamadı");
                            stream.write(data);
                        }
                        toast("Yedek İndirilenler/TicaretImparatorlugu içine kaydedildi");
                    } else {
                        pendingExport = encoded;
                        Intent create = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                        create.addCategory(Intent.CATEGORY_OPENABLE);
                        create.setType("application/json");
                        create.putExtra(Intent.EXTRA_TITLE, name);
                        startActivityForResult(create, SAVE_JSON);
                    }
                } catch (Exception e) {
                    toast("Yedek kaydedilemedi: " + e.getMessage());
                }
            });
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OPEN_JSON) {
            if (fileChooser != null) {
                fileChooser.onReceiveValue(resultCode == RESULT_OK && data != null && data.getData() != null
                    ? new Uri[]{data.getData()} : null);
                fileChooser = null;
            }
        } else if (requestCode == SAVE_JSON) {
            try {
                if (resultCode == RESULT_OK && data != null && data.getData() != null
                    && pendingExport != null) {
                    try (OutputStream out = getContentResolver().openOutputStream(data.getData())) {
                        if (out == null) throw new IllegalStateException("Kayıt dosyası açılamadı");
                        out.write(Base64.decode(pendingExport, Base64.DEFAULT));
                    }
                    toast("Yedek kaydedildi");
                }
            } catch (Exception e) { toast("Yedek kaydedilemedi"); }
            finally { pendingExport = null; }
        }
    }

    @Override public void onBackPressed() {
        if (webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }
    @Override protected void onDestroy() {
        if (webView != null) { webView.destroy(); webView = null; }
        super.onDestroy();
    }
}
