package com.omie.desafio.feature.sales.presentation.home

import com.omie.desafio.feature.sales.domain.model.Sale

data class HomeState(
    val totalValueCents: Long = 0,
    val sales: List<Sale> = emptyList(),
    val isLoading: Boolean = true,
)

sealed interface HomeAction {
    data object OnNewSaleClick : HomeAction
    data object OnManageProductsClick : HomeAction
    data class OnSaleClick(val saleId: Long) : HomeAction
    data object OnDeveloperModeClick : HomeAction
}

sealed interface HomeEvent {
    data object NavigateToNewSale : HomeEvent
    data object NavigateToProducts : HomeEvent
    data class NavigateToSaleDetail(val saleId: Long) : HomeEvent
    data object NavigateToDeveloperMode : HomeEvent
}
