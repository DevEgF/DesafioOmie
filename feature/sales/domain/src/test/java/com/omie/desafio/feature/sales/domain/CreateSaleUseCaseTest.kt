package com.omie.desafio.feature.sales.domain

import com.omie.desafio.core.domain.DataError
import com.omie.desafio.core.domain.Result
import com.omie.desafio.feature.sales.domain.model.SaleItem
import com.omie.desafio.feature.sales.domain.repository.SaleRepository
import com.omie.desafio.feature.sales.domain.usecase.CreateSaleUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CreateSaleUseCaseTest {
    private val repository = mockk<SaleRepository>()
    private val useCase = CreateSaleUseCase(repository)

    private val item = SaleItem(
        productId = 1,
        productName = "Caneta",
        productDescription = "Caneta azul",
        quantity = 2,
        unitPriceCents = 500,
    )

    @Test
    fun `given blank client name, when creating a sale, then returns blank client name validation failure`() = runTest {
        // Given
        val clientName = "  "

        // When
        val result = useCase(clientName, listOf(item))

        // Then
        assertEquals(Result.Failure(DataError.Validation.BLANK_CLIENT_NAME), result)
    }

    @Test
    fun `given empty items, when creating a sale, then returns no items validation failure`() = runTest {
        // Given
        val items = emptyList<SaleItem>()

        // When
        val result = useCase("Maria", items)

        // Then
        assertEquals(Result.Failure(DataError.Validation.NO_ITEMS), result)
    }

    @Test
    fun `given a valid sale, when creating a sale, then delegates to repository`() = runTest {
        // Given
        coEvery { repository.createSale("Maria", listOf(item)) } returns Result.Success(1L)

        // When
        val result = useCase("Maria", listOf(item))

        // Then
        assertEquals(Result.Success(1L), result)
    }

    @Test
    fun `given non-positive quantity, when creating a sale, then returns invalid quantity validation failure`() = runTest {
        // Given
        val invalidItem = item.copy(quantity = 0)

        // When
        val result = useCase("Maria", listOf(invalidItem))

        // Then
        assertEquals(Result.Failure(DataError.Validation.INVALID_QUANTITY), result)
    }

    @Test
    fun `given non-positive unit price, when creating a sale, then returns invalid unit price validation failure`() = runTest {
        // Given
        val invalidItem = item.copy(unitPriceCents = 0)

        // When
        val result = useCase("Maria", listOf(invalidItem))

        // Then
        assertEquals(Result.Failure(DataError.Validation.INVALID_UNIT_PRICE), result)
    }

    @Test
    fun `given a repository failure, when creating a sale, then returns the failure unchanged`() = runTest {
        // Given
        coEvery { repository.createSale("Maria", listOf(item)) } returns Result.Failure(DataError.Local.UNKNOWN)

        // When
        val result = useCase("Maria", listOf(item))

        // Then
        assertEquals(Result.Failure(DataError.Local.UNKNOWN), result)
    }
}
