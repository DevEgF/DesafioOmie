package com.omie.desafio.core.analytics.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.omie.desafio.core.analytics.AndroidDeviceMetricsProvider
import com.omie.desafio.core.analytics.DataStoreCrashCounter
import com.omie.desafio.core.analytics.FirebaseAnalyticsTracker
import com.omie.desafio.core.analytics.FirebaseRemoteConfigProvider
import com.omie.desafio.core.analytics.R
import com.omie.desafio.core.domain.AnalyticsTracker
import com.omie.desafio.core.domain.CrashCounter
import com.omie.desafio.core.domain.DeviceMetricsProvider
import com.omie.desafio.core.domain.RemoteConfigProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Shared local Preferences DataStore for core:analytics — backs both the crash counter
// (DataStoreCrashCounter) and the Developer Mode Remote Config override
// (FirebaseRemoteConfigProvider). Both consumers get the same Hilt-scoped singleton.
private val Context.crashCounterDataStore: DataStore<Preferences> by preferencesDataStore(name = "crash_counter")

@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsModule {
    @Binds
    @Singleton
    abstract fun bindAnalyticsTracker(impl: FirebaseAnalyticsTracker): AnalyticsTracker

    @Binds
    @Singleton
    abstract fun bindRemoteConfigProvider(impl: FirebaseRemoteConfigProvider): RemoteConfigProvider

    @Binds
    @Singleton
    abstract fun bindCrashCounter(impl: DataStoreCrashCounter): CrashCounter

    @Binds
    @Singleton
    abstract fun bindDeviceMetricsProvider(impl: AndroidDeviceMetricsProvider): DeviceMetricsProvider

    companion object {
        @Provides
        @Singleton
        fun provideFirebaseAnalytics(@ApplicationContext context: Context): FirebaseAnalytics =
            FirebaseAnalytics.getInstance(context)

        @Provides
        @Singleton
        fun provideFirebaseRemoteConfig(): FirebaseRemoteConfig {
            val remoteConfig = FirebaseRemoteConfig.getInstance()
            remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
            remoteConfig.fetchAndActivate()
            return remoteConfig
        }

        @Provides
        @Singleton
        fun provideCrashCounterDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
            context.crashCounterDataStore
    }
}
