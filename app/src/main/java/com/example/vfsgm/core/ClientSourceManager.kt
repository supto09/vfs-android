package com.example.vfsgm.core

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object ClientSourceManager {
    fun getClientSource(mysteriousPrefix: String = "GA;"): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        val plainClientSource =
            mysteriousPrefix + LocalDateTime.now().format(formatter) + "Z"

        println("PLainClientSource: $plainClientSource")

        return EncryptionManager.encryptWithRsaOaepSha256(plainClientSource)
    }
}