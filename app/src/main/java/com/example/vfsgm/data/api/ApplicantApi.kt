package com.example.vfsgm.data.api

import com.example.vfsgm.data.network.VfsApiClient
import com.example.vfsgm.core.ClientSourceManager
import com.example.vfsgm.core.logging.AppLogService
import com.example.vfsgm.data.dto.AppConfig
import com.example.vfsgm.data.dto.Entry
import com.example.vfsgm.data.dto.SessionData
import com.example.vfsgm.data.network.PublicIpManager
import com.example.vfsgm.data.network.await
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

class ApplicantApi {

    private val client by lazy(LazyThreadSafetyMode.PUBLICATION) {
        VfsApiClient().client
    }

    private val moshi by lazy(LazyThreadSafetyMode.PUBLICATION) {
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    }
    private val addApplicantResponseAdapter by lazy(LazyThreadSafetyMode.PUBLICATION) {
        moshi.adapter(AddApplicantResponse::class.java)
    }


    suspend fun loadApplicants(sessionData: SessionData, entry: Entry, appConfig: AppConfig) {
        val requestBodyJson = """
            {
              "countryCode": "${entry.countryCode}",
              "missionCode": "${entry.missionCode}",
              "languageCode": "en-US",
              "visaToken": null,
              "loginUser": "${sessionData.username}"
            }
            """.trimIndent()

        AppLogService.log(
            appConfig.deviceIndex,
            "LoadApplicants: $requestBodyJson"
        )

        val mediaType = "application/json;charset=UTF-8".toMediaType()
        val requestBody = requestBodyJson.toRequestBody(mediaType)

        val request = Request.Builder().apply {
            url("https://lift-api.vfsglobal.com/appointment/application")
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

        val call = client.newCall(request)
        call.await().use { res ->
            val bodyStr = res.body?.string().orEmpty()
            println("LoadApplicants Status: ${res.code}")
            println(bodyStr)

            AppLogService.log(
                appConfig.deviceIndex,
                "LoadApplicants: Status: ${res.code} Body: $bodyStr"
            )

            if (!res.isSuccessful) throw IOException("HTTP ${res.code}: $bodyStr")
        }
    }

    suspend fun addApplicant(
        sessionData: SessionData,
        entry: Entry,
        appConfig: AppConfig
    ): String {
        val requestBodyJson = """
        {
          "countryCode": "${entry.countryCode}",
          "missionCode": "${entry.missionCode}",
          "centerCode": "${entry.vacCode}",
          "loginUser": "${sessionData.username}",
          "visaCategoryCode": "${entry.visaCategoryCode}",
          "isEdit": false,
          "feeEntryTypeCode": null,
          "feeExemptionTypeCode": null,
          "feeExemptionDetailsCode": null,
          "applicantList": [
            ${
            entry.applicants.joinToString(",") { applicant ->
                """
                    {
                      "urn": "",
                      "arn": "",
                      "centerClassCode": null,
                      "selectedSubvisaCategory": null,
                      "Subclasscode": null,
                      "dateOfApplication": null,
                      "loginUser": "${sessionData.username}",
                      "firstName": "${applicant.firstName}",
                      "employerFirstName": "",
                      "middleName": "",
                      "lastName": "${applicant.lastName}",
                      "employerLastName": "",
                      "salutation": "",
                      "gender": ${applicant.gender.id},
                      "nationalId": null,
                      "VisaToken": null,
                      "employerContactNumber": "",
                      "contactNumber": "${applicant.contactNumber}",
                      "dialCode": "${applicant.dialCode}",
                      "employerDialCode": "",
                      "passportNumber": "${applicant.passportNumber}",
                      "confirmPassportNumber": null,
                      "passportExpirtyDate": "${applicant.passportExpiryDate}",
                      "dateOfBirth": "${applicant.dateOfBirth}",
                      "emailId": "${applicant.emailId.uppercase()}",
                      "employerEmailId": "",
                      "nationalityCode": "${applicant.nationalityCode.isoCode}",
                      "state": null,
                      "city": null,
                      "isEndorsedChild": false,
                      "applicantType": 0,
                      "addressline1": null,
                      "addressline2": null,
                      "pincode": null,
                      "referenceNumber": null,
                      "vlnNumber": null,
                      "applicantGroupId": 0,
                      "parentPassportNumber": "",
                      "parentPassportExpiry": "",
                      "dateOfDeparture": null,
                      "entryType": "",
                      "eoiVisaType": "",
                      "passportType": "",
                      "vfsReferenceNumber": "",
                      "familyReunificationCerificateNumber": "",
                      "PVRequestRefNumber": "",
                      "PVStatus": "",
                      "PVStatusDescription": "",
                      "PVCanAllowRetry": true,
                      "PVisVerified": false,
                      "eefRegistrationNumber": "",
                      "isAutoRefresh": true,
                      "helloVerifyNumber": "",
                      "OfflineCClink": "",
                      "idenfystatuscheck": false,
                      "vafStatus": null,
                      "SpecialAssistance": "",
                      "AdditionalRefNo": null,
                      "juridictionCode": "",
                      "canInitiateVAF": false,
                      "canEditVAF": false,
                      "canDeleteVAF": false,
                      "canDownloadVAF": false,
                      "Retryleft": "",
                      "ipAddress": "${PublicIpManager.publicIp}"
                    }
                    """.trimIndent()
            }
        }
          ],
          "languageCode": "en-US",
          "isWaitlist": false,
          "juridictionCode": null,
          "regionCode": null
        }
        """.trimIndent()

        println(
            requestBodyJson.replace("\n", "")
                .replace("\r", "")
        )

        AppLogService.log(
            appConfig.deviceIndex,
            "AddApplicants: requestBodyJson: ${
                requestBodyJson.replace("\n", "")
                    .replace("\r", "")
            }"
        )


        val mediaType = "application/json;charset=UTF-8".toMediaType()
        val requestBody = requestBodyJson.toRequestBody(mediaType)

        val request = Request.Builder().apply {
            url("https://lift-api.vfsglobal.com/appointment/applicants")
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

        val call = client.newCall(request)
        call.await().use { res ->
            val bodyStr = res.body?.string().orEmpty()
            println("Add Applicant Code: ${res.code}: Response: $bodyStr")
            AppLogService.log(
                appConfig.deviceIndex,
                "Add Applicant Response: $bodyStr"
            )

            if (!res.isSuccessful) throw IOException("HTTP ${res.code}: $bodyStr")

            val addApplicantResponse = addApplicantResponseAdapter.fromJson(bodyStr)
                ?: throw IOException("Failed to parse Add Applicant Response. Body=$bodyStr")

            return addApplicantResponse.urn ?: ""
        }
    }


    fun getGender(accessToken: String) {
        val request = Request.Builder().apply {
            url("https://lift-api.vfsglobal.com/master/gender/en-US")
            get()
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

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    println("Status: ${it.code}")
                    println(it.body?.string())
                }
            }
        })
    }
}


@JsonClass(generateAdapter = true)
private data class AddApplicantResponse(
    @Json(name = "urn")
    val urn: String?,

    @Json(name = "applicantList")
    val applicantList: List<ApplicantResponse>,

    @Json(name = "status")
    val status: String?,

    @Json(name = "error")
    val error: ErrorResponse?
)

@JsonClass(generateAdapter = true)
private data class ApplicantResponse(
    @Json(name = "arn")
    val arn: String,

    @Json(name = "firstName")
    val firstName: String,

    @Json(name = "lastName")
    val lastName: String,

    @Json(name = "passportNumber")
    val passportNumber: String,

    @Json(name = "isPackagePurchaseMandatory")
    val isPackagePurchaseMandatory: Boolean,

    @Json(name = "isPhotoUpload")
    val isPhotoUpload: Boolean,

    @Json(name = "idenfystatuscheck")
    val idenfystatuscheck: Boolean,

    @Json(name = "isWatermarkValid")
    val isWatermarkValid: Boolean
)

@JsonClass(generateAdapter = true)
data class ErrorResponse(
    @Json(name = "code")
    val code: Int,

    @Json(name = "description")
    val description: String,

    @Json(name = "type")
    val type: String
)
