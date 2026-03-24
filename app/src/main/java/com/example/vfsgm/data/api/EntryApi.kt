package com.example.vfsgm.data.api

import com.example.vfsgm.data.constants.CountryCode
import com.example.vfsgm.data.constants.Gender
import com.example.vfsgm.data.constants.MissionCode
import com.example.vfsgm.data.constants.Nationality
import com.example.vfsgm.data.constants.VisaApplicationCenterCode
import com.example.vfsgm.data.constants.VisaCategoryCode
import com.example.vfsgm.data.dto.Applicant
import com.example.vfsgm.data.dto.Entry
import com.example.vfsgm.data.network.MyApiClient
import com.example.vfsgm.data.network.await
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Request
import java.io.IOException
import java.util.Locale

class EntryApi {
    private val client by lazy(LazyThreadSafetyMode.PUBLICATION) {
        MyApiClient().client
    }
    private val moshi by lazy(LazyThreadSafetyMode.PUBLICATION) {
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    }
    private val entryResponseAdapter by lazy(LazyThreadSafetyMode.PUBLICATION) {
        moshi.adapter(EntryApiResponse::class.java)
    }

    suspend fun getEntry(entryIndex: Int): Entry {
        val request = Request.Builder().apply {
            url("https://vfsapi.ashulo.org/v1/entries/$entryIndex")
            get()
        }.build()

        return try {
            client.newCall(request).await().use { res ->
                val bodyStr = res.body?.string().orEmpty()
                if (!res.isSuccessful) throw IOException("HTTP ${res.code}: $bodyStr")

                val parsed = entryResponseAdapter.fromJson(bodyStr)
                    ?: throw IOException("Failed to parse EntryResponse. Body=$bodyStr")

                parsed.toEntry()
            }
        } catch (error: Exception) {
            error.printStackTrace()
            throw IOException("Failed to load entry for index=$entryIndex", error)
        }
    }
    private fun fallbackStaticEntry(): Entry {
        return Entry(
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
    }
}

private fun EntryApiResponse.toEntry(): Entry {
    val mappedCountryCode = countryCode?.let { raw ->
        CountryCode.fromId(raw.lowercase(Locale.US))
            ?: CountryCode.entries.firstOrNull { it.name.equals(raw, ignoreCase = true) }
    } ?: CountryCode.PAK

    val mappedMissionCode = missionCode?.let { raw ->
        MissionCode.fromId(raw.lowercase(Locale.US))
            ?: MissionCode.entries.firstOrNull { it.name.equals(raw, ignoreCase = true) }
    } ?: MissionCode.UKR

    val mappedVacCode = vacCode?.let { raw ->
        VisaApplicationCenterCode.fromId(raw.uppercase(Locale.US))
            ?: VisaApplicationCenterCode.entries.firstOrNull { it.name.equals(raw, ignoreCase = true) }
    } ?: VisaApplicationCenterCode.LHE

    val mappedVisaCategoryCode = visaCategoryCode?.let { raw ->
        VisaCategoryCode.fromId(raw.lowercase(Locale.US))
            ?: VisaCategoryCode.entries.firstOrNull { it.name.equals(raw, ignoreCase = true) }
    } ?: VisaCategoryCode.IP

    val mappedApplicants = applicants.orEmpty().map { it.toApplicant() }

    return Entry(
        countryCode = mappedCountryCode,
        missionCode = mappedMissionCode,
        vacCode = mappedVacCode,
        visaCategoryCode = mappedVisaCategoryCode,
        applicants = mappedApplicants
    )
}

private fun EntryApplicantApiResponse.toApplicant(): Applicant {
    return Applicant(
        firstName = firstName.orEmpty(),
        lastName = lastName.orEmpty(),
        gender = parseGender(gender),
        dateOfBirth = dateOfBirth.orEmpty(),
        dialCode = dialCode.orEmpty(),
        contactNumber = contactNumber.orEmpty(),
        passportNumber = passportNumber.orEmpty(),
        passportExpiryDate = passportExpiryDate.orEmpty(),
        emailId = emailId.orEmpty(),
        nationalityCode = parseNationality(nationalityCode)
    )
}

private fun parseGender(raw: String?): Gender {
    val normalized = raw.orEmpty().trim()
    if (normalized.isEmpty()) return Gender.NOT_SPECIFIED

    normalized.toIntOrNull()?.let { id ->
        return Gender.fromId(id) ?: Gender.NOT_SPECIFIED
    }

    return when (normalized.uppercase(Locale.US).replace(" ", "_")) {
        "MALE" -> Gender.MALE
        "FEMALE" -> Gender.FEMALE
        "OTHERS", "OTHER", "TRANSGENDER" -> Gender.OTHERS
        "NOT_SPECIFIED", "NOTSPECIFIED" -> Gender.NOT_SPECIFIED
        else -> Gender.NOT_SPECIFIED
    }
}

private fun parseNationality(raw: String?): Nationality {
    val normalized = raw.orEmpty().trim()
    if (normalized.isEmpty()) return Nationality.UNKNOWN

    val fromIso = Nationality.fromIso(normalized)
    if (fromIso != Nationality.UNKNOWN) return fromIso

    return Nationality.entries.firstOrNull {
        it.name.equals(normalized.replace(" ", "_"), ignoreCase = true) ||
            it.displayName.equals(normalized, ignoreCase = true)
    } ?: Nationality.UNKNOWN
}

@JsonClass(generateAdapter = true)
data class EntryApiResponse(
    @Json(name = "index")
    val index: Int?,

    @Json(name = "countryCode")
    val countryCode: String?,

    @Json(name = "missionCode")
    val missionCode: String?,

    @Json(name = "vacCode")
    val vacCode: String?,

    @Json(name = "visaCategoryCode")
    val visaCategoryCode: String?,

    @Json(name = "applicants")
    val applicants: List<EntryApplicantApiResponse>?,
)

@JsonClass(generateAdapter = true)
data class EntryApplicantApiResponse(
    @Json(name = "firstName")
    val firstName: String?,

    @Json(name = "lastName")
    val lastName: String?,

    @Json(name = "gender")
    val gender: String?,

    @Json(name = "dateOfBirth")
    val dateOfBirth: String?,

    @Json(name = "dialCode")
    val dialCode: String?,

    @Json(name = "contactNumber")
    val contactNumber: String?,

    @Json(name = "passportNumber")
    val passportNumber: String?,

    @Json(name = "passportExpiryDate")
    val passportExpiryDate: String?,

    @Json(name = "emailId")
    val emailId: String?,

    @Json(name = "nationalityCode")
    val nationalityCode: String?,
)
