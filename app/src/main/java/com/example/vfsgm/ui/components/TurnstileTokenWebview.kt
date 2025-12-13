package com.example.vfsgm.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlin.apply
import kotlin.text.trimIndent



class TurnstileBridge(
    private val tokenCallback: (String?) -> Unit
) {
    @JavascriptInterface
    fun onToken(token: String?) {
        tokenCallback(token)
    }

    // Optional helpers if you call them from JS too:
    @JavascriptInterface
    fun onExpired() {
        tokenCallback(null)
    }

    @JavascriptInterface
    fun onError(msg: String?) {
        // log or handle if you want
    }
}


private const val BASE_ORIGIN = "https://visa.vfsglobal.com/pak/en/ukr/login"

@Composable
fun TurnstileTokenWebview(
    siteKey: String,
    onToken: (String?) -> Unit,
    onClose: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onClose,
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.width(200.dp)
        ) { Text("Close") }

        AndroidView(
            factory = { ctx: Context ->
                buildTurnstileOnlyWebView(ctx) { token ->
                    onToken(token)
                }
            },
            update = { webView ->
                loadTurnstileLocalHtml(webView, baseOrigin = BASE_ORIGIN, siteKey = siteKey)
            }
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
private fun buildTurnstileOnlyWebView(
    context: Context,
    onJsToken: (String?) -> Unit
): WebView = WebView(context).apply {
    WebView.setWebContentsDebuggingEnabled(true)
    settings.javaScriptEnabled = true
    settings.domStorageEnabled = true
    settings.databaseEnabled = true
    settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE

    // Allow cookies (Turnstile loads from CF domains)
    CookieManager.getInstance().setAcceptCookie(true)
    CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

    // We’re not loading the site; block navigations just in case
    webViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean = true
    }

    addJavascriptInterface(TurnstileBridge(onJsToken), "AndroidTurnstile")
}

/** Loads a minimal HTML that renders Turnstile; baseUrl sets the document origin to your host */
private fun loadTurnstileLocalHtml(webView: WebView, baseOrigin: String, siteKey: String) {
    val html = """
        <!doctype html>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <div id="ts-mount" style="margin:16px;"></div>
        <script>
          function tsOnload(){
            try {
              window.__tsId = turnstile.render('#ts-mount', {
                sitekey: '$siteKey',
                appearance: 'always',
                callback: function(token){
                  if (window.AndroidTurnstile && AndroidTurnstile.onToken) {
                    AndroidTurnstile.onToken(token);
                  }
                },
                "expired-callback": function(){
                  if (window.AndroidTurnstile && AndroidTurnstile.onToken) AndroidTurnstile.onToken('');
                },
                "error-callback": function(e){ console.log('Turnstile error', e); }
              });
            } catch (e) { console.log(e); }
          }
          // Optional helpers
          window.resetTurnstile = function(){
            try { if (window.__tsId && window.turnstile) window.turnstile.reset(window.__tsId); } catch(e){}
          };
        </script>
        <script src="https://challenges.cloudflare.com/turnstile/v0/api.js?onload=tsOnload"></script>
    """.trimIndent()

    // No site content is fetched; this only sets the page origin to your host.
    webView.loadDataWithBaseURL(baseOrigin, html, "text/html", "utf-8", null)
}