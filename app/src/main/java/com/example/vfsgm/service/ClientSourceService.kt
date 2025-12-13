package com.example.vfsgm.service

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ClientSourceService {
    fun getClientSource(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        val plainClientSource =
            "GA;" + LocalDateTime.now().format(formatter) + "Z"

        println("PLainClientSource: $plainClientSource")


        return EncryptionService().encryptWithRsaOaepSha256(plainClientSource)
    }
}