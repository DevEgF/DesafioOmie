package com.omie.desafio.feature.sales.domain.usecase

import com.omie.desafio.core.domain.DataError
import com.omie.desafio.core.domain.Result
import com.omie.desafio.feature.sales.domain.model.SaleItem
import com.omie.desafio.feature.sales.domain.repository.SaleRepository
import javax.inject.Inject

class CreateSaleUseCase @Inject constructor(
    private val repository: SaleRepository,
) {
    suspend operator fun invoke(clientName: String, items: List<SaleItem>): Result<Long, DataError> {
        val validationError = when {
            clientName.isBlank() -> DataError.Validation.BLANK_CLIENT_NAME
            items.isEmpty() -> DataError.Validation.NO_ITEMS
            else -> items.firstNotNullOfOrNull { item ->
                when {
                    item.quantity <= 0 -> DataError.Validation.INVALID_QUANTITY
                    item.unitPriceCents <= 0 -> DataError.Validation.INVALID_UNIT_PRICE
                    else -> null
                }
            }
        }
        return validationError?.let { Result.Failure(it) } ?: repository.createSale(clientName, items)
    }
}
