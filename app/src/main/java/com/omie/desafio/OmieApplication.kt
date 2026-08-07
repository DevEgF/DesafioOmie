package com.omie.desafio

import android.app.Application
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.omie.desafio.core.analytics.di.CrashCounterEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class OmieApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        if (FirebaseCrashlytics.getInstance().didCrashOnPreviousExecution()) {
            val entryPoint = EntryPointAccessors.fromApplication(this, CrashCounterEntryPoint::class.java)
            CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                entryPoint.crashCounter().increment()
            }
        }
    }
}
