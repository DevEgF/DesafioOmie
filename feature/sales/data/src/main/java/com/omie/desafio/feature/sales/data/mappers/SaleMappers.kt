package com.omie.desafio.feature.sales.data.mappers

import com.omie.desafio.core.database.entity.SaleItemEntity
import com.omie.desafio.core.database.entity.SaleWithItems
import com.omie.desafio.feature.sales.domain.model.Sale
import com.omie.desafio.feature.sales.domain.model.SaleItem

fun SaleWithItems.toDomain() = Sale(
    id = sale.id,
    clientName = sale.clientName,
    items = items.map { it.toDomain() },
    createdAt = sale.createdAt,
)

fun SaleItemEntity.toDomain() = SaleItem(
    productId = productId,
    productName = productName,
    productDescription = productDescription,
    quantity = quantity,
    unitPriceCents = unitPriceCents,
)
