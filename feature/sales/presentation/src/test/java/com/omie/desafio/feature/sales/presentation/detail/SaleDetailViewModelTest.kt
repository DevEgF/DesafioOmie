package com.omie.desafio.feature.sales.presentation.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.omie.desafio.feature.sales.domain.usecase.GetSaleUseCase
import com.omie.desafio.feature.sales.domain.model.Sale
import com.omie.desafio.feature.sales.domain.model.SaleItem
import com.omie.desafio.feature.sales.domain.repository.SaleRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SaleDetailViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val repository = mockk<SaleRepository>()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(saleId: Long) = SaleDetailViewModel(
        getSale = GetSaleUseCase(repository),
        savedStateHandle = SavedStateHandle(mapOf("saleId" to saleId)),
    )

    @Test
    fun `given a sale exists for the id in SavedStateHandle, when state is observed, then it exposes the sale`() = runTest {
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
        val viewModel = buildViewModel(saleId = 7)
        dispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertEquals(sale, state.sale)
        assertEquals(false, state.isLoading)
    }

    @Test
    fun `given no sale exists for the id, when state is observed, then sale is null and loading is false`() = runTest {
        // Given
        coEvery { repository.observeSale(99) } returns flowOf(null)

        // When
        val viewModel = buildViewModel(saleId = 99)
        dispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertNull(state.sale)
        assertEquals(false, state.isLoading)
    }

    @Test
    fun `given the back action, when dispatched, then emits NavigateBack event`() = runTest {
        // Given
        coEvery { repository.observeSale(1) } returns flowOf(null)
        val viewModel = buildViewModel(saleId = 1)

        // When / Then
        viewModel.events.test {
            viewModel.onAction(SaleDetailAction.OnBackClick)
            assertEquals(SaleDetailEvent.NavigateBack, awaitItem())
        }
    }
}
