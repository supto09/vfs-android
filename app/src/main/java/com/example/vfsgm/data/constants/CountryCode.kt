package com.example.vfsgm.data.constants

enum class CountryCode(val id: String) {
    PAK("pak");

    companion object {
        fun fromId(id: String): CountryCode? =
            entries.firstOrNull { it.id == id }
    }
}