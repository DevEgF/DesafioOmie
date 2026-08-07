package com.omie.desafio.feature.sales.domain.model

data class SaleItem(
    val productId: Long,
    val productName: String,
    val productDescription: String,
    val quantity: Int,
    val unitPriceCents: Long,
) {
    val totalValueCents: Long get() = quantity * unitPriceCents
}
