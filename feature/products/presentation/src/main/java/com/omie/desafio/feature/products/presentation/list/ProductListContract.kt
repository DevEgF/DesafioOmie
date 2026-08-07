package com.omie.desafio.feature.products.presentation.list

import com.omie.desafio.feature.products.domain.model.Product

data class ProductListState(
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = true,
)

sealed interface ProductListAction {
    data object OnAddProductClick : ProductListAction
    data class OnEditProductClick(val product: Product) : ProductListAction
    data class OnDeleteProduct(val product: Product) : ProductListAction
}

sealed interface ProductListEvent {
    data object NavigateToAddProduct : ProductListEvent
    data class NavigateToEditProduct(val productId: Long) : ProductListEvent
}
