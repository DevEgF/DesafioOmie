package com.omie.desafio.feature.products.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omie.desafio.core.domain.AnalyticsTracker
import com.omie.desafio.core.domain.Result
import com.omie.desafio.feature.products.domain.usecase.DeleteProductUseCase
import com.omie.desafio.feature.products.domain.usecase.GetProductsUseCase
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
class ProductListViewModel @Inject constructor(
    private val getProducts: GetProductsUseCase,
    private val deleteProduct: DeleteProductUseCase,
    private val analyticsTracker: AnalyticsTracker,
) : ViewModel() {

    private val _state = MutableStateFlow(ProductListState())
    val state: StateFlow<ProductListState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<ProductListEvent>()
    val events: SharedFlow<ProductListEvent> = _events

    init {
        viewModelScope.launch {
            getProducts().collect { products ->
                _state.update { it.copy(products = products, isLoading = false) }
            }
        }
    }

    fun onAction(action: ProductListAction) {
        when (action) {
            is ProductListAction.OnAddProductClick -> emitEvent(ProductListEvent.NavigateToAddProduct)
            is ProductListAction.OnEditProductClick -> emitEvent(ProductListEvent.NavigateToEditProduct(action.product.id))
            is ProductListAction.OnDeleteProduct -> viewModelScope.launch {
                val result = deleteProduct(action.product)
                if (result is Result.Success) {
                    analyticsTracker.logEvent("product_deleted", mapOf("productId" to action.product.id))
                }
            }
        }
    }

    private fun emitEvent(event: ProductListEvent) {
        viewModelScope.launch { _events.emit(event) }
    }
}
