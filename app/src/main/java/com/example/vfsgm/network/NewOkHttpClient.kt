package com.example.vfsgm.network

import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.concurrent.TimeUnit
import kotlin.apply
import kotlin.collections.forEach
import kotlin.collections.isNotEmpty
import kotlin.text.isNotEmpty
import kotlin.text.isNullOrBlank

class ResponseTimeInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()

        // Record the start time
        val startTime = System.nanoTime()

        val response: Response = chain.proceed(request)

        // Record the end time
        val endTime = System.nanoTime()

        // Calculate the duration in milliseconds
        val responseTime = (endTime - startTime) / 1_000_000.0
        println("Response time: $responseTime ms")

        return response
    }
}


class CommonInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val builder = originalRequest.newBuilder()

        // --- User-Agent ---
        if (AgentHolder.agent.isNotEmpty()) {
            builder.header("User-Agent", AgentHolder.agent)
        }



        val newRequest = builder.build()

        // Debug print
        println(">>> Final headers for ${newRequest.url}")
        for ((name, value) in newRequest.headers) {
            println("   $name: $value")
        }

        return chain.proceed(newRequest)
    }
}

class CookieLoggingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val cookieHeader = request.header("Cookie")
        if (!cookieHeader.isNullOrBlank()) {
            println("🍪 Sent Cookies: $cookieHeader")
        } else {
            println("🍪 Sent Cookies No Cookie header in this request.")
        }

        val response = chain.proceed(request)

        val setCookieHeaders = response.headers("Set-Cookie")
        if (setCookieHeaders.isNotEmpty()) {
            println("📥 Received Set-Cookie headers:")
            setCookieHeaders.forEach { println("   ➤ $it") }
        } else {
            println("📥 Received Set-Cookie headers: No Set-Cookie headers received.")
        }

        return response
    }
}


//
class NewOkHttpClient(val followRedirect: Boolean = true) {
    val client = OkHttpClient.Builder().apply {
//        println("======================OkHttpClient client=======================")
//        CookieJarHolder.cookieJar.printAllCookies()
//        println("=============================================")

        addInterceptor(CommonInterceptor())
        addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        addNetworkInterceptor(CookieLoggingInterceptor())
        connectionPool(ConnectionPool(10, 10, TimeUnit.MINUTES)) // Config
        protocols(listOf(Protocol.HTTP_2, Protocol.HTTP_1_1)) // Enable HT
        connectTimeout(80, TimeUnit.SECONDS) // Set connection timeout
        readTimeout(80, TimeUnit.SECONDS) // Set read timeout
        writeTimeout(80, TimeUnit.SECONDS) // Set write timeout
        followRedirects(followRedirect)
        cookieJar(CookieJarHolder.cookieJar)
    }.build()
}