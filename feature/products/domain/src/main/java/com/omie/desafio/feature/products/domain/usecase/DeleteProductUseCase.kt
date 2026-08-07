package com.omie.desafio.feature.products.domain.usecase

import com.omie.desafio.core.domain.DataError
import com.omie.desafio.core.domain.Result
import com.omie.desafio.feature.products.domain.model.Product
import com.omie.desafio.feature.products.domain.repository.ProductRepository
import javax.inject.Inject

class DeleteProductUseCase @Inject constructor(
    private val repository: ProductRepository,
) {
    suspend operator fun invoke(product: Product): Result<Unit, DataError.Local> = repository.delete(product)
}
