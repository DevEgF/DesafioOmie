package com.omie.desafio.navigation

import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable
    data object Home : Route

    @Serializable
    data object NewSale : Route

    @Serializable
    data object ProductList : Route

    @Serializable
    data class ProductForm(val productId: Long = 0L) : Route

    @Serializable
    data class SaleDetail(val saleId: Long) : Route

    @Serializable
    data object DeveloperMode : Route
}
