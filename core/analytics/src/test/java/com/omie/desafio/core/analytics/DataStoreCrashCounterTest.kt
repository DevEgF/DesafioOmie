package com.omie.desafio.core.analytics

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DataStoreCrashCounterTest {
    private val crashCountKey = intPreferencesKey("crash_count")

    @Test
    fun `given no stored value, when getCrashCount is called, then returns zero`() = runTest {
        // Given
        val dataStore = mockk<DataStore<Preferences>>()
        every { dataStore.data } returns MutableStateFlow(emptyPreferences())

        // When
        val counter = DataStoreCrashCounter(dataStore)
        val result = counter.getCrashCount()

        // Then
        assertEquals(0, result)
    }

    @Test
    fun `given a stored value, when getCrashCount is called, then returns that value`() = runTest {
        // Given
        val prefs = emptyPreferences().toMutablePreferences().apply { set(crashCountKey, 3) }
        val dataStore = mockk<DataStore<Preferences>>()
        every { dataStore.data } returns MutableStateFlow(prefs)

        // When
        val counter = DataStoreCrashCounter(dataStore)
        val result = counter.getCrashCount()

        // Then
        assertEquals(3, result)
    }
}
