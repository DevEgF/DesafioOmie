package com.omie.desafio.feature.products.domain.usecase

import com.omie.desafio.core.domain.DataError
import com.omie.desafio.core.domain.Result
import com.omie.desafio.feature.products.domain.model.Product
import com.omie.desafio.feature.products.domain.repository.ProductRepository
import javax.inject.Inject

class UpsertProductUseCase @Inject constructor(
    private val repository: ProductRepository,
) {
    suspend operator fun invoke(product: Product): Result<Long, DataError> {
        val validationError = when {
            product.name.isBlank() -> DataError.Validation.BLANK_PRODUCT_NAME
            product.description.isBlank() -> DataError.Validation.BLANK_PRODUCT_DESCRIPTION
            product.unitPriceCents <= 0 -> DataError.Validation.INVALID_UNIT_PRICE
            else -> null
        }
        return validationError?.let { Result.Failure(it) } ?: repository.upsert(product)
    }
}
