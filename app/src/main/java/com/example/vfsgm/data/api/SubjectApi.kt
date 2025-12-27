package com.example.vfsgm.data.api

import com.example.vfsgm.data.constants.CountryCode
import com.example.vfsgm.data.constants.Gender
import com.example.vfsgm.data.constants.MissionCode
import com.example.vfsgm.data.constants.Nationality
import com.example.vfsgm.data.constants.VisaApplicationCenterCode
import com.example.vfsgm.data.constants.VisaCategoryCode
import com.example.vfsgm.data.dto.Applicant
import com.example.vfsgm.data.dto.Subject

class SubjectApi {
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