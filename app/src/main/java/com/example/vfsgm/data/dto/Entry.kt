package com.example.vfsgm.data.dto

import com.example.vfsgm.data.constants.Gender
import com.example.vfsgm.data.constants.Nationality

data class Entry(
    val countryCode: String = "PAK", // from country
    val missionCode: String = "UKR", // target country
    val vacCode: String = "LHE",  // application center
    val visaCategoryCode: String = "IP",
    val applicants: List<Applicant> = mutableListOf()
)


data class Applicant(
    val firstName: String = "",
    val lastName: String = "",
    val gender: Gender = Gender.MALE,
    val dateOfBirth: String = "", // DD/MM/YYYY
    val dialCode: String = "",
    val contactNumber: String = "",
    val passportNumber: String = "",
    val passportExpiryDate: String = "", // DD/MM/YYYY
    val emailId: String = "",
    val nationalityCode: Nationality = Nationality.UNITED_STATES,
)
