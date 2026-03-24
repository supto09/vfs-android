package com.example.vfsgm.data.api

import com.example.vfsgm.core.ClientSourceManager
import com.example.vfsgm.core.SealedResult
import com.example.vfsgm.data.dto.Entry
import com.example.vfsgm.data.dto.SessionData
import com.example.vfsgm.data.network.VfsApiClient
import com.example.vfsgm.data.network.await
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

data class ScheduleResult(
    val statusCode: Int,
    val responsePreview: String
)

class ScheduleApi {
    private val client by lazy(LazyThreadSafetyMode.PUBLICATION) {
        VfsApiClient().client
    }
    private val moshi by lazy(LazyThreadSafetyMode.PUBLICATION) {
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    }
    private val responseAdapter by lazy(LazyThreadSafetyMode.PUBLICATION) {
        moshi.adapter(ScheduleApiResponse::class.java)
    }

    suspend fun schedule(
        sessionData: SessionData,
        entry: Entry,
        urn: String,
        allocationId: String
    ): SealedResult<ScheduleResult> {
        val requestBodyJson = """
            {
              "missionCode": "${entry.missionCode.id}",
              "countryCode": "${entry.countryCode.id}",
              "centerCode": "${entry.vacCode.id}",
              "loginUser": "${sessionData.username}",
              "urn": "$urn",
              "aurn": null,
              "notificationType": "none",
              "paymentdetails": {
                "paymentmode": "Vac",
                "RequestRefNo": "",
                "clientId": "",
                "merchantId": "",
                "amount": 0,
                "currency": "PKR"
              },
              "allocationId": "$allocationId",
              "CanVFSReachoutToApplicant": false,
              "TnCConsentAndAcceptance": true
            }
            """.trimIndent()

        val requestBody = requestBodyJson.toRequestBody("application/json;charset=UTF-8".toMediaType())

        val request = Request.Builder().apply {
            url("https://lift-api.vfsglobal.com/appointment/schedule")
            post(requestBody)
            addHeader(
                "clientsource", ClientSourceManager.getClientSource(
                    mysteriousPrefix = "GA;"
                )
            )
            addHeader("Authorize", sessionData.accessToken)
            addHeader("accept", "application/json, text/plain, */*")
            addHeader("content-type", "application/json;charset=UTF-8")
            addHeader("route", "${entry.countryCode.id}/en/${entry.missionCode.id}")
            addHeader("Origin", "https://visa.vfsglobal.com")
            addHeader("Referer", "https://visa.vfsglobal.com/")
            tag(String::class.java, "appointment.schedule")
        }.build()

        return try {
            client.newCall(request).await().use { res ->
                val bodyStr = res.body?.string().orEmpty()
                val preview = bodyStr.take(700)

                if (!res.isSuccessful) {
                    throw IOException("HTTP ${res.code}: $preview")
                }

                val parsed = responseAdapter.fromJson(bodyStr)
                parsed?.error?.let { err ->
                    throw IOException("Schedule error: ${err.description} (code=${err.code})")
                }

                SealedResult.Success(
                    ScheduleResult(
                        statusCode = res.code,
                        responsePreview = preview
                    )
                )
            }
        } catch (error: Exception) {
            SealedResult.Error(error)
        }
    }
}

@JsonClass(generateAdapter = true)
private data class ScheduleApiResponse(
    @Json(name = "error")
    val error: ErrorResponse?
)
