package com.omie.desafio.feature.sales.domain

import com.omie.desafio.feature.sales.domain.repository.SaleRepository
import com.omie.desafio.feature.sales.domain.usecase.GetSalesSummaryUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetSalesSummaryUseCaseTest {
    private val repository = mockk<SaleRepository>()
    private val useCase = GetSalesSummaryUseCase(repository)

    @Test
    fun `given a total emitted by the repository, when invoked, then returns the same flow`() = runTest {
        // Given
        coEvery { repository.observeTotalValueCents() } returns flowOf(1500L)

        // When
        val result = useCase().first()

        // Then
        assertEquals(1500L, result)
    }
}
