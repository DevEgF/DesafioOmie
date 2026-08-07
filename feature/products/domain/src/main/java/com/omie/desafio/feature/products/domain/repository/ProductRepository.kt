package com.omie.desafio.feature.products.domain.repository

import com.omie.desafio.core.domain.DataError
import com.omie.desafio.core.domain.Result
import com.omie.desafio.feature.products.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun observeProducts(): Flow<List<Product>>
    suspend fun getById(id: Long): Product?
    suspend fun upsert(product: Product): Result<Long, DataError.Local>
    suspend fun delete(product: Product): Result<Unit, DataError.Local>
}
