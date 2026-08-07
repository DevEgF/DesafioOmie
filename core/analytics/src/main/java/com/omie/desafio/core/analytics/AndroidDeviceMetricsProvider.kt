package com.omie.desafio.core.analytics

import android.app.ActivityManager
import android.content.Context
import android.os.Process
import com.omie.desafio.core.domain.DeviceMetricsProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidDeviceMetricsProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) : DeviceMetricsProvider {
    override fun getAppMemoryUsageMb(): Long {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = activityManager.getProcessMemoryInfo(intArrayOf(Process.myPid()))
        val totalPssKb = memoryInfo.firstOrNull()?.totalPss ?: 0
        return totalPssKb / 1024L
    }

    override fun getAppStorageUsageMb(): Long {
        val dbBytes = context.getDatabasePath("omie_desafio.db").length()
        val cacheBytes = context.cacheDir
            ?.walkTopDown()
            ?.filter { it.isFile }
            ?.sumOf { it.length() }
            ?: 0L
        return (dbBytes + cacheBytes) / (1024L * 1024L)
    }
}
