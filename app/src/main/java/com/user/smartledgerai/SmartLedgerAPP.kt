package com.user.smartledgerai

import android.app.Application
import timber.log.Timber
import com.user.smartledgerai.BuildConfig
class SmartLedgerAPP : Application() {
    override fun onCreate() {
        super.onCreate()
        if(BuildConfig.Debug){
            Timber.plant(Timber.DebugTree())
        }
    }
}