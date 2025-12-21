package com.example.vfsgm.data.constants

enum class VisaApplicationCenterCode(val id: String) {
    LHE("LHE");

    companion object {
        fun fromId(id: String): VisaApplicationCenterCode? =
            entries.firstOrNull { it.id == id }
    }
}