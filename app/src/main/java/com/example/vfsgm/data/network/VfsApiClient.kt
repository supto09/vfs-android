package com.example.vfsgm.data.network

import com.example.vfsgm.core.logging.LogType
import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import java.util.concurrent.TimeUnit

class CommonInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val builder = originalRequest.newBuilder()

        if (AgentHolder.agent.isNotEmpty()) {
            builder.header("User-Agent", AgentHolder.agent)
        }

        return chain.proceed(builder.build())
    }
}

class CookieLoggingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val cookieHeader = request.header("Cookie")

        if (!cookieHeader.isNullOrBlank()) {
            logNetwork(
                "Request sent with cookie header",
                LogType.DEBUG,
                metadata = mapOf(
                    "host" to request.url.host,
                    "cookieHeader" to "<redacted>"
                )
            )
        } else {
            logNetwork(
                "Request sent without cookie header",
                LogType.DEBUG,
                metadata = mapOf("host" to request.url.host)
            )
        }

        val response = chain.proceed(request)
        val setCookieHeaders = response.headers("Set-Cookie")
        if (setCookieHeaders.isNotEmpty()) {
            logNetwork(
                "Response contained Set-Cookie headers",
                LogType.DEBUG,
                metadata = mapOf(
                    "host" to request.url.host,
                    "count" to setCookieHeaders.size.toString()
                )
            )
        }

        return response
    }
}

class VfsApiClient(val followRedirect: Boolean = true) {
    val client = OkHttpClient.Builder().apply {
        addInterceptor(CommonInterceptor())
        addInterceptor(ApiTraceInterceptor())
        addNetworkInterceptor(CookieLoggingInterceptor())
        connectionPool(ConnectionPool(10, 10, TimeUnit.MINUTES))
        protocols(listOf(Protocol.HTTP_2, Protocol.HTTP_1_1))
        connectTimeout(80, TimeUnit.SECONDS)
        readTimeout(80, TimeUnit.SECONDS)
        writeTimeout(80, TimeUnit.SECONDS)
        followRedirects(followRedirect)
        cookieJar(CookieJarHolder.cookieJar)
    }.build()
}
