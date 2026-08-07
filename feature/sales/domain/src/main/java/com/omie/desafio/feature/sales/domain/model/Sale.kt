package com.omie.desafio.feature.sales.domain.model

data class Sale(
    val id: Long,
    val clientName: String,
    val items: List<SaleItem>,
    val createdAt: Long,
) {
    val totalQuantity: Int get() = items.sumOf { it.quantity }
    val totalValueCents: Long get() = items.sumOf { it.totalValueCents }
}
