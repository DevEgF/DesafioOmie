package com.omie.desafio.feature.products.domain.model

data class Product(
    val id: Long,
    val name: String,
    val description: String,
    val unitPriceCents: Long,
)
