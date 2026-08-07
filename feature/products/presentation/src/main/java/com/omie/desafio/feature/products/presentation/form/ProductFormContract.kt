package com.omie.desafio.feature.products.presentation.form

import com.omie.desafio.core.presentation.UiText

data class ProductFormState(
    val productId: Long = 0,
    val name: String = "",
    val description: String = "",
    val unitPriceCents: Long = 0,
    val isSaving: Boolean = false,
    val errorMessage: UiText? = null,
)

sealed interface ProductFormAction {
    data class OnNameChange(val value: String) : ProductFormAction
    data class OnDescriptionChange(val value: String) : ProductFormAction
    data class OnUnitPriceChange(val digitsOnly: String) : ProductFormAction
    data object OnSaveClick : ProductFormAction
}

sealed interface ProductFormEvent {
    data object ProductSaved : ProductFormEvent
}
