package com.example.vfsgm.data.api

import com.example.vfsgm.core.SealedResult
import com.example.vfsgm.data.constants.CountryCode
import com.example.vfsgm.data.constants.Gender
import com.example.vfsgm.data.constants.MissionCode
import com.example.vfsgm.data.constants.Nationality
import com.example.vfsgm.data.constants.VisaApplicationCenterCode
import com.example.vfsgm.data.constants.VisaCategoryCode
import com.example.vfsgm.data.dto.Applicant
import com.example.vfsgm.data.dto.LeasedAccount
import com.example.vfsgm.data.dto.Subject
import com.example.vfsgm.data.network.await
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.delay
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class SubjectApi {
    private val client by lazy(LazyThreadSafetyMode.PUBLICATION) {
        OkHttpClient.Builder().build()
    }
    private val moshi by lazy(LazyThreadSafetyMode.PUBLICATION) {
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    }
    private val leaseAccountResponseAdapter by lazy(LazyThreadSafetyMode.PUBLICATION) {
        moshi.adapter(LeaseAccountResponse::class.java)
    }

    suspend fun leaseAccount(subject: Subject): SealedResult<LeasedAccount> {
        val requestBodyJson = """
            {
              "leaseOwner": "device-1",
              "countryCode": "${subject.countryCode.id}",
              "missionCode": "${subject.missionCode.id}"
            }
            """.trimIndent()

        println(requestBodyJson)

        val mediaType = "application/json".toMediaType()
        val requestBody = requestBodyJson.toRequestBody(mediaType)


        val request = Request.Builder().apply {
            url("https://api.ashulo.org/vfs/accounts/lease")
            post(requestBody)
        }.build()

        return try {
            val call = client.newCall(request)
            call.await().use { res ->
                val bodyStr = res.body?.string().orEmpty()
                println("Lease Account Response: $bodyStr")

                if (!res.isSuccessful) throw IOException("HTTP ${res.code}: $bodyStr")

                val leaseAccountResponse = leaseAccountResponseAdapter.fromJson(bodyStr)
                    ?: throw IOException("Failed to parse LoginResponse. Body=$bodyStr")

                SealedResult.Success(
                    LeasedAccount(
                        email = leaseAccountResponse.email,
                        password = leaseAccountResponse.password
                    )
                )
            }
        } catch (error: Exception) {
            error.printStackTrace()

            SealedResult.Error(error)
        }
    }

    suspend fun reportBlock(email: String, subject: Subject): SealedResult<Unit> {
        val requestBodyJson = """
            {
              "email": "$email",
              "countryCode": "${subject.countryCode.id}",
              "missionCode": "${subject.missionCode.id}"
            }
            """.trimIndent()

        println(requestBodyJson)

        val mediaType = "application/json".toMediaType()
        val requestBody = requestBodyJson.toRequestBody(mediaType)


        val request = Request.Builder().apply {
            url("https://api.ashulo.org/vfs/accounts/reportBlocked")
            post(requestBody)
        }.build()

        return try {
            val call = client.newCall(request)
            call.await().use { res ->
                val bodyStr = res.body?.string().orEmpty()
                println("Report Block Response: $bodyStr")

                SealedResult.Success(Unit)
            }
        } catch (error: Exception) {
            error.printStackTrace()

            SealedResult.Error(error)
        }
    }

    fun getSubject(): Subject {
        val subject = Subject(
//            username = "papog38807@mekuron.com",
            username = "caweb66200@m3player.com",
            password = "CczEk4u6n!7Z9i$",
            countryCode = CountryCode.PAK,
            missionCode = MissionCode.UKR,
            vacCode = VisaApplicationCenterCode.LHE,
            visaCategoryCode = VisaCategoryCode.IP,
            applicants = listOf(
                Applicant(
                    firstName = "XI",
                    lastName = "WION",
                    gender = Gender.FEMALE,
                    dateOfBirth = "01/12/2000",
                    dialCode = "254",
                    contactNumber = "12324564223",
                    passportNumber = "A43573334",
                    passportExpiryDate = "17/12/2027",
                    emailId = "caweb66200@m3player.com",
                    nationalityCode = Nationality.PAKISTAN
                )
            )
        )

        return subject
    }
}


@JsonClass(generateAdapter = true)
data class LeaseAccountResponse(
    @Json(name = "id")
    val id: String,

    @Json(name = "email")
    val email: String,

    @Json(name = "password")
    val password: String,

    @Json(name = "countryCode")
    val countryCode: String,

    @Json(name = "missionCode")
    val missionCode: String,
)