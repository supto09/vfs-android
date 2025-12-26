package com.example.vfsgm.data.api

import com.example.vfsgm.core.ClientSourceManager
import com.example.vfsgm.data.dto.Subject
import com.example.vfsgm.data.network.NewOkHttpClient
import com.example.vfsgm.data.network.await
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory


class CalenderApi {
    private val client by lazy(LazyThreadSafetyMode.PUBLICATION) {
        NewOkHttpClient().client
    }
    private val moshi by lazy(LazyThreadSafetyMode.PUBLICATION) {
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    }
    private val loadCalenderResponseAdapter by lazy(LazyThreadSafetyMode.PUBLICATION) {
        moshi.adapter(LoadCalenderResponse::class.java)
    }

    suspend fun loadCalender(
        accessToken: String,
        subject: Subject,
        urn: String
    ): List<String> {
        val todayFormatted = LocalDate.now()
            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

        val requestBodyJson = """
            {
              "countryCode": "${subject.countryCode.name.lowercase()}",
              "missionCode": "${subject.missionCode.name.lowercase()}",
              "centerCode": "${subject.vacCode.name}",
              "loginUser": "${subject.username}",
              "visaCategoryCode": "${subject.visaCategoryCode.name}",
              "fromDate": "$todayFormatted",
              "urn": "$urn",
              "payCode": ""
            }
        """.trimIndent()

        val mediaType = "application/json;charset=UTF-8".toMediaType()
        val requestBody = requestBodyJson.toRequestBody(mediaType)

        val request = Request.Builder().apply {
            url("https://lift-api.vfsglobal.com/appointment/calendar")
            post(requestBody)
            addHeader(
                "clientsource", ClientSourceManager.getClientSource(
                    mysteriousPrefix = "GA;"
                )
            )
            addHeader("Authorize", accessToken)
            addHeader("accept", "application/json, text/plain, */*")
            addHeader("Origin", "https://visa.vfsglobal.com")
            addHeader("Referer", "https://visa.vfsglobal.com/")
        }.build()

        val call = client.newCall(request)
        call.await().use { res ->
            val bodyStr = res.body?.string().orEmpty()
            println("Load Calender Response: $bodyStr")

            if (!res.isSuccessful) throw IOException("HTTP ${res.code}: $bodyStr")

            val loadCalenderResponse = loadCalenderResponseAdapter.fromJson(bodyStr)
                ?: throw IOException("Failed to parse Load Calender Response. Body=$bodyStr")

            val calenders = loadCalenderResponse.calendars.map { it.date }.distinct()
            return calenders
        }
    }
}


@JsonClass(generateAdapter = true)
data class LoadCalenderResponse(
    @Json(name = "mission")
    val mission: String,

    @Json(name = "center")
    val center: String,

    @Json(name = "visacategory")
    val visaCategory: String,

    @Json(name = "calendars")
    val calendars: List<CalendarDate>,

    @Json(name = "error")
    val error: ApiError?
)

@JsonClass(generateAdapter = true)
data class CalendarDate(
    @Json(name = "date")
    val date: String,

    @Json(name = "isWeekend")
    val isWeekend: Boolean
)

@JsonClass(generateAdapter = true)
data class ApiError(
    @Json(name = "code")
    val code: Int,

    @Json(name = "description")
    val description: String,

    @Json(name = "type")
    val type: String
)