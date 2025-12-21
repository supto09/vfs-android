package com.example.vfsgm.data.constants


enum class Gender(val id: Int) {
    MALE(1),
    FEMALE(2),
    OTHERS(3),
    NOT_SPECIFIED(65);

    companion object {
        fun fromId(id: Int): Gender? =
            Gender.entries.firstOrNull { it.id == id }
    }
}

//const val Gender = {
//    "genders": [
//    {
//        "id": 1,
//        "langaugeName": "Male",
//        "isoCode": "Male"
//    },
//    {
//        "id": 2,
//        "langaugeName": "Female",
//        "isoCode": "Female"
//    },
//    {
//        "id": 3,
//        "langaugeName": "Others / Transgender",
//        "isoCode": "Others"
//    },
//    {
//        "id": 65,
//        "langaugeName": "Not Specified",
//        "isoCode": "Not Specified"
//    }
//    ],
//    "error": null
//}