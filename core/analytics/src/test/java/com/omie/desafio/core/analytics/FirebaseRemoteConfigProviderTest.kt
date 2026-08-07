package com.omie.desafio.core.analytics

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.emptyPreferences
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class FirebaseRemoteConfigProviderTest {
    private val saleDetailEnabledKey = booleanPreferencesKey("sale_detail_enabled_override")
    private val remoteConfig = mockk<FirebaseRemoteConfig>()
    private val dataStore = mockk<DataStore<Preferences>>()
    private val provider = FirebaseRemoteConfigProvider(remoteConfig, dataStore)

    @Test
    fun `given no override and the flag is true in remote config, when isSaleDetailEnabled is called, then returns true`() {
        // Given
        every { dataStore.data } returns MutableStateFlow(emptyPreferences())
        every { remoteConfig.getBoolean("sale_detail_enabled") } returns true

        // When
        val result = provider.isSaleDetailEnabled()

        // Then
        assertEquals(true, result)
    }

    @Test
    fun `given no override and the flag is false in remote config, when isSaleDetailEnabled is called, then returns false`() {
        // Given
        every { dataStore.data } returns MutableStateFlow(emptyPreferences())
        every { remoteConfig.getBoolean("sale_detail_enabled") } returns false

        // When
        val result = provider.isSaleDetailEnabled()

        // Then
        assertEquals(false, result)
    }

    @Test
    fun `given a local override is set, when isSaleDetailEnabled is called, then it wins over the remote value`() {
        // Given
        val prefs = emptyPreferences().toMutablePreferences().apply { set(saleDetailEnabledKey, false) }
        every { dataStore.data } returns MutableStateFlow(prefs)
        every { remoteConfig.getBoolean("sale_detail_enabled") } returns true

        // When
        val result = provider.isSaleDetailEnabled()

        // Then
        assertEquals(false, result)
    }

    @Test
    fun `given no override stored, when getSaleDetailEnabledOverride is called, then returns null`() {
        // Given
        every { dataStore.data } returns MutableStateFlow(emptyPreferences())

        // When
        val result = provider.getSaleDetailEnabledOverride()

        // Then
        assertNull(result)
    }

    @Test
    fun `given a non-null value, when setSaleDetailEnabledOverride is called, then it writes the override`() = runTest {
        // Given
        val transformSlot = slot<suspend (Preferences) -> Preferences>()
        coEvery { dataStore.updateData(capture(transformSlot)) } coAnswers {
            transformSlot.captured(emptyPreferences())
        }

        // When
        provider.setSaleDetailEnabledOverride(true)

        // Then
        coVerify { dataStore.updateData(any()) }
    }
}
