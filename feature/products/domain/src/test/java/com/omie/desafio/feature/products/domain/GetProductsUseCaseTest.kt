package com.omie.desafio.feature.products.domain

import com.omie.desafio.feature.products.domain.model.Product
import com.omie.desafio.feature.products.domain.repository.ProductRepository
import com.omie.desafio.feature.products.domain.usecase.GetProductsUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetProductsUseCaseTest {
    private val repository = mockk<ProductRepository>()
    private val useCase = GetProductsUseCase(repository)

    @Test
    fun `given products emitted by the repository, when invoked, then returns the same flow`() = runTest {
        // Given
        val products = listOf(
            Product(
                id = 1,
                name = "Caneta",
                description = "Caneta azul",
                unitPriceCents = 500
            )
        )
        coEvery { repository.observeProducts() } returns flowOf(products)

        // When
        val result = useCase().first()

        // Then
        assertEquals(products, result)
    }
}
