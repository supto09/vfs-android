package com.example.vfsgm.ui.components

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.vfsgm.core.logging.AppLogService
import com.example.vfsgm.core.logging.LogType
import com.example.vfsgm.ui.components.atomics.MySolidButton
import kotlinx.coroutines.delay

@Composable
fun CloudflareModalWrapper(deviceIndex: Int) {
    val showDialog = remember { mutableStateOf(false) }
    val restartCount = remember { mutableIntStateOf(0) }

    val reopenIntervalMinutes = 4L
    val reopenIntervalMs = reopenIntervalMinutes * 60_000L

    // 1) Auto-open once when this screen first appears
    LaunchedEffect(Unit) {
        AppLogService.log(deviceIndex, "Cloudflare modal initial auto-open", LogType.DEBUG, tag = "CloudflareModal")
        showDialog.value = true
    }

//    // 2) Auto-open every X minutes (this coroutine is cancelled automatically if composable is removed)
    LaunchedEffect(reopenIntervalMs) {
        while (true) {
            delay(reopenIntervalMs)
            if (!showDialog.value) {
                AppLogService.log(
                    deviceIndex,
                    "Cloudflare modal periodic reopen triggered",
                    LogType.INFO,
                    tag = "CloudflareModal",
                    metadata = mapOf("intervalMs" to reopenIntervalMs.toString())
                )
                restartCount.intValue = 0
                showDialog.value = true
            }
        }
    }

    Box {
        MySolidButton(
            onClick = {
                AppLogService.log(deviceIndex, "Cloudflare modal opened manually", LogType.INFO, tag = "CloudflareModal")
                showDialog.value = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("CF")
        }

        if (showDialog.value) {
            Dialog(onDismissRequest = {
                AppLogService.log(deviceIndex, "Cloudflare dialog dismissed", LogType.WARNING, tag = "CloudflareModal")
                showDialog.value = false
            }) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.8f)
                ) {
                    key(restartCount.intValue) {
                        CloudflareBypassWebview(
                            deviceIndex = deviceIndex,
                            restartCount = restartCount.intValue,
                            onCompleted = {
                                println("On Complete received")
                                AppLogService.log(
                                    deviceIndex,
                                    "Cloudflare bypass completed",
                                    LogType.SUCCESS,
                                    tag = "CloudflareModal",
                                    metadata = mapOf("restartCount" to restartCount.intValue.toString())
                                )
                                // Optional callback once cf_clearance is acquired
                                showDialog.value = false
                                restartCount.intValue = 0
                            },
                            onTimeout = {
                                println("⏰ Timeout → restarting WebView")
                                AppLogService.log(
                                    deviceIndex,
                                    "Cloudflare bypass timeout, restarting webview",
                                    LogType.WARNING,
                                    tag = "CloudflareModal",
                                    metadata = mapOf("restartCount" to restartCount.intValue.toString())
                                )
                                restartCount.intValue += 1

                                // 1) Close dialog
                                showDialog.value = false

                                // 2) Reopen after a short delay (forces fresh WebView)
                                Handler(Looper.getMainLooper()).postDelayed({
                                    showDialog.value = true
                                }, 300L) // 300ms is enough
                            },
                            onRequestManualIntervention = {
                                println("Manual Intervention required")
                                AppLogService.log(
                                    deviceIndex,
                                    "Cloudflare bypass requires manual intervention",
                                    LogType.ERROR,
                                    tag = "CloudflareModal",
                                    critical = true
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
