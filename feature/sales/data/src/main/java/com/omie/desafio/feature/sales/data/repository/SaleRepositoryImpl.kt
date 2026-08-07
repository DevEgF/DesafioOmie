package com.omie.desafio.feature.sales.data.repository

import com.omie.desafio.core.database.dao.SaleDao
import com.omie.desafio.core.database.entity.SaleEntity
import com.omie.desafio.core.database.entity.SaleItemEntity
import com.omie.desafio.core.domain.AnalyticsTracker
import com.omie.desafio.core.domain.DataError
import com.omie.desafio.core.domain.Result
import com.omie.desafio.feature.sales.data.mappers.toDomain
import com.omie.desafio.feature.sales.domain.model.Sale
import com.omie.desafio.feature.sales.domain.model.SaleItem
import com.omie.desafio.feature.sales.domain.repository.SaleRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SaleRepositoryImpl @Inject constructor(
    private val dao: SaleDao,
    private val analyticsTracker: AnalyticsTracker,
) : SaleRepository {
    override fun observeSales(): Flow<List<Sale>> =
        dao.observeAllWithItems().map { list -> list.map { it.toDomain() } }

    override fun observeSale(saleId: Long): Flow<Sale?> =
        dao.observeWithItemsById(saleId).map { it?.toDomain() }

    override fun observeTotalValueCents(): Flow<Long> = dao.observeTotalValueCents()

    override suspend fun createSale(clientName: String, items: List<SaleItem>): Result<Long, DataError.Local> = try {
        val sale = SaleEntity(
            clientName = clientName,
            totalQuantity = items.sumOf { it.quantity },
            totalValueCents = items.sumOf { it.totalValueCents },
            createdAt = System.currentTimeMillis(),
        )
        val itemEntities = items.map {
            SaleItemEntity(
                saleId = 0,
                productId = it.productId,
                productName = it.productName,
                productDescription = it.productDescription,
                quantity = it.quantity,
                unitPriceCents = it.unitPriceCents,
                totalValueCents = it.totalValueCents,
            )
        }
        Result.Success(dao.insertSaleWithItems(sale, itemEntities))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        analyticsTracker.recordException(e)
        Result.Failure(DataError.Local.UNKNOWN)
    }
}
