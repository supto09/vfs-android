package com.example.vfsgm.data.network

import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.Protocol
import java.util.concurrent.TimeUnit

class MyApiClient {
    val client = OkHttpClient.Builder().apply {
        addInterceptor(ApiTraceInterceptor())
        connectionPool(ConnectionPool(5, 10, TimeUnit.MINUTES))
        protocols(listOf(Protocol.HTTP_2, Protocol.HTTP_1_1))
        connectTimeout(40, TimeUnit.SECONDS)
        readTimeout(40, TimeUnit.SECONDS)
        writeTimeout(40, TimeUnit.SECONDS)
    }.build()
}
