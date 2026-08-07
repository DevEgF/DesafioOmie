package com.omie.desafio.core.analytics

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.omie.desafio.core.domain.RemoteConfigProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseRemoteConfigProvider @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig,
    private val overrideDataStore: DataStore<Preferences>,
) : RemoteConfigProvider {
    override fun isSaleDetailEnabled(): Boolean =
        getSaleDetailEnabledOverride() ?: remoteConfig.getBoolean("sale_detail_enabled")

    override fun getSaleDetailEnabledOverride(): Boolean? = runBlocking {
        overrideDataStore.data.first()[SALE_DETAIL_ENABLED_KEY]
    }

    override suspend fun setSaleDetailEnabledOverride(enabled: Boolean?) {
        overrideDataStore.edit { prefs ->
            if (enabled == null) {
                prefs.remove(SALE_DETAIL_ENABLED_KEY)
            } else {
                prefs[SALE_DETAIL_ENABLED_KEY] = enabled
            }
        }
    }

    private companion object {
        val SALE_DETAIL_ENABLED_KEY = booleanPreferencesKey("sale_detail_enabled_override")
    }
}
