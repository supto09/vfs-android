package com.example.vfsgm.data.dto


data class DataState(
    val urn: String = "",
    val earliestSlotDate: String = "",
    val availableDates: List<String> = emptyList()
)
