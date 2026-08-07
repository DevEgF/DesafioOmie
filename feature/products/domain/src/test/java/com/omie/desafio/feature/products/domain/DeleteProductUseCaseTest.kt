package com.omie.desafio.feature.products.domain

import com.omie.desafio.core.domain.DataError
import com.omie.desafio.core.domain.Result
import com.omie.desafio.feature.products.domain.model.Product
import com.omie.desafio.feature.products.domain.repository.ProductRepository
import com.omie.desafio.feature.products.domain.usecase.DeleteProductUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DeleteProductUseCaseTest {
    private val repository = mockk<ProductRepository>()
    private val useCase = DeleteProductUseCase(repository)

    @Test
    fun `given a product, when deleting, then delegates to repository`() = runTest {
        // Given
        val product =
            Product(id = 1, name = "Caneta", description = "Caneta azul", unitPriceCents = 500)
        coEvery { repository.delete(product) } returns Result.Success(Unit)

        // When
        val result = useCase(product)

        // Then
        assertEquals(Result.Success(Unit), result)
    }

    @Test
    fun `given a repository failure, when deleting, then returns the failure unchanged`() = runTest {
        // Given
        val product =
            Product(id = 1, name = "Caneta", description = "Caneta azul", unitPriceCents = 500)
        coEvery { repository.delete(product) } returns Result.Failure(DataError.Local.UNKNOWN)

        // When
        val result = useCase(product)

        // Then
        assertEquals(Result.Failure(DataError.Local.UNKNOWN), result)
    }
}
