package com.example.vfsgm.service

import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import javax.crypto.Cipher


class EncryptionService {
    fun encryptWithRsaOaepSha256(inputValue: String): String {

        // Exact known constants
        val PUBLIC_KEY_PEM_PREFIX = "-----BEGIN PUBLIC KEY-----"
        val PUBLIC_KEY_PEM_SUFFIX = "-----END PUBLIC KEY-----"

        // Exact rsaString value (from sessionStorage key: csk_str)
        val rsaKeyBody = """
        MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuupFgB+lYIOtSxrRoHzc
        LmCZKJ6+oSbgqgOPzFMM0TasOeLw0NXEn1XfIzXdx75+tegNKwyIZumoh0yhubKs
        t59GV321kN0iquYRHrdh3ygfDDHlS9rROQeBqRga0ncSADtbLMrBPqXJjPCoV76y
        t92towriKoH75BhiazY0mghm4LjmAWrV0u/GNpV3tk9bxbtHEXGaFmxCJqjg+7x6
        1e5wXLfvpj9w1QsiSWOSJxLOyICz/9ByxXycQQFdNmjnnnwco9Gt/Mi33NYH71j0
        5oXIjklFC4lvJqaqSY5lS7Vwb9oCt9zX9J0Yz4z4e/3V+0jgRnWOFGofyks4FKe2
        GQIDAQAB
    """.trimIndent().replace("\n", "")

        // Remove PEM headers/footers and decode Base64
        val publicKeyBytes = Base64.getDecoder().decode(rsaKeyBody)

        val keySpec = X509EncodedKeySpec(publicKeyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        val publicKey: PublicKey = keyFactory.generatePublic(keySpec)

        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)

        val encryptedBytes = cipher.doFinal(
            inputValue.toByteArray(Charsets.UTF_8)
        )

        return Base64.getEncoder().encodeToString(encryptedBytes)
    }

}