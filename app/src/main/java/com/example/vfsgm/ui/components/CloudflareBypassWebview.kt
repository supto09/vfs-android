package com.example.vfsgm.ui.components


import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.MotionEvent
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.vfsgm.network.AgentHolder
import com.example.vfsgm.network.CookieJarHolder
import okhttp3.Cookie
import kotlin.random.Random

const val timeoutMs = 40_000L // X time (example: 20 seconds)
const val interventionDelayMs = 50_000L


@Composable
fun CloudflareBypassWebview(
    restartCount: Int,
    onCompleted: () -> Unit,
    onTimeout: () -> Unit,
    onRequestManualIntervention: ()-> Unit
) {
    val context = LocalContext.current
    val urlToBypass = "https://visa.vfsglobal.com/pak/en/ukr/login"
//    val urlToBypass = "https://sergiodemo.com/security/challenge/legacy-challenge"

    var webViewRef: WebView? = null
    val stopTimersRef = remember { mutableStateOf<(() -> Unit)?>(null) }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
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
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        AndroidView(
            factory = {
                val webView = WebView(context)
                webViewRef = webView

                val mainHandler = Handler(Looper.getMainLooper())
                val timeoutHandler = Handler(Looper.getMainLooper())
                val interventionHandler = Handler(Looper.getMainLooper())

                lateinit var pollRunnable: Runnable
                lateinit var timeoutRunnable: Runnable

                fun stopAllTimers() {
                    // safest: wipe all callbacks/messages for each handler
                    mainHandler.removeCallbacksAndMessages(null)
                    timeoutHandler.removeCallbacksAndMessages(null)
                    interventionHandler.removeCallbacksAndMessages(null)
                }

                stopTimersRef.value = { stopAllTimers() }


                // =========================
                // 1) BASIC WEBVIEW SETTINGS
                // =========================
                webView.settings.javaScriptEnabled = true
                webView.settings.domStorageEnabled = true

                // =========================
                // 2) COOKIE SETUP + CLEAN START
                // =========================
                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(true)
                CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)

                // Clear old cookies and cache so the Cloudflare flow starts fresh.
                // Note: removeAllCookies clears cookies for ALL WebViews in the app.
                cookieManager.removeAllCookies(null)
                cookieManager.flush()

                webView.clearCache(true)
                webView.clearHistory()
                webView.clearFormData()

                // ==========================================
                // 3) COOKIE CHANGE TRACKING + SYNC TO OKHTTP
                // ==========================================
                var lastCookieSnapshot: String? = null

                // Helpers for checking the cookie we care about.
                fun hasClearanceCookie(): Boolean {
                    val all = cookieManager.getCookie(urlToBypass) ?: return false
                    return all.contains("cf_clearance=", ignoreCase = true)
                }

                fun logAndSyncIfChanged() {
                    val all = cookieManager.getCookie(urlToBypass) ?: return
                    if (all == lastCookieSnapshot) return

                    lastCookieSnapshot = all
//                    println("🍪 Cookie snapshot for $urlToBypass -> $all")

                    // Sync cookies from WebView -> OkHttp cookie jar (your existing logic)
                    syncCookiesFromWebViewToOkHttp(context, urlToBypass)

                    // Persist WebView cookie store ASAP
                    cookieManager.flush()
                }

                // ==========================================================
                // 4) TIMEOUT WATCHDOG:
                //    If cf_clearance doesn't appear within X time -> call dummy
                // ==========================================================

                var clearanceFound = false


                fun onClearanceTimeoutDummy() {
                    // Dummy function for now (future: notification)
                    println("⏰ Dummy timeout fired: cf_clearance NOT found within ${timeoutMs}ms")

                    if(restartCount >= 2){
                        // code for self repairing
                        clickWebview(webViewRef)

                        interventionHandler.postDelayed({
                            println("⚠️ Self-repair failed → awaiting manual intervention")
                            if (!clearanceFound && !hasClearanceCookie()) {
                                onRequestManualIntervention()
                            }
                        }, interventionDelayMs)
                    } else{
                        onTimeout()
                    }
                }

                 timeoutRunnable = Runnable {
                    // Only fire if still not found
                    if (!clearanceFound && !hasClearanceCookie()) {
                        onClearanceTimeoutDummy()
                    }
                }

                // Start countdown immediately when WebView is created/opened
                timeoutHandler.postDelayed(timeoutRunnable, timeoutMs)

                // ==========================================================
                // 5) 1-SECOND POLLING:
                //    Cloudflare can update cookies in background while "waiting"
                // ==========================================================
                pollRunnable = object : Runnable {
                    override fun run() {
                        // Regular cookie sync
                        logAndSyncIfChanged()

                        // If clearance cookie shows up, mark found + cancel timeout
                        if (!clearanceFound && hasClearanceCookie()) {
                            clearanceFound = true
                            onCompleted()
                            println("✅ cf_clearance detected (timeout cancelled)")
                        }

                        mainHandler.postDelayed(this, 1000L)
                    }
                }


                // Start/stop polling safely to avoid leaks
                webView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                    override fun onViewAttachedToWindow(v: View) {
                        mainHandler.postDelayed(pollRunnable, 1000L)
                    }

                    override fun onViewDetachedFromWindow(v: View) {
                        stopAllTimers()
                    }
                })

                // ==========================================
                // 6) WEBVIEW CLIENT: log & sync cookies on events
                // ==========================================
                webView.webViewClient = object : WebViewClient() {

                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        logAndSyncIfChanged()
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        AgentHolder.agent = webView.settings.userAgentString
                        logAndSyncIfChanged()

                        // If clearance cookie shows up here, cancel timeout
                        if (!clearanceFound && hasClearanceCookie()) {
                            clearanceFound = true
                            onCompleted()
                            println("✅ cf_clearance detected onPageFinished (timeout cancelled)")
                        }
                    }

                    override fun onLoadResource(view: WebView?, url: String?) {
                        super.onLoadResource(view, url)


                        logAndSyncIfChanged()

                        // If clearance cookie shows up during resource load, cancel timeout
                        if (!clearanceFound && hasClearanceCookie()) {
                            clearanceFound = true
                            onCompleted()
                            println("✅ cf_clearance detected onLoadResource (timeout cancelled)")
                        }
                    }

                    override fun shouldInterceptRequest(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): WebResourceResponse? {
                        val url = request?.url?.toString()
                            ?: return super.shouldInterceptRequest(view, request)

                        // Never block the main HTML document
                        if (request.isForMainFrame) {
                            return super.shouldInterceptRequest(view, request)
                        }

//                        println("Passed URL: $url")
                        logAndSyncIfChanged()

                        // If clearance cookie shows up here, cancel timeout
                        if (!clearanceFound && hasClearanceCookie()) {
                            clearanceFound = true
                            onCompleted()
                            println("✅ cf_clearance detected in shouldInterceptRequest (timeout cancelled)")
                        }

                        return super.shouldInterceptRequest(view, request)
                    }
                }

                // ==========================================
                // 7) DEBUG: capture tap coordinates
                // ==========================================
                webView.setOnTouchListener { _, e ->
                    if (e.action == MotionEvent.ACTION_DOWN) {
                        println("Last Tap: ${e.x} / ${e.y}")
                    }
                    false
                }

                // ==========================================
                // 8) LOAD URL (after clean state + watchers set)
                // ==========================================
                webView.loadUrl(urlToBypass)

                webView
            },
            onRelease = { view ->
                // Stop & destroy the WebView when Compose disposes it
                view.stopLoading()
                view.destroy()
                stopTimersRef.value?.invoke()
            }
        )
    }
}

