package com.user.smartledgerai

import android.app.Application

class SmartLedgerAPP : Application() {
    override fun onCreate() {
        super.onCreate()
        if(BuildConfig.Debug){
            Timber.plant(Timber.DebugTree())
        }
    }
}