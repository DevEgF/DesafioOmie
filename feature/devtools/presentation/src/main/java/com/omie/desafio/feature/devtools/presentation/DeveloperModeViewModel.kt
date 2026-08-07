package com.omie.desafio.feature.devtools.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omie.desafio.core.domain.CrashCounter
import com.omie.desafio.core.domain.DeviceMetricsProvider
import com.omie.desafio.core.domain.RemoteConfigProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeveloperModeViewModel @Inject constructor(
    private val crashCounter: CrashCounter,
    private val deviceMetricsProvider: DeviceMetricsProvider,
    private val remoteConfigProvider: RemoteConfigProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(DeveloperModeState())
    val state: StateFlow<DeveloperModeState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<DeveloperModeEvent>()
    val events: SharedFlow<DeveloperModeEvent> = _events

    init {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    crashCount = crashCounter.getCrashCount(),
                    memoryUsageMb = deviceMetricsProvider.getAppMemoryUsageMb(),
                    storageUsageMb = deviceMetricsProvider.getAppStorageUsageMb(),
                    saleDetailEnabled = remoteConfigProvider.isSaleDetailEnabled(),
                    saleDetailOverrideActive = remoteConfigProvider.getSaleDetailEnabledOverride() != null,
                    isLoading = false,
                )
            }
        }
    }

    fun onAction(action: DeveloperModeAction) {
        when (action) {
            DeveloperModeAction.OnBackClick -> emitEvent(DeveloperModeEvent.NavigateBack)
            is DeveloperModeAction.OnSaleDetailToggle -> toggleSaleDetail(action.enabled)
            DeveloperModeAction.OnResetSaleDetailOverride -> resetSaleDetailOverride()
        }
    }

    private fun toggleSaleDetail(enabled: Boolean) {
        viewModelScope.launch {
            remoteConfigProvider.setSaleDetailEnabledOverride(enabled)
            _state.update { it.copy(saleDetailEnabled = enabled, saleDetailOverrideActive = true) }
        }
    }

    private fun resetSaleDetailOverride() {
        viewModelScope.launch {
            remoteConfigProvider.setSaleDetailEnabledOverride(null)
            _state.update {
                it.copy(
                    saleDetailEnabled = remoteConfigProvider.isSaleDetailEnabled(),
                    saleDetailOverrideActive = false,
                )
            }
        }
    }

    private fun emitEvent(event: DeveloperModeEvent) {
        viewModelScope.launch { _events.emit(event) }
    }
}
