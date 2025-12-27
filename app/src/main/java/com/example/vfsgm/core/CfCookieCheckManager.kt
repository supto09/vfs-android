package com.example.vfsgm.core

import com.example.vfsgm.data.network.CookieJarHolder
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

object CfCookieCheckManager {
    suspend fun waitUntilCfCookieKeyExists(
        timeoutMs: Long = 30_000L,
        initialPollDelayMs: Long = 150L,
        maxPollDelayMs: Long = 1_000L
    ): Boolean {
        val result = withTimeoutOrNull(timeoutMs) {
            var delayMs = initialPollDelayMs.coerceAtLeast(0L)

            while (true) {
                val found = CookieJarHolder.cookieJar
                    .getCookiesForDomain("vfsglobal.com")
                    .any { it.name == "cf_clearance" }

                if (found) return@withTimeoutOrNull true


                println("waitUntilCfCookieKeyExists Cookie Not Found, Initiating Delay")

                delay(delayMs)
                delayMs = (delayMs * 2).coerceAtMost(maxPollDelayMs)
            }
        }

        return result == true
    }
}