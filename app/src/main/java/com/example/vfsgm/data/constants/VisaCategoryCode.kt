package com.example.vfsgm.data.constants

enum class VisaCategoryCode(val id: String) {
    IP("ip");

    companion object {
        fun fromId(id: String): VisaCategoryCode? =
            entries.firstOrNull { it.id == id }
    }
}