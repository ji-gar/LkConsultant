package com.io.lkconsultants

import android.app.Application
import com.room.roomy.retrofit.TokenProvider

class ApplicationClass : Application() {
    override fun onCreate() {
        super.onCreate()
        TokenProvider.init(applicationContext)

    }
}