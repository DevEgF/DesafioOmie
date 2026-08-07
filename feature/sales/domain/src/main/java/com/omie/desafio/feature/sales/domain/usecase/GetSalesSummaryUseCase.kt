package com.omie.desafio.feature.sales.domain.usecase

import com.omie.desafio.feature.sales.domain.repository.SaleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSalesSummaryUseCase @Inject constructor(
    private val repository: SaleRepository,
) {
    operator fun invoke(): Flow<Long> = repository.observeTotalValueCents()
}
