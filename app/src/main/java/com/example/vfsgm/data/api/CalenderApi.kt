package com.example.vfsgm.data.api

import com.example.vfsgm.core.ClientSourceManager
import com.example.vfsgm.core.SealedResult
import com.example.vfsgm.data.constants.DATE_FORMAT
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
import java.time.LocalDateTime


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
    private val checkIsSlotAvailableResponseAdapter by lazy(LazyThreadSafetyMode.PUBLICATION) {
        moshi.adapter(CheckIsSlotAvailableResponse::class.java)
    }


    suspend fun checkIsSlotAvailable(
        accessToken: String,
        subject: Subject,
    ): SealedResult<String?> {
        val requestBodyJson = """
            {
              "countryCode": "${subject.countryCode.name.lowercase()}",
              "missionCode": "${subject.missionCode.name.lowercase()}",
              "vacCode": "${subject.vacCode.name}",
              "visaCategoryCode": "${subject.visaCategoryCode.name}",
              "loginUser": "${subject.username}",
              "roleName": "Individual",             
              "payCode": ""
            }
        """.trimIndent()

        val mediaType = "application/json;charset=UTF-8".toMediaType()
        val requestBody = requestBodyJson.toRequestBody(mediaType)

        val request = Request.Builder().apply {
            url("https://lift-api.vfsglobal.com/appointment/CheckIsSlotAvailable")
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


        return try {
            client.newCall(request).await().use { res ->
                val bodyStr = res.body?.string().orEmpty()
                println("CheckSlotAvailable Response: $bodyStr")

                if (!res.isSuccessful) throw IOException("HTTP ${res.code}: $bodyStr")

                val checkIsSlotAvailableResponse =
                    checkIsSlotAvailableResponseAdapter.fromJson(bodyStr)
                        ?: throw IOException("Failed to parse CheckSlotAvailable. Body=$bodyStr")

                val earliestDate = checkIsSlotAvailableResponse.earliestDate
                    ?.let {
                        LocalDateTime.parse(
                            it,
                            DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss")
                        )
                    }
                    ?.format(DateTimeFormatter.ofPattern(DATE_FORMAT))

                SealedResult.Success(earliestDate)
            }
        } catch (
            error: Exception
        ) {
            SealedResult.Error(error)
        }
    }


    suspend fun loadCalender(
        accessToken: String, subject: Subject, urn: String
    ): SealedResult<List<String>> {
        val todayFormatted = LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT))

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



        return try {
            client.newCall(request).await().use { res ->
                val bodyStr = res.body?.string().orEmpty()
                println("Load Calender Response: $bodyStr")

                if (!res.isSuccessful) {
                    throw IOException("HTTP ${res.code}: $bodyStr")
                }

                val parsed = loadCalenderResponseAdapter.fromJson(bodyStr)
                    ?: throw IOException("Failed to parse Load Calender Response. Body=$bodyStr")


                parsed.error?.let { err ->
                    throw Exception(err.description)
                }

                // keep order, remove duplicates
                val distinctDate = parsed.calendars.map { it.date }.distinct()
                SealedResult.Success(distinctDate)
            }
        } catch (error: Exception) {
            SealedResult.Error(error)
        }
    }
}


// load calender response
@JsonClass(generateAdapter = true)
data class LoadCalenderResponse(
    @Json(name = "mission") val mission: String,

    @Json(name = "center") val center: String,

    @Json(name = "visacategory") val visaCategory: String,

    @Json(name = "calendars") val calendars: List<CalendarDate>,

    @Json(name = "error") val error: ApiError?
)

@JsonClass(generateAdapter = true)
data class CalendarDate(
    @Json(name = "date") val date: String,

    @Json(name = "isWeekend") val isWeekend: Boolean
)


// check slot available response
@JsonClass(generateAdapter = true)
data class CheckIsSlotAvailableResponse(
    @Json(name = "earliestDate")
    val earliestDate: String?,

    @Json(name = "earliestSlotLists")
    val earliestSlotLists: List<EarliestSlot>?,

    @Json(name = "error")
    val error: ApiError?
)

@JsonClass(generateAdapter = true)
data class EarliestSlot(
    @Json(name = "applicant")
    val applicant: String,

    @Json(name = "date")
    val date: String
)


// common response
@JsonClass(generateAdapter = true)
data class ApiError(
    @Json(name = "code") val code: Int,

    @Json(name = "description") val description: String,

    @Json(name = "type") val type: String
)