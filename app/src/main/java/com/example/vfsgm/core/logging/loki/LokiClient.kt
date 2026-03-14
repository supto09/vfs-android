package com.example.vfsgm.core.logging.loki

import com.example.vfsgm.core.logging.LogEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

sealed class LokiUploadResult {
    data object Success : LokiUploadResult()
    data class RetryableFailure(val code: Int?, val body: String?, val reason: String) : LokiUploadResult()
    data class FatalFailure(val code: Int?, val body: String?, val reason: String) : LokiUploadResult()
}

class LokiClient(
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()
) {
    suspend fun push(events: List<LogEvent>): LokiUploadResult = withContext(Dispatchers.IO) {
        if (events.isEmpty()) return@withContext LokiUploadResult.Success

        val payload = LokiPayload.build(events)
        val request = Request.Builder()
            .url(LokiConfig.PUSH_URL)
            .header("Authorization", Credentials.basic(LokiConfig.USERNAME, LokiConfig.PASSWORD))
            .post(payload.toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()

        try {
            okHttpClient.newCall(request).execute().use { response ->
                val responseBody = response.body?.string()?.take(1000)
                return@withContext when {
                    response.isSuccessful -> {
                        println("✅ Loki upload success: status=${response.code}, events=${events.size}")
                        LokiUploadResult.Success
                    }

                    response.code == 429 || response.code >= 500 -> LokiUploadResult.RetryableFailure(
                        code = response.code,
                        body = responseBody,
                        reason = "Loki retryable HTTP error"
                    )

                    else -> LokiUploadResult.FatalFailure(
                        code = response.code,
                        body = responseBody,
                        reason = "Loki unrecoverable HTTP error"
                    )
                }
            }
        } catch (e: IOException) {
            return@withContext LokiUploadResult.RetryableFailure(
                code = null,
                body = null,
                reason = e.message ?: "Network IOException"
            )
        }
    }
}
