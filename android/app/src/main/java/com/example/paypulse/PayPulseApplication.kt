package com.example.paypulse

import android.app.Application
import com.example.paypulse.data.AppContainer

class PayPulseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContainer.init(this)
    }
}
