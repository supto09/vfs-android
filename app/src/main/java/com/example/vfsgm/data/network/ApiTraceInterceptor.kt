package com.example.vfsgm.data.network

import com.example.vfsgm.core.logging.AppLogService
import com.example.vfsgm.core.logging.DeviceIndexContext
import com.example.vfsgm.core.logging.LogType
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import java.io.IOException
import java.nio.charset.StandardCharsets
import kotlin.random.Random

private const val NETWORK_LOG_TAG = "ApiTrace"
private const val BODY_PREVIEW_LIMIT = 1200
private val SENSITIVE_KEYS = listOf(
    "password",
    "accessToken",
    "authorization",
    "cf_turnstile_response",
    "token",
    "cookie"
)

internal fun logNetwork(
    message: String,
    type: LogType = LogType.INFO,
    metadata: Map<String, String> = emptyMap()
) {
    AppLogService.log(
        deviceIndex = DeviceIndexContext.get(),
        message = message,
        logType = type,
        tag = NETWORK_LOG_TAG,
        metadata = metadata
    )
}

class ApiTraceInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestId = buildRequestId()
        val requestStartMs = System.currentTimeMillis()
        val requestPath = request.url.encodedPath
        val apiLabel = request.tag(String::class.java) ?: requestPath

        logNetwork(
            message = "HTTP START [$requestId]",
            type = LogType.INFO,
            metadata = mapOf(
                "requestId" to requestId,
                "apiLabel" to apiLabel,
                "method" to request.method,
                "url" to request.url.toString(),
                "path" to requestPath,
                "headers" to summarizeHeaders(request)
            )
        )

        return try {
            val response = chain.proceed(request)
            val durationMs = System.currentTimeMillis() - requestStartMs
            val responseBodyPreview = redactSensitiveText(response.peekBody(BODY_PREVIEW_LIMIT.toLong()).string())
                .replace("\n", " ")
                .take(BODY_PREVIEW_LIMIT)

            val type = if (response.isSuccessful) LogType.SUCCESS else LogType.WARNING
            logNetwork(
                message = "HTTP END [$requestId] -> ${response.code}",
                type = type,
                metadata = mapOf(
                    "requestId" to requestId,
                    "apiLabel" to apiLabel,
                    "method" to request.method,
                    "url" to request.url.toString(),
                    "statusCode" to response.code.toString(),
                    "durationMs" to durationMs.toString(),
                    "responseHeaders" to summarizeHeaders(response),
                    "responseBodyPreview" to responseBodyPreview
                )
            )
            response
        } catch (e: IOException) {
            val durationMs = System.currentTimeMillis() - requestStartMs
            logNetwork(
                message = "HTTP FAIL [$requestId]",
                type = LogType.ERROR,
                metadata = mapOf(
                    "requestId" to requestId,
                    "apiLabel" to apiLabel,
                    "method" to request.method,
                    "url" to request.url.toString(),
                    "durationMs" to durationMs.toString(),
                    "error" to (e.message ?: "unknown")
                )
            )
            throw e
        }
    }

    private fun buildRequestId(): String {
        val now = System.currentTimeMillis().toString(36)
        val rnd = Random.nextInt(0, 0xFFFF).toString(16).padStart(4, '0')
        return "$now-$rnd"
    }

    private fun summarizeHeaders(request: Request): String {
        return request.headers.names()
            .sorted()
            .joinToString(",") { name ->
                if (name.equals("Authorization", ignoreCase = true) ||
                    name.equals("Cookie", ignoreCase = true)
                ) "$name=<redacted>" else "$name=${request.header(name)}"
            }
            .take(BODY_PREVIEW_LIMIT)
    }

    private fun summarizeHeaders(response: Response): String {
        return response.headers.names()
            .sorted()
            .joinToString(",") { name ->
                if (name.equals("Set-Cookie", ignoreCase = true)) "$name=<redacted>" else "$name=${response.header(name)}"
            }
            .take(BODY_PREVIEW_LIMIT)
    }

    private fun requestBodyPreview(request: Request): String {
        val body = request.body ?: return ""
        return runCatching {
            val buffer = Buffer()
            body.writeTo(buffer)
            val charset = body.contentType()?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8
            val content = buffer.readString(charset)
            redactSensitiveText(content).replace("\n", " ").take(BODY_PREVIEW_LIMIT)
        }.getOrDefault("<unavailable>")
    }
}

private fun redactSensitiveText(raw: String): String {
    var masked = raw
    SENSITIVE_KEYS.forEach { key ->
        val jsonPattern = Regex("(?i)(\"$key\"\\s*:\\s*\")(.*?)(\")")
        masked = masked.replace(jsonPattern, "$1<redacted>$3")

        val formPattern = Regex("(?i)($key=)([^&\\s]+)")
        masked = masked.replace(formPattern, "$1<redacted>")
    }
    return masked
}
