package com.omie.desafio.feature.devtools.presentation

import app.cash.turbine.test
import com.omie.desafio.core.domain.CrashCounter
import com.omie.desafio.core.domain.DeviceMetricsProvider
import com.omie.desafio.core.domain.RemoteConfigProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DeveloperModeViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val crashCounter = mockk<CrashCounter>()
    private val deviceMetricsProvider = mockk<DeviceMetricsProvider>()
    private val remoteConfigProvider = mockk<RemoteConfigProvider>(relaxed = true)

    private fun buildViewModel() = DeveloperModeViewModel(crashCounter, deviceMetricsProvider, remoteConfigProvider)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `given crash count, device metrics and the flag state, when state is observed, then it exposes all values`() = runTest {
        // Given
        every { crashCounter.getCrashCount() } returns 2
        every { deviceMetricsProvider.getAppMemoryUsageMb() } returns 120
        every { deviceMetricsProvider.getAppStorageUsageMb() } returns 8
        every { remoteConfigProvider.isSaleDetailEnabled() } returns true
        every { remoteConfigProvider.getSaleDetailEnabledOverride() } returns null

        // When
        val viewModel = buildViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertEquals(2, state.crashCount)
        assertEquals(120L, state.memoryUsageMb)
        assertEquals(8L, state.storageUsageMb)
        assertEquals(true, state.saleDetailEnabled)
        assertEquals(false, state.isLoading)
    }

    @Test
    fun `given no local override stored, when state is observed, then saleDetailOverrideActive is false`() = runTest {
        // Given
        every { crashCounter.getCrashCount() } returns 0
        every { deviceMetricsProvider.getAppMemoryUsageMb() } returns 0
        every { deviceMetricsProvider.getAppStorageUsageMb() } returns 0
        every { remoteConfigProvider.isSaleDetailEnabled() } returns true
        every { remoteConfigProvider.getSaleDetailEnabledOverride() } returns null

        // When
        val viewModel = buildViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(false, viewModel.state.value.saleDetailOverrideActive)
    }

    @Test
    fun `given a local override stored, when state is observed, then saleDetailOverrideActive is true`() = runTest {
        // Given
        every { crashCounter.getCrashCount() } returns 0
        every { deviceMetricsProvider.getAppMemoryUsageMb() } returns 0
        every { deviceMetricsProvider.getAppStorageUsageMb() } returns 0
        every { remoteConfigProvider.isSaleDetailEnabled() } returns false
        every { remoteConfigProvider.getSaleDetailEnabledOverride() } returns false

        // When
        val viewModel = buildViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(true, viewModel.state.value.saleDetailOverrideActive)
    }

    @Test
    fun `given the back action, when dispatched, then emits NavigateBack event`() = runTest {
        // Given
        every { crashCounter.getCrashCount() } returns 0
        every { deviceMetricsProvider.getAppMemoryUsageMb() } returns 0
        every { deviceMetricsProvider.getAppStorageUsageMb() } returns 0
        val viewModel = buildViewModel()

        // When / Then
        viewModel.events.test {
            viewModel.onAction(DeveloperModeAction.OnBackClick)
            assertEquals(DeveloperModeEvent.NavigateBack, awaitItem())
        }
    }

    @Test
    fun `given the sale detail toggle is flipped off, when dispatched, then persists the override and updates state`() = runTest {
        // Given
        every { crashCounter.getCrashCount() } returns 0
        every { deviceMetricsProvider.getAppMemoryUsageMb() } returns 0
        every { deviceMetricsProvider.getAppStorageUsageMb() } returns 0
        every { remoteConfigProvider.isSaleDetailEnabled() } returns true
        every { remoteConfigProvider.getSaleDetailEnabledOverride() } returns null
        coEvery { remoteConfigProvider.setSaleDetailEnabledOverride(any()) } returns Unit
        val viewModel = buildViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.onAction(DeveloperModeAction.OnSaleDetailToggle(enabled = false))
        dispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { remoteConfigProvider.setSaleDetailEnabledOverride(false) }
        assertEquals(false, viewModel.state.value.saleDetailEnabled)
        assertEquals(true, viewModel.state.value.saleDetailOverrideActive)
    }

    @Test
    fun `given an override is active, when reset is dispatched, then clears the override and falls back to the remote value`() = runTest {
        // Given
        every { crashCounter.getCrashCount() } returns 0
        every { deviceMetricsProvider.getAppMemoryUsageMb() } returns 0
        every { deviceMetricsProvider.getAppStorageUsageMb() } returns 0
        every { remoteConfigProvider.isSaleDetailEnabled() } returnsMany listOf(false, true)
        every { remoteConfigProvider.getSaleDetailEnabledOverride() } returns false
        coEvery { remoteConfigProvider.setSaleDetailEnabledOverride(null) } returns Unit
        val viewModel = buildViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.onAction(DeveloperModeAction.OnResetSaleDetailOverride)
        dispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { remoteConfigProvider.setSaleDetailEnabledOverride(null) }
        assertEquals(true, viewModel.state.value.saleDetailEnabled)
        assertEquals(false, viewModel.state.value.saleDetailOverrideActive)
    }
}
