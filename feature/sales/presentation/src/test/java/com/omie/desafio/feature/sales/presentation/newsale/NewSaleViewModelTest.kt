package com.omie.desafio.feature.sales.presentation.newsale

import app.cash.turbine.test
import com.omie.desafio.core.domain.AnalyticsTracker
import com.omie.desafio.core.presentation.UiText
import com.omie.desafio.feature.products.domain.usecase.GetProductsUseCase
import com.omie.desafio.feature.products.domain.model.Product
import com.omie.desafio.feature.products.domain.repository.ProductRepository
import com.omie.desafio.feature.sales.domain.usecase.CreateSaleUseCase
import com.omie.desafio.feature.sales.domain.model.SaleItem
import com.omie.desafio.feature.sales.domain.repository.SaleRepository
import com.omie.desafio.feature.sales.presentation.R
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
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NewSaleViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val productRepository = mockk<ProductRepository>()
    private val saleRepository = mockk<SaleRepository>()
    private val analyticsTracker = mockk<AnalyticsTracker>(relaxed = true)
    private val product = Product(id = 1, name = "Caneta", description = "Caneta azul", unitPriceCents = 500)
    private val secondProduct = Product(id = 2, name = "Lápis", description = "Lápis preto", unitPriceCents = 300)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        coEvery { productRepository.observeProducts() } returns flowOf(listOf(product, secondProduct))
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel() = NewSaleViewModel(
        getProducts = GetProductsUseCase(productRepository),
        createSale = CreateSaleUseCase(saleRepository),
        analyticsTracker = analyticsTracker,
    )

    private fun buildViewModelWithOneItem(clientName: String = "Maria"): NewSaleViewModel {
        val viewModel = buildViewModel()
        dispatcher.scheduler.advanceUntilIdle()
        viewModel.onAction(NewSaleAction.OnClientNameChange(clientName))
        viewModel.onAction(NewSaleAction.OnProductSelected(product))
        viewModel.onAction(NewSaleAction.OnQuantityChange("1"))
        viewModel.onAction(NewSaleAction.OnUnitPriceChange("1500"))
        viewModel.onAction(NewSaleAction.OnIncludeItemClick)
        return viewModel
    }

    @Test
    fun `given no product selected, when including an item, then sets an error and adds nothing`() = runTest {
        // Given
        val viewModel = buildViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.onAction(NewSaleAction.OnQuantityChange("2"))
        viewModel.onAction(NewSaleAction.OnUnitPriceChange("500"))
        viewModel.onAction(NewSaleAction.OnIncludeItemClick)

        // Then
        val state = viewModel.state.value
        assertEquals(emptyList<SaleItem>(), state.items)
        assertEquals(UiText.StringResource(R.string.new_sale_error_no_product_selected), state.errorMessage)
    }

    @Test
    fun `given a valid item, when including it, then appends it and clears the input fields`() = runTest {
        // Given
        val viewModel = buildViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.onAction(NewSaleAction.OnProductSelected(product))
        viewModel.onAction(NewSaleAction.OnQuantityChange("2"))
        viewModel.onAction(NewSaleAction.OnUnitPriceChange("500"))
        viewModel.onAction(NewSaleAction.OnIncludeItemClick)

        // Then
        val state = viewModel.state.value
        assertEquals(1, state.items.size)
        assertEquals(500L, state.items[0].unitPriceCents)
        assertEquals(1000L, state.totalValueCents)
        assertNull(state.selectedProduct)
        assertEquals("", state.quantityText)
    }

    @Test
    fun `given two distinct products included, when saving, then persists both items with correct per-item values`() = runTest {
        // Given
        val viewModel = buildViewModel()
        dispatcher.scheduler.advanceUntilIdle()
        var savedItems: List<SaleItem> = emptyList()
        coEvery { saleRepository.createSale("Maria", any()) } answers {
            savedItems = secondArg()
            com.omie.desafio.core.domain.Result.Success(1L)
        }

        // When
        viewModel.onAction(NewSaleAction.OnClientNameChange("Maria"))
        viewModel.onAction(NewSaleAction.OnProductSelected(product))
        viewModel.onAction(NewSaleAction.OnQuantityChange("2"))
        viewModel.onAction(NewSaleAction.OnUnitPriceChange("500"))
        viewModel.onAction(NewSaleAction.OnIncludeItemClick)

        viewModel.onAction(NewSaleAction.OnProductSelected(secondProduct))
        viewModel.onAction(NewSaleAction.OnQuantityChange("3"))
        viewModel.onAction(NewSaleAction.OnUnitPriceChange("300"))
        viewModel.onAction(NewSaleAction.OnIncludeItemClick)

        viewModel.onAction(NewSaleAction.OnSaveClick)
        dispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(2, savedItems.size)

        val firstItem = savedItems.first { it.productId == product.id }
        assertEquals("Caneta azul", firstItem.productDescription)
        assertEquals(2, firstItem.quantity)
        assertEquals(500L, firstItem.unitPriceCents)
        assertEquals(1000L, firstItem.totalValueCents)

        val secondItem = savedItems.first { it.productId == secondProduct.id }
        assertEquals("Lápis preto", secondItem.productDescription)
        assertEquals(3, secondItem.quantity)
        assertEquals(300L, secondItem.unitPriceCents)
        assertEquals(900L, secondItem.totalValueCents)
    }

    @Test
    fun `given a cancel action, when dispatched, then emits Cancelled event`() = runTest {
        // Given
        val viewModel = buildViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        // When / Then
        viewModel.events.test {
            viewModel.onAction(NewSaleAction.OnCancelClick)
            assertEquals(NewSaleEvent.Cancelled, awaitItem())
        }
    }

    @Test
    fun `given a valid product selection, when included, then logs sale_item_included with product id and quantity`() = runTest {
        // Given
        val viewModel = buildViewModel()
        dispatcher.scheduler.advanceUntilIdle()
        viewModel.onAction(NewSaleAction.OnProductSelected(product))
        viewModel.onAction(NewSaleAction.OnQuantityChange("3"))
        val nameSlot = io.mockk.slot<String>()
        val paramsSlot = io.mockk.slot<Map<String, Any?>>()

        // When
        viewModel.onAction(NewSaleAction.OnIncludeItemClick)

        // Then
        verify { analyticsTracker.logEvent(capture(nameSlot), capture(paramsSlot)) }
        assertEquals("sale_item_included", nameSlot.captured)
        assertEquals(product.id, paramsSlot.captured["productId"])
        assertEquals(3, paramsSlot.captured["quantity"])
    }

    @Test
    fun `given a valid order, when saved successfully, then logs sale_created with item count and total`() = runTest {
        // Given
        coEvery { saleRepository.createSale(any(), any()) } returns com.omie.desafio.core.domain.Result.Success(1L)
        val viewModel = buildViewModelWithOneItem()
        val nameSlots = mutableListOf<String>()
        val paramsSlots = mutableListOf<Map<String, Any?>>()

        // When
        viewModel.onAction(NewSaleAction.OnSaveClick)
        dispatcher.scheduler.advanceUntilIdle()

        // Then
        verify { analyticsTracker.logEvent(capture(nameSlots), capture(paramsSlots)) }
        val index = nameSlots.indexOf("sale_created")
        assertEquals(1, paramsSlots[index]["itemCount"])
        assertEquals(1500L, paramsSlots[index]["totalValueCents"])
    }

    @Test
    fun `given the client name is blank, when save fails validation, then logs sale_save_failed with the reason`() = runTest {
        // Given
        val viewModel = buildViewModelWithOneItem(clientName = "")

        // When
        viewModel.onAction(NewSaleAction.OnSaveClick)
        dispatcher.scheduler.advanceUntilIdle()

        // Then
        verify { analyticsTracker.logEvent("sale_save_failed", mapOf("reason" to "BLANK_CLIENT_NAME")) }
    }
}