fun syncCookiesFromWebViewToOkHttp(context: Context, webUrl: String) {
    println("❌ Running cookie sync")

    val uri = Uri.parse(webUrl)
    val domain = uri.host ?: return
    val cookieStr = CookieManager.getInstance().getCookie(webUrl) ?: return

    val targetCookies = mutableListOf<Cookie>()

    // Parse "name=value; name2=value2; ..."
    cookieStr.split(";").forEach { raw ->
        val entry = raw.trim()
        val eq = entry.indexOf('=')
        if (eq <= 0) return@forEach

        val name = entry.substring(0, eq).trim()
        val value = entry.substring(eq + 1).trim()

        // Only sync cf_clearance (as in your code)
        val isTarget = name.equals("cf_clearance", ignoreCase = true)

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
    } else {
        println("❌ No cf_clearance cookie found to sync")
    }
}

fun clickWebview(webViewRef: WebView?) {
    val now = SystemClock.uptimeMillis()
    val x = 450F
    val y = 605F

    fun randDelay(minMs: Long, maxMs: Long) = Random.nextLong(minMs, maxMs + 1)

    val down = MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN, x, y, 0)

    val moveT = now + randDelay(15, 50)
    val move = MotionEvent.obtain(
        now,
        moveT,
        MotionEvent.ACTION_MOVE,
        x + Random.nextInt(-2, 3),
        y + Random.nextInt(-2, 3),
        0
    )

    val upT = moveT + randDelay(20, 80)
    val up = MotionEvent.obtain(now, upT, MotionEvent.ACTION_UP, x, y, 0)

    webViewRef?.dispatchTouchEvent(down)
    webViewRef?.dispatchTouchEvent(move)
    webViewRef?.dispatchTouchEvent(up)

    down.recycle(); move.recycle(); up.recycle()
}
