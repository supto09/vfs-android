package com.example.vfsgm.data.api

import com.example.vfsgm.core.SealedResult
import com.example.vfsgm.data.constants.CountryCode
import com.example.vfsgm.data.constants.Gender
import com.example.vfsgm.data.constants.MissionCode
import com.example.vfsgm.data.constants.Nationality
import com.example.vfsgm.data.constants.VisaApplicationCenterCode
import com.example.vfsgm.data.constants.VisaCategoryCode
import com.example.vfsgm.data.dto.Applicant
import com.example.vfsgm.data.dto.Entry
import com.example.vfsgm.data.dto.LeasedAccount
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

class LeasedAccountApi {
    private val client by lazy(LazyThreadSafetyMode.PUBLICATION) {
        OkHttpClient.Builder().build()
    }
    private val moshi by lazy(LazyThreadSafetyMode.PUBLICATION) {
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    }
    private val leaseAccountResponseAdapter by lazy(LazyThreadSafetyMode.PUBLICATION) {
        moshi.adapter(LeaseAccountResponse::class.java)
    }

    suspend fun leaseAccount(entry: Entry): SealedResult<LeasedAccount> {
        val requestBodyJson = """
            {
              "leaseOwner": "device-1",
              "countryCode": "${entry.countryCode.id}",
              "missionCode": "${entry.missionCode.id}"
            }
            """.trimIndent()

        println(requestBodyJson)

        val mediaType = "application/json".toMediaType()
        val requestBody = requestBodyJson.toRequestBody(mediaType)


        val request = Request.Builder().apply {
            url("https://vfsapi.ashulo.org/accounts/lease")
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

    suspend fun reportBlock(email: String, entry: Entry): SealedResult<Unit> {
        val requestBodyJson = """
            {
              "email": "$email",
              "countryCode": "${entry.countryCode.id}",
              "missionCode": "${entry.missionCode.id}"
            }
            """.trimIndent()

        println(requestBodyJson)

        val mediaType = "application/json".toMediaType()
        val requestBody = requestBodyJson.toRequestBody(mediaType)


        val request = Request.Builder().apply {
            url("https://vfsapi.ashulo.org/accounts/reportBlocked")
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

    fun getEntry(): Entry {
        val entry = Entry(
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

        return entry
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
