package com.omie.desafio.feature.products.presentation.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omie.desafio.core.domain.AnalyticsTracker
import com.omie.desafio.core.domain.DataError
import com.omie.desafio.core.domain.Result
import com.omie.desafio.core.presentation.UiText
import com.omie.desafio.feature.products.domain.model.Product
import com.omie.desafio.feature.products.domain.repository.ProductRepository
import com.omie.desafio.feature.products.domain.usecase.UpsertProductUseCase
import com.omie.desafio.feature.products.presentation.R
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
class ProductFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ProductRepository,
    private val upsertProduct: UpsertProductUseCase,
    private val analyticsTracker: AnalyticsTracker,
) : ViewModel() {

    private val _state = MutableStateFlow(ProductFormState())
    val state: StateFlow<ProductFormState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<ProductFormEvent>()
    val events: SharedFlow<ProductFormEvent> = _events

    private val initialProductId: Long = savedStateHandle.get<Long>("productId") ?: 0L

    init {
        if (initialProductId != 0L) {
            viewModelScope.launch {
                repository.getById(initialProductId)?.let { product ->
                    _state.update {
                        it.copy(
                            productId = product.id,
                            name = product.name,
                            description = product.description,
                            unitPriceCents = product.unitPriceCents,
                        )
                    }
                }
            }
        }
    }

    fun onAction(action: ProductFormAction) {
        when (action) {
            is ProductFormAction.OnNameChange -> _state.update { it.copy(name = action.value, errorMessage = null) }
            is ProductFormAction.OnDescriptionChange ->
                _state.update { it.copy(description = action.value, errorMessage = null) }
            is ProductFormAction.OnUnitPriceChange ->
                _state.update {
                    it.copy(unitPriceCents = action.digitsOnly.toLongOrNull() ?: 0L, errorMessage = null)
                }
            ProductFormAction.OnSaveClick -> save()
        }
    }

    private fun save() {
        val current = _state.value

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val result = upsertProduct(
                Product(
                    id = initialProductId,
                    name = current.name,
                    description = current.description,
                    unitPriceCents = current.unitPriceCents,
                ),
            )
            _state.update { it.copy(isSaving = false) }
            when (result) {
                is Result.Success -> {
                    analyticsTracker.logEvent(
                        name = if (initialProductId == 0L) "product_created" else "product_updated",
                        params = mapOf("productId" to result.data),
                    )
                    _events.emit(ProductFormEvent.ProductSaved)
                }
                is Result.Failure -> {
                    analyticsTracker.logEvent(
                        name = "product_save_failed",
                        params = mapOf("reason" to (result.error as Enum<*>).name),
                    )
                    _state.update { it.copy(errorMessage = result.error.toUserMessage()) }
                }
            }
        }
    }

    private fun DataError.toUserMessage(): UiText = when (this) {
        DataError.Validation.BLANK_PRODUCT_NAME -> UiText.StringResource(R.string.product_error_blank_name)
        DataError.Validation.BLANK_PRODUCT_DESCRIPTION -> UiText.StringResource(R.string.product_error_blank_description)
        DataError.Validation.INVALID_UNIT_PRICE -> UiText.StringResource(R.string.product_error_invalid_unit_price)
        else -> UiText.StringResource(R.string.product_error_generic)
    }
}
