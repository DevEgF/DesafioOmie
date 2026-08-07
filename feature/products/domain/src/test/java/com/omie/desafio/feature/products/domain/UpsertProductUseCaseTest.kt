package com.omie.desafio.feature.products.domain

import com.omie.desafio.core.domain.DataError
import com.omie.desafio.core.domain.Result
import com.omie.desafio.feature.products.domain.model.Product
import com.omie.desafio.feature.products.domain.repository.ProductRepository
import com.omie.desafio.feature.products.domain.usecase.UpsertProductUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UpsertProductUseCaseTest {
    private val repository = mockk<ProductRepository>()
    private val useCase = UpsertProductUseCase(repository)

    @Test
    fun `given blank product name, when upserting, then returns blank name validation failure`() = runTest {
        // Given
        val product =
            Product(id = 0, name = "  ", description = "Caneta azul", unitPriceCents = 500)

        // When
        val result = useCase(product)

        // Then
        assertEquals(Result.Failure(DataError.Validation.BLANK_PRODUCT_NAME), result)
    }

    @Test
    fun `given blank product description, when upserting, then returns blank description validation failure`() = runTest {
        // Given
        val product = Product(id = 0, name = "Caneta", description = "  ", unitPriceCents = 500)

        // When
        val result = useCase(product)

        // Then
        assertEquals(Result.Failure(DataError.Validation.BLANK_PRODUCT_DESCRIPTION), result)
    }

    @Test
    fun `given non positive price, when upserting, then returns invalid price validation failure`() = runTest {
        // Given
        val product =
            Product(id = 0, name = "Caneta", description = "Caneta azul", unitPriceCents = 0)

        // When
        val result = useCase(product)

        // Then
        assertEquals(Result.Failure(DataError.Validation.INVALID_UNIT_PRICE), result)
    }

    @Test
    fun `given a valid product, when upserting, then delegates to repository`() = runTest {
        // Given
        val product =
            Product(id = 0, name = "Caneta", description = "Caneta azul", unitPriceCents = 500)
        coEvery { repository.upsert(product) } returns Result.Success(1L)

        // When
        val result = useCase(product)

        // Then
        assertEquals(Result.Success(1L), result)
    }
}
