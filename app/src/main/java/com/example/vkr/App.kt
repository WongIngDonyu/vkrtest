package com.example.vkr

import android.app.Application
import com.yandex.mapkit.MapKitFactory

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setApiKey("4a93e277-e50e-4263-a1a4-cca7ef9bd9a4")
        MapKitFactory.initialize(this)
    }
}