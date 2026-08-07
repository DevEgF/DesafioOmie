package com.omie.desafio.feature.sales.domain

import com.omie.desafio.feature.sales.domain.model.Sale
import com.omie.desafio.feature.sales.domain.model.SaleItem
import com.omie.desafio.feature.sales.domain.repository.SaleRepository
import com.omie.desafio.feature.sales.domain.usecase.GetSaleUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class GetSaleUseCaseTest {
    private val repository = mockk<SaleRepository>()
    private val useCase = GetSaleUseCase(repository)

    @Test
    fun `given a sale emitted by the repository for an id, when invoked, then returns that sale`() = runTest {
        // Given
        val sale = Sale(
            id = 7,
            clientName = "Maria",
            items = listOf(
                SaleItem(
                    productId = 1,
                    productName = "Caneta",
                    productDescription = "Caneta azul",
                    quantity = 2,
                    unitPriceCents = 500,
                ),
            ),
            createdAt = 0L,
        )
        coEvery { repository.observeSale(7) } returns flowOf(sale)

        // When
        val result = useCase(7).first()

        // Then
        assertEquals(sale, result)
    }

    @Test
    fun `given no sale exists for an id, when invoked, then returns null`() = runTest {
        // Given
        coEvery { repository.observeSale(99) } returns flowOf(null)

        // When
        val result = useCase(99).first()

        // Then
        assertNull(result)
    }
}
