package com.omie.desafio.feature.sales.presentation.newsale

import com.omie.desafio.core.presentation.UiText
import com.omie.desafio.feature.products.domain.model.Product
import com.omie.desafio.feature.sales.domain.model.SaleItem

data class NewSaleState(
    val clientName: String = "",
    val products: List<Product> = emptyList(),
    val selectedProduct: Product? = null,
    val quantityText: String = "",
    val unitPriceCents: Long = 0,
    val items: List<SaleItem> = emptyList(),
    val errorMessage: UiText? = null,
    val isSaving: Boolean = false,
) {
    val totalQuantity: Int get() = items.sumOf { it.quantity }
    val totalValueCents: Long get() = items.sumOf { it.totalValueCents }
}

sealed interface NewSaleAction {
    data class OnClientNameChange(val value: String) : NewSaleAction
    data class OnProductSelected(val product: Product) : NewSaleAction
    data class OnQuantityChange(val value: String) : NewSaleAction
    data class OnUnitPriceChange(val digitsOnly: String) : NewSaleAction
    data object OnIncludeItemClick : NewSaleAction
    data class OnRemoveItemClick(val item: SaleItem) : NewSaleAction
    data object OnSaveClick : NewSaleAction
    data object OnCancelClick : NewSaleAction
}

sealed interface NewSaleEvent {
    data object SaleSaved : NewSaleEvent
    data object Cancelled : NewSaleEvent
}
