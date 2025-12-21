package com.example.vfsgm.data.dto

import com.example.vfsgm.data.constants.CountryCode
import com.example.vfsgm.data.constants.Gender
import com.example.vfsgm.data.constants.MissionCode
import com.example.vfsgm.data.constants.Nationality
import com.example.vfsgm.data.constants.VisaApplicationCenterCode
import com.example.vfsgm.data.constants.VisaCategoryCode

data class Subject(
    val username: String = "",
    val password: String = "",
    val countryCode: CountryCode = CountryCode.PAK, // from country
    val missionCode: MissionCode = MissionCode.UKR, // target country
    val vacCode: VisaApplicationCenterCode = VisaApplicationCenterCode.LHE,  // application center
    val visaCategoryCode: VisaCategoryCode = VisaCategoryCode.IP,
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