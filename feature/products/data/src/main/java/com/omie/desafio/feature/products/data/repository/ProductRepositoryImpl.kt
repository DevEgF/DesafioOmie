package com.omie.desafio.feature.products.data.repository

import com.omie.desafio.core.database.dao.ProductDao
import com.omie.desafio.core.domain.AnalyticsTracker
import com.omie.desafio.core.domain.DataError
import com.omie.desafio.core.domain.Result
import com.omie.desafio.feature.products.data.mapper.toDomain
import com.omie.desafio.feature.products.data.mapper.toEntity
import com.omie.desafio.feature.products.domain.model.Product
import com.omie.desafio.feature.products.domain.repository.ProductRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val dao: ProductDao,
    private val analyticsTracker: AnalyticsTracker,
) : ProductRepository {
    override fun observeProducts(): Flow<List<Product>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getById(id: Long): Product? = dao.getById(id)?.toDomain()

    override suspend fun upsert(product: Product): Result<Long, DataError.Local> = try {
        val createdAt = if (product.id != 0L) {
            dao.getById(product.id)?.createdAt ?: System.currentTimeMillis()
        } else {
            System.currentTimeMillis()
        }
        Result.Success(dao.upsert(product.toEntity(createdAt)))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        analyticsTracker.recordException(e)
        Result.Failure(DataError.Local.UNKNOWN)
    }

    override suspend fun delete(product: Product): Result<Unit, DataError.Local> = try {
        dao.delete(product.toEntity(createdAt = 0L))
        Result.Success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        analyticsTracker.recordException(e)
        Result.Failure(DataError.Local.UNKNOWN)
    }
}
