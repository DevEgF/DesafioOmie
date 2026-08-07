package com.omie.desafio.feature.sales.presentation.home

import app.cash.turbine.test
import com.omie.desafio.core.domain.RemoteConfigProvider
import com.omie.desafio.feature.sales.domain.usecase.GetSalesUseCase
import com.omie.desafio.feature.sales.domain.model.Sale
import com.omie.desafio.feature.sales.domain.model.SaleItem
import com.omie.desafio.feature.sales.domain.repository.SaleRepository
import io.mockk.coEvery
import io.mockk.every
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val repository = mockk<SaleRepository>()
    private val remoteConfigProvider = mockk<RemoteConfigProvider>()

    private fun buildViewModel() = HomeViewModel(
        getSales = GetSalesUseCase(repository),
        remoteConfigProvider = remoteConfigProvider,
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `given sales emitted by the repository, when state is observed, then it exposes sales and the total`() = runTest {
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
        val viewModel = buildViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertEquals(listOf(sale), state.sales)
        assertEquals(1000L, state.totalValueCents)
        assertEquals(false, state.isLoading)
    }

    @Test
    fun `given the new sale action, when dispatched, then emits NavigateToNewSale event`() = runTest {
        // Given
        coEvery { repository.observeSales() } returns flowOf(emptyList())
        val viewModel = buildViewModel()

        // When / Then
        viewModel.events.test {
            viewModel.onAction(HomeAction.OnNewSaleClick)
            assertEquals(HomeEvent.NavigateToNewSale, awaitItem())
        }
    }

    @Test
    fun `given the manage products action, when dispatched, then emits NavigateToProducts event`() = runTest {
        // Given
        coEvery { repository.observeSales() } returns flowOf(emptyList())
        val viewModel = buildViewModel()

        // When / Then
        viewModel.events.test {
            viewModel.onAction(HomeAction.OnManageProductsClick)
            assertEquals(HomeEvent.NavigateToProducts, awaitItem())
        }
    }

    @Test
    fun `given the sale detail flag is enabled, when a sale is clicked, then emits NavigateToSaleDetail event`() = runTest {
        // Given
        coEvery { repository.observeSales() } returns flowOf(emptyList())
        every { remoteConfigProvider.isSaleDetailEnabled() } returns true
        val viewModel = buildViewModel()

        // When / Then
        viewModel.events.test {
            viewModel.onAction(HomeAction.OnSaleClick(saleId = 5))
            assertEquals(HomeEvent.NavigateToSaleDetail(5), awaitItem())
        }
    }

    @Test
    fun `given the sale detail flag is disabled, when a sale is clicked, then no event is emitted`() = runTest {
        // Given
        coEvery { repository.observeSales() } returns flowOf(emptyList())
        every { remoteConfigProvider.isSaleDetailEnabled() } returns false
        val viewModel = buildViewModel()

        // When / Then
        viewModel.events.test {
            viewModel.onAction(HomeAction.OnSaleClick(saleId = 5))
            expectNoEvents()
        }
    }

    @Test
    fun `given the developer mode action, when dispatched, then emits NavigateToDeveloperMode event`() = runTest {
        // Given
        coEvery { repository.observeSales() } returns flowOf(emptyList())
        val viewModel = buildViewModel()

        // When / Then
        viewModel.events.test {
            viewModel.onAction(HomeAction.OnDeveloperModeClick)
            assertEquals(HomeEvent.NavigateToDeveloperMode, awaitItem())
        }
    }
}
