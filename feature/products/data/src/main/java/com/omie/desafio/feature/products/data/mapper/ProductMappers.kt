package com.omie.desafio.feature.products.data.mapper

import com.omie.desafio.core.database.entity.ProductEntity
import com.omie.desafio.feature.products.domain.model.Product

fun ProductEntity.toDomain() = Product(id = id, name = name, description = description, unitPriceCents = unitPriceCents)

fun Product.toEntity(createdAt: Long) = ProductEntity(
    id = id,
    name = name,
    description = description,
    unitPriceCents = unitPriceCents,
    createdAt = createdAt,
)
