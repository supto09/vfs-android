package com.example.vfsgm.data.constants

enum class MissionCode(val id: String) {
    UKR("ukr");

    companion object {
        fun fromId(id: String): MissionCode? =
            entries.firstOrNull { it.id == id }
    }
}