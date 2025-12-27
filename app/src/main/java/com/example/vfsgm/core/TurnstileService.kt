package com.example.vfsgm.core

import com.example.vfsgm.data.constants.SITE_KEY
import com.example.vfsgm.data.constants.TWO_CAPTCHA_KEY
import com.twocaptcha.TwoCaptcha
import com.twocaptcha.captcha.Turnstile


object TurnstileService {
    suspend fun solveTurnstile(): String? {
        val solver = TwoCaptcha(TWO_CAPTCHA_KEY)
        val captcha = Turnstile()

        captcha.setUrl("https://visa.vfsglobal.com/pak/en/ukr/login")
        captcha.setSiteKey(SITE_KEY)

        println("Solving mal with site key: $SITE_KEY")
        return try {
            solver.solve(captcha)
            println("captcha.code")
            println("================")
            println(captcha.code)
            captcha.code
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}

