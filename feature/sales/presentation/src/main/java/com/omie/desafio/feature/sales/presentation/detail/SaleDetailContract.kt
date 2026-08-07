package com.omie.desafio.feature.sales.presentation.detail

import com.omie.desafio.feature.sales.domain.model.Sale

data class SaleDetailState(
    val sale: Sale? = null,
    val isLoading: Boolean = true,
)

sealed interface SaleDetailAction {
    data object OnBackClick : SaleDetailAction
}

sealed interface SaleDetailEvent {
    data object NavigateBack : SaleDetailEvent
}
