package com.modelrelease.cloud;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import android.content.ContentValues;
import android.provider.MediaStore.Downloads;
import androidx.webkit.WebViewAssetLoader;
import java.io.OutputStream;

public class MainActivity extends Activity {
    private static final int FILE_REQUEST = 101;
    private WebView webView;
    private ValueCallback<Uri[]> fileCallback;
    private final String origin = "https://appassets.androidplatform.net";

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        webView = new WebView(this);
        setContentView(webView);
        WebViewAssetLoader loader = new WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this)).build();
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccess(false);
        webView.getSettings().setAllowContentAccess(false);
        webView.getSettings().setMixedContentMode(android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        webView.addJavascriptInterface(new DownloadBridge(), "AndroidFiles");
        webView.setWebViewClient(new WebViewClient() {
            @Override public android.webkit.WebResourceResponse shouldInterceptRequest(WebView v, WebResourceRequest r) {
                return loader.shouldInterceptRequest(r.getUrl());
            }
            @Override public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                Uri u = r.getUrl();
                if (origin.equals(u.getScheme() + "://" + u.getHost())) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, u)); }
                catch (Exception e) { Toast.makeText(MainActivity.this, "No se pudo abrir el enlace", Toast.LENGTH_SHORT).show(); }
                return true;
            }
            @Override public void onPageFinished(WebView v, String url) {
                if (url.startsWith(origin + "/assets/")) v.evaluateJavascript(
                    "(function(){if(window.__androidPdfHook)return;window.__androidPdfHook=true;"+
                    "const click=HTMLAnchorElement.prototype.click;HTMLAnchorElement.prototype.click=function(){"+
                    "if(this.href && this.href.startsWith('blob:')){"+
                    "const name=this.download||'Model_Release.pdf';"+
                    "fetch(this.href).then(r=>r.blob()).then(b=>{const fr=new FileReader();fr.onload=()=>AndroidFiles.save(name,String(fr.result).split(',')[1]);fr.readAsDataURL(b)});return;}"+
                    "return click.call(this);};})();", null);
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView v, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (fileCallback != null) fileCallback.onReceiveValue(null);
                fileCallback = callback;
                try { startActivityForResult(params.createIntent(), FILE_REQUEST); return true; }
                catch (ActivityNotFoundException ex) { fileCallback = null; return false; }
            }
        });
        webView.loadUrl(origin + "/assets/offline.html");
    }

    public class DownloadBridge {
        @JavascriptInterface public void save(String filename, String base64) {
            try {
                byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
                String safe = filename.replaceAll("[^a-zA-Z0-9._-]", "_");
                if (!safe.toLowerCase().endsWith(".pdf")) safe += ".pdf";
                ContentValues values = new ContentValues();
                values.put(Downloads.DISPLAY_NAME, safe);
                values.put(Downloads.MIME_TYPE, "application/pdf");
                values.put(Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/ModelReleaseCloud");
                Uri uri = getContentResolver().insert(Downloads.EXTERNAL_CONTENT_URI, values);
                if (uri == null) throw new Exception("No se pudo crear el archivo");
                try (OutputStream out = getContentResolver().openOutputStream(uri)) { out.write(bytes); }
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "PDF guardado en Descargas/ModelReleaseCloud", Toast.LENGTH_LONG).show());
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error al guardar PDF", Toast.LENGTH_LONG).show());
            }
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_REQUEST && fileCallback != null) {
            fileCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
            fileCallback = null;
        }
    }
    @Override public void onBackPressed() {
        if (webView.canGoBack()) webView.goBack(); else super.onBackPressed();
    }
}
