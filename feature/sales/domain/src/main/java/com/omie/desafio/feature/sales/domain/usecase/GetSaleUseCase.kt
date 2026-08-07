package com.omie.desafio.feature.sales.domain.usecase

import com.omie.desafio.feature.sales.domain.model.Sale
import com.omie.desafio.feature.sales.domain.repository.SaleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSaleUseCase @Inject constructor(
    private val repository: SaleRepository,
) {
    operator fun invoke(saleId: Long): Flow<Sale?> = repository.observeSale(saleId)
}
