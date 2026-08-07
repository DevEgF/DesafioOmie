package com.omie.desafio.core.analytics

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.omie.desafio.core.domain.CrashCounter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreCrashCounter @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : CrashCounter {
    override fun getCrashCount(): Int = runBlocking {
        dataStore.data.first()[CRASH_COUNT_KEY] ?: 0
    }

    suspend fun increment() {
        dataStore.edit { prefs ->
            val current = prefs[CRASH_COUNT_KEY] ?: 0
            prefs[CRASH_COUNT_KEY] = current + 1
        }
    }

    private companion object {
        val CRASH_COUNT_KEY = intPreferencesKey("crash_count")
    }
}
