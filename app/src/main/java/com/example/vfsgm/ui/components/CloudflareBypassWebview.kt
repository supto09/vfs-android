package com.example.vfsgm.ui.components


import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.vfsgm.network.AgentHolder
import com.example.vfsgm.network.CookieJarHolder
import okhttp3.Cookie


@Composable
fun CloudflareBypassWebview(onCompleted: () -> Unit) {
    val context = LocalContext.current
    val urlToBypass = "https://visa.vfsglobal.com/pak/en/ukr/login"

    var webViewRef: WebView? = null

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
    ) {
        Box(
            modifier = Modifier.padding(8.dp)
        ) {
            Button(
                onClick = {
                    println("OnComplete Clicked")
                    onCompleted()
                },
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    modifier = Modifier.size(18.dp) // icon size
                )
            }
        }

        AndroidView(
            factory = {
                val webView = WebView(context)
                webViewRef = webView  // save reference

                webView.settings.javaScriptEnabled = true
                webView.settings.domStorageEnabled = true

                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(true)
                CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)

                // Track last snapshot to avoid noisy logs
                var lastCookieSnapshot: String? = null

                fun logAndSyncIfChanged() {
                    val all = cookieManager.getCookie(urlToBypass) ?: return
                    if (all == lastCookieSnapshot) return
                    lastCookieSnapshot = all
                    println("🍪 Cookie snapshot for $urlToBypass -> $all")
                    syncCookiesFromWebViewToOkHttp(context, urlToBypass)
                    cookieManager.flush() // persist ASAP
                }

                // 1s polling to catch background updates during waiting room
                val handler = Handler(Looper.getMainLooper())
                val poll = object : Runnable {
                    override fun run() {
                        logAndSyncIfChanged()
                        handler.postDelayed(this, 1000L)
                    }
                }

                // Stop polling when the view is detached to avoid leaks
                webView.addOnAttachStateChangeListener(object :
                    View.OnAttachStateChangeListener {
                    override fun onViewAttachedToWindow(v: View) {
                        handler.postDelayed(poll, 1000L)
                    }

                    override fun onViewDetachedFromWindow(v: View) {
                        handler.removeCallbacks(poll)
                    }
                })

                webView.webViewClient = object : WebViewClient() {
                    override fun onPageStarted(
                        view: WebView?,
                        url: String?,
                        favicon: Bitmap?
                    ) {
                        super.onPageStarted(view, url, favicon)
                        logAndSyncIfChanged()
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        AgentHolder.agent = webView.settings.userAgentString
                        logAndSyncIfChanged()
                    }

                    override fun onLoadResource(view: WebView?, url: String?) {
                        super.onLoadResource(view, url)
                        // You'll often see Cloudflare update cookies while loading resources:
                        // e.g., https://static.cloudflareinsights.com/beacon.min.js/...
                        if (url?.contains("cloudflare", ignoreCase = true) == true ||
                            url?.contains("beacon.min.js", ignoreCase = true) == true
                        ) {
                            println("📡 Cloudflare resource loaded: $url")
                        }
                        logAndSyncIfChanged()
                    }

                    override fun shouldInterceptRequest(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): WebResourceResponse? {
                        val url = request?.url?.toString() ?: return super.shouldInterceptRequest(
                            view,
                            request
                        )

                        // never block the main HTML document
                        if (request.isForMainFrame)
                            return super.shouldInterceptRequest(view, request)

//                        if (isAllowedIndexJs(url)) {
//                            Handler(Looper.getMainLooper()).post {
//                                indexJsUrl = url
//                            }
//                        }

//                        if (isFromPaymentHost(url) && isJsOrCss(url) && !isAllowedIndexJs(url)) {
//                            println("🚫 Blocking JS/CSS from payment.ivacbd.com: $url")
//                            logAndSyncIfChanged()
//                            return blockedResponse()
//                        }

                        println("Passed URL: $url")

                        logAndSyncIfChanged()
                        return super.shouldInterceptRequest(view, request)
                    }
                }

                webView.loadUrl(urlToBypass)
                webView
            },
            onRelease = { view ->
                view.stopLoading()
                view.destroy()
            }
        )
    }


}

fun syncCookiesFromWebViewToOkHttp(context: Context, webUrl: String) {
    val uri = Uri.parse(webUrl)
    val domain = uri.host ?: return
    val cookieStr = CookieManager.getInstance().getCookie(webUrl) ?: return

    val targetCookies = mutableListOf<Cookie>()
    cookieStr.split(";").forEach { raw ->
        val entry = raw.trim()
        val eq = entry.indexOf('=')
        if (eq <= 0) return@forEach

        val name = entry.substring(0, eq).trim()
        val value = entry.substring(eq + 1).trim()

        val isTarget = name.equals("cf_clearance", ignoreCase = true) ||
                name.startsWith("__cfwaitingroom_", ignoreCase = true)

        if (isTarget && value.isNotEmpty()) {
            try {
                targetCookies += Cookie.Builder()
                    .domain(domain)
                    .path("/")
                    .name(name)
                    .value(value)
                    .httpOnly()
                    .secure()
                    .build()
            } catch (_: Throwable) {
                // ignore malformed cookie entries
            }
        }
    }

    if (targetCookies.isNotEmpty()) {
        CookieJarHolder.cookieJar.addCookiesManually("lift-api.vfsglobal.com", targetCookies)
//        CookieJarHolder.cookieJar.addCookiesManually("api-payment.ivacbd.com", targetCookies)
//        println("✅ Synced to OkHttp: ${targetCookies.joinToString { it.name }}")

//        println("All Cookies in CookieJar")
//        CookieJarHolder.cookieJar.printAllCookies()
    } else {
        println("❌ No cf_clearance / __cfwaitingroom_* cookies found to sync")
    }

    // Ensure WebView side is persisted
//    CookieManager.getInstance().flush()
}