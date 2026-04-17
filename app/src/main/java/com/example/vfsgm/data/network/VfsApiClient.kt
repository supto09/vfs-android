package com.example.vfsgm.data.network

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

class VfsApiClient(val followRedirect: Boolean = true) {
    val client = OkHttpClient.Builder().apply {
        addInterceptor(CommonInterceptor())
        addInterceptor(ApiTraceInterceptor())
        connectionPool(ConnectionPool(10, 10, TimeUnit.MINUTES))
        protocols(listOf(Protocol.HTTP_2, Protocol.HTTP_1_1))
        connectTimeout(80, TimeUnit.SECONDS)
        readTimeout(80, TimeUnit.SECONDS)
        writeTimeout(80, TimeUnit.SECONDS)
        followRedirects(followRedirect)
        cookieJar(CookieJarHolder.cookieJar)
    }.build()
}
