package com.omie.desafio.feature.sales.presentation.newsale

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omie.desafio.core.domain.AnalyticsTracker
import com.omie.desafio.core.domain.DataError
import com.omie.desafio.core.domain.Result
import com.omie.desafio.core.presentation.UiText
import com.omie.desafio.feature.products.domain.usecase.GetProductsUseCase
import com.omie.desafio.feature.sales.domain.usecase.CreateSaleUseCase
import com.omie.desafio.feature.sales.domain.model.SaleItem
import com.omie.desafio.feature.sales.presentation.R
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
class NewSaleViewModel @Inject constructor(
    getProducts: GetProductsUseCase,
    private val createSale: CreateSaleUseCase,
    private val analyticsTracker: AnalyticsTracker,
) : ViewModel() {

    private val _state = MutableStateFlow(NewSaleState())
    val state: StateFlow<NewSaleState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<NewSaleEvent>()
    val events: SharedFlow<NewSaleEvent> = _events

    init {
        viewModelScope.launch {
            getProducts().collect { products -> _state.update { it.copy(products = products) } }
        }
    }

    fun onAction(action: NewSaleAction) {
        when (action) {
            is NewSaleAction.OnClientNameChange -> _state.update { it.copy(clientName = action.value, errorMessage = null) }
            is NewSaleAction.OnProductSelected -> _state.update {
                it.copy(
                    selectedProduct = action.product,
                    unitPriceCents = action.product.unitPriceCents,
                )
            }
            is NewSaleAction.OnQuantityChange -> _state.update { it.copy(quantityText = action.value, errorMessage = null) }
            is NewSaleAction.OnUnitPriceChange ->
                _state.update {
                    it.copy(unitPriceCents = action.digitsOnly.toLongOrNull() ?: 0L, errorMessage = null)
                }
            NewSaleAction.OnIncludeItemClick -> includeItem()
            is NewSaleAction.OnRemoveItemClick -> _state.update { it.copy(items = it.items - action.item) }
            NewSaleAction.OnSaveClick -> save()
            NewSaleAction.OnCancelClick -> viewModelScope.launch { _events.emit(NewSaleEvent.Cancelled) }
        }
    }

    private fun includeItem() {
        val current = _state.value
        val product = current.selectedProduct
        val quantity = current.quantityText.toIntOrNull()
        val unitPriceCents = current.unitPriceCents

        val error = when {
            product == null -> UiText.StringResource(R.string.new_sale_error_no_product_selected)
            quantity == null || quantity <= 0 -> UiText.StringResource(R.string.new_sale_error_invalid_quantity)
            unitPriceCents <= 0 -> UiText.StringResource(R.string.new_sale_error_invalid_unit_price)
            else -> null
        }

        if (error != null || product == null || quantity == null) {
            _state.update { it.copy(errorMessage = error) }
            return
        }

        val item = SaleItem(
            productId = product.id,
            productName = product.name,
            productDescription = product.description,
            quantity = quantity,
            unitPriceCents = unitPriceCents,
        )
        val existingIndex = current.items.indexOfFirst { it.productId == product.id }
        val updatedItems = if (existingIndex >= 0) {
            val existing = current.items[existingIndex]
            current.items.toMutableList().apply {
                this[existingIndex] = existing.copy(
                    quantity = existing.quantity + quantity,
                    unitPriceCents = unitPriceCents,
                )
            }
        } else {
            current.items + item
        }
        _state.update {
            it.copy(
                items = updatedItems,
                selectedProduct = null,
                quantityText = "",
                unitPriceCents = 0,
                errorMessage = null,
            )
        }
        analyticsTracker.logEvent(
            name = "sale_item_included",
            params = mapOf("productId" to item.productId, "quantity" to item.quantity),
        )
    }

    private fun save() {
        val current = _state.value
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val result = createSale(current.clientName, current.items)
            _state.update { it.copy(isSaving = false) }
            when (result) {
                is Result.Success -> {
                    analyticsTracker.logEvent(
                        name = "sale_created",
                        params = mapOf(
                            "itemCount" to current.items.size,
                            "totalValueCents" to current.totalValueCents,
                        ),
                    )
                    _events.emit(NewSaleEvent.SaleSaved)
                }
                is Result.Failure -> {
                    analyticsTracker.logEvent(
                        name = "sale_save_failed",
                        params = mapOf("reason" to (result.error as Enum<*>).name),
                    )
                    _state.update { it.copy(errorMessage = result.error.toUserMessage()) }
                }
            }
        }
    }

    private fun DataError.toUserMessage(): UiText = when (this) {
        DataError.Validation.BLANK_CLIENT_NAME -> UiText.StringResource(R.string.new_sale_error_blank_client_name)
        DataError.Validation.NO_ITEMS -> UiText.StringResource(R.string.new_sale_error_no_items)
        DataError.Validation.INVALID_QUANTITY -> UiText.StringResource(R.string.new_sale_error_invalid_quantity)
        DataError.Validation.INVALID_UNIT_PRICE -> UiText.StringResource(R.string.new_sale_error_invalid_unit_price)
        else -> UiText.StringResource(R.string.new_sale_error_generic)
    }
}
