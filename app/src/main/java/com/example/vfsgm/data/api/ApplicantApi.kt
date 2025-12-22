package com.example.vfsgm.data.api

import com.example.vfsgm.data.network.NewOkHttpClient
import com.example.vfsgm.core.ClientSourceManager
import com.example.vfsgm.data.dto.Subject
import com.example.vfsgm.data.network.PublicIpManager
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

class ApplicantApi {
    fun loadApplicants(accessToken: String, username: String) {
        val requestBodyJson = """
            {
              "countryCode": "pak",
              "missionCode": "ukr",
              "languageCode": "en-US",
              "visaToken": null,
              "loginUser": "$username"
            }
            """.trimIndent()

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
            addHeader("Authorize", accessToken)
            addHeader("accept", "application/json, text/plain, */*")
            addHeader("Origin", "https://visa.vfsglobal.com")
            addHeader("Referer", "https://visa.vfsglobal.com/")
        }.build()

        NewOkHttpClient().client.newCall(request).enqueue(object : Callback {
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

    fun addApplicant(
        accessToken: String,
        subject: Subject
    ) {
        val requestBodyJson = """
        {
          "countryCode": "${subject.countryCode.name.lowercase()}",
          "missionCode": "${subject.missionCode.name.lowercase()}",
          "centerCode": "${subject.vacCode.name}",
          "loginUser": "${subject.username}",
          "visaCategoryCode": "${subject.visaCategoryCode.name}",
          "isEdit": false,
          "feeEntryTypeCode": null,
          "feeExemptionTypeCode": null,
          "feeExemptionDetailsCode": null,
          "applicantList": [
            ${
            subject.applicants.joinToString(",") { applicant ->
                """
                    {
                      "urn": "",
                      "arn": "",
                      "centerClassCode": null,
                      "selectedSubvisaCategory": null,
                      "Subclasscode": null,
                      "dateOfApplication": null,
                      "loginUser": "${subject.username}",
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
            addHeader("Authorize", accessToken)
            addHeader("accept", "application/json, text/plain, */*")
            addHeader("Origin", "https://visa.vfsglobal.com")
            addHeader("Referer", "https://visa.vfsglobal.com/")
        }.build()

        NewOkHttpClient().client.newCall(request).enqueue(object : Callback {
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

        NewOkHttpClient().client.newCall(request).enqueue(object : Callback {
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