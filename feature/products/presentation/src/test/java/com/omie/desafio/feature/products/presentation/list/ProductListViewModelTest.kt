package com.omie.desafio.feature.products.presentation.list

import app.cash.turbine.test
import com.omie.desafio.core.domain.AnalyticsTracker
import com.omie.desafio.core.domain.Result
import com.omie.desafio.feature.products.domain.usecase.DeleteProductUseCase
import com.omie.desafio.feature.products.domain.usecase.GetProductsUseCase
import com.omie.desafio.feature.products.domain.model.Product
import com.omie.desafio.feature.products.domain.repository.ProductRepository
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
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
class ProductListViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val repository = mockk<ProductRepository>()
    private val analyticsTracker = mockk<AnalyticsTracker>(relaxed = true)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel() = ProductListViewModel(
        getProducts = GetProductsUseCase(repository),
        deleteProduct = DeleteProductUseCase(repository),
        analyticsTracker = analyticsTracker,
    )

    @Test
    fun `given products emitted by the repository, when state is observed, then it exposes the loaded products`() = runTest {
        // Given
        val products = listOf(Product(id = 1, name = "Caneta", description = "Caneta azul", unitPriceCents = 500))
        coEvery { repository.observeProducts() } returns flowOf(products)

        // When
        val viewModel = buildViewModel()

        // Then
        viewModel.state.test {
            assertEquals(ProductListState(isLoading = true), awaitItem())
            assertEquals(ProductListState(products = products, isLoading = false), awaitItem())
        }
    }

    @Test
    fun `given the add product action, when dispatched, then emits NavigateToAddProduct event`() = runTest {
        // Given
        coEvery { repository.observeProducts() } returns flowOf(emptyList())
        val viewModel = buildViewModel()

        // When / Then
        viewModel.events.test {
            viewModel.onAction(ProductListAction.OnAddProductClick)
            assertEquals(ProductListEvent.NavigateToAddProduct, awaitItem())
        }
    }

    @Test
    fun `given a product delete succeeds, when handled, then logs product_deleted with the product id`() = runTest {
        // Given
        coEvery { repository.observeProducts() } returns flowOf(emptyList())
        val product = Product(id = 7, name = "Caneta", description = "Caneta azul", unitPriceCents = 500)
        coEvery { repository.delete(product) } returns Result.Success(Unit)
        val viewModel = buildViewModel()

        // When
        viewModel.onAction(ProductListAction.OnDeleteProduct(product))
        dispatcher.scheduler.advanceUntilIdle()

        // Then
        verify { analyticsTracker.logEvent("product_deleted", mapOf("productId" to 7L)) }
    }
}
