package com.omie.desafio.feature.sales.domain.repository

import com.omie.desafio.core.domain.DataError
import com.omie.desafio.core.domain.Result
import com.omie.desafio.feature.sales.domain.model.Sale
import com.omie.desafio.feature.sales.domain.model.SaleItem
import kotlinx.coroutines.flow.Flow

interface SaleRepository {
    fun observeSales(): Flow<List<Sale>>
    fun observeSale(saleId: Long): Flow<Sale?>
    fun observeTotalValueCents(): Flow<Long>
    suspend fun createSale(clientName: String, items: List<SaleItem>): Result<Long, DataError.Local>
}
