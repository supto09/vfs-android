package com.example.vfsgm

import android.app.Application
import com.example.vfsgm.network.CookieJarHolder

class MyApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        CookieJarHolder.init(this)
    }
}