package com.example.vfsgm.data.api

import com.example.vfsgm.data.network.NewOkHttpClient
import com.example.vfsgm.core.ClientSourceManager
import com.example.vfsgm.core.FirebaseLogService
import com.example.vfsgm.core.SealedResult
import com.example.vfsgm.data.dto.AppConfig
import com.example.vfsgm.data.dto.SessionData
import com.example.vfsgm.data.dto.Subject
import com.example.vfsgm.data.network.await
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class SlotApi {
    private val client by lazy(LazyThreadSafetyMode.PUBLICATION) {
        NewOkHttpClient().client
    }

    private val moshi by lazy(LazyThreadSafetyMode.PUBLICATION) {
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    }
    private val loadSlotResponseAdapter by lazy(LazyThreadSafetyMode.PUBLICATION) {
        moshi.adapter(LoadSlotResponse::class.java)
    }


    suspend fun loadSlots(
        sessionData: SessionData,
        subject: Subject,
        urn: String,
        slotDate: String,
        appConfig: AppConfig
    ): SealedResult<List<String>> {
        val requestBodyJson = """
            {
                "countryCode": "${subject.countryCode}",
                "missionCode": "${subject.missionCode}",
                "centerCode": "${subject.vacCode}",
                "loginUser": "${sessionData.username}",
                "visaCategoryCode": "${subject.visaCategoryCode}",
                "slotDate": "$slotDate",
                "urn": "$urn"
            }
            """.trimIndent()

        FirebaseLogService.log(
            appConfig.deviceIndex,
            "LoadApplicants: $requestBodyJson"
        )

        val mediaType = "application/json;charset=UTF-8".toMediaType()
        val requestBody = requestBodyJson.toRequestBody(mediaType)

        val request = Request.Builder().apply {
            url("https://lift-api.vfsglobal.com/appointment/timeslot")
            post(requestBody)
            addHeader(
                "clientsource", ClientSourceManager.getClientSource(
                    mysteriousPrefix = "GA;"
                )
            )
            addHeader("Authorize", sessionData.accessToken)
            addHeader("accept", "application/json, text/plain, */*")
            addHeader("Origin", "https://visa.vfsglobal.com")
            addHeader("Referer", "https://visa.vfsglobal.com/")
        }.build()

        return try {
            client.newCall(request).await().use { res ->
                val bodyStr = res.body?.string().orEmpty()
                println("Load Calender Response: $bodyStr")

                if (!res.isSuccessful) {
                    throw IOException("HTTP ${res.code}: $bodyStr")
                }

                val parsed = loadSlotResponseAdapter.fromJson(bodyStr)
                    ?: throw IOException("Failed to parse Load Slot Response. Body=$bodyStr")


                parsed.error?.let { err ->
                    throw Exception(err.description)
                }


                val allocationIds = parsed.slots.map { it.allocationId }.distinct()
                SealedResult.Success(allocationIds)
            }
        } catch (error: Exception) {
            SealedResult.Error(error)
        }
    }
}

@JsonClass(generateAdapter = true)
data class LoadSlotResponse(
    @Json(name = "mission")
    val mission: String,

    @Json(name = "center")
    val center: String,

    @Json(name = "visacategory")
    val visaCategory: String,

    @Json(name = "date")
    val date: String,

    @Json(name = "slots")
    val slots: List<VisaSlot>,

    @Json(name = "error")
    val error: ErrorResponse?
)

@JsonClass(generateAdapter = true)
data class VisaSlot(
    @Json(name = "visaGroupName")
    val visaGroupName: String,

    @Json(name = "allocationId")
    val allocationId: String,

    @Json(name = "slot")
    val slot: String,

    @Json(name = "type")
    val type: String,

    @Json(name = "allocationCategory")
    val allocationCategory: String,

    @Json(name = "categoryCode")
    val categoryCode: String
)
