package com.omie.desafio.feature.sales.domain

import com.omie.desafio.feature.sales.domain.model.Sale
import com.omie.desafio.feature.sales.domain.model.SaleItem
import com.omie.desafio.feature.sales.domain.repository.SaleRepository
import com.omie.desafio.feature.sales.domain.usecase.GetSalesUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetSalesUseCaseTest {
    private val repository = mockk<SaleRepository>()
    private val useCase = GetSalesUseCase(repository)

    @Test
    fun `given sales emitted by the repository, when invoked, then returns the same flow`() = runTest {
        // Given
        val sale = Sale(
            id = 1,
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
        coEvery { repository.observeSales() } returns flowOf(listOf(sale))

        // When
        val result = useCase().first()

        // Then
        assertEquals(listOf(sale), result)
        assertEquals(2, result[0].totalQuantity)
        assertEquals(1000L, result[0].totalValueCents)
    }
}
