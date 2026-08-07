package com.omie.desafio.feature.devtools.presentation

data class DeveloperModeState(
    val crashCount: Int = 0,
    val memoryUsageMb: Long = 0,
    val storageUsageMb: Long = 0,
    val saleDetailEnabled: Boolean = true,
    val saleDetailOverrideActive: Boolean = false,
    val isLoading: Boolean = true,
)

sealed interface DeveloperModeAction {
    data object OnBackClick : DeveloperModeAction
    data class OnSaleDetailToggle(val enabled: Boolean) : DeveloperModeAction
    data object OnResetSaleDetailOverride : DeveloperModeAction
}

sealed interface DeveloperModeEvent {
    data object NavigateBack : DeveloperModeEvent
}
