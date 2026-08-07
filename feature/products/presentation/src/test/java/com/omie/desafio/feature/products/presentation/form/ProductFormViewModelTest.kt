package com.omie.desafio.feature.products.presentation.form

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.omie.desafio.core.domain.AnalyticsTracker
import com.omie.desafio.core.domain.Result
import com.omie.desafio.core.presentation.UiText
import com.omie.desafio.feature.products.domain.model.Product
import com.omie.desafio.feature.products.domain.repository.ProductRepository
import com.omie.desafio.feature.products.domain.usecase.UpsertProductUseCase
import com.omie.desafio.feature.products.presentation.R
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProductFormViewModelTest {
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

    private fun buildViewModel(productId: Long = 0L) = ProductFormViewModel(
        savedStateHandle = SavedStateHandle(mapOf("productId" to productId)),
        repository = repository,
        upsertProduct = UpsertProductUseCase(repository),
        analyticsTracker = analyticsTracker,
    )

    @Test
    fun `given an existing product id, when created, then loads the product into state`() = runTest {
        // Given
        val product = Product(id = 5, name = "Caneta", description = "Caneta azul", unitPriceCents = 500)
        coEvery { repository.getById(5) } returns product

        // When
        val viewModel = buildViewModel(productId = 5)
        dispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertEquals("Caneta", state.name)
        assertEquals("Caneta azul", state.description)
        assertEquals(500L, state.unitPriceCents)
    }

    @Test
    fun `given valid input, when saving, then emits ProductSaved event`() = runTest {
        // Given
        val viewModel = buildViewModel()
        coEvery { repository.upsert(any()) } returns Result.Success(1L)

        // When / Then
        viewModel.onAction(ProductFormAction.OnNameChange("Caneta"))
        viewModel.onAction(ProductFormAction.OnDescriptionChange("Caneta azul"))
        viewModel.onAction(ProductFormAction.OnUnitPriceChange("500"))

        viewModel.events.test {
            viewModel.onAction(ProductFormAction.OnSaveClick)
            dispatcher.scheduler.advanceUntilIdle()
            assertEquals(ProductFormEvent.ProductSaved, awaitItem())
        }
    }

    @Test
    fun `given invalid input, when saving, then sets an error message`() = runTest {
        // Given
        val viewModel = buildViewModel()

        // When
        viewModel.onAction(ProductFormAction.OnSaveClick)
        dispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(UiText.StringResource(R.string.product_error_blank_name), viewModel.state.value.errorMessage)
    }

    @Test
    fun `given a new product with id zero, when saved successfully, then logs product_created with the new id`() = runTest {
        // Given
        coEvery { repository.upsert(any()) } returns Result.Success(9L)
        val viewModel = buildViewModel(productId = 0L)
        viewModel.onAction(ProductFormAction.OnNameChange("Caneta"))
        viewModel.onAction(ProductFormAction.OnDescriptionChange("Caneta azul"))
        viewModel.onAction(ProductFormAction.OnUnitPriceChange("500"))

        // When
        viewModel.onAction(ProductFormAction.OnSaveClick)
        dispatcher.scheduler.advanceUntilIdle()

        // Then
        verify { analyticsTracker.logEvent("product_created", mapOf("productId" to 9L)) }
    }

    @Test
    fun `given an existing product id, when saved successfully, then logs product_updated with that id`() = runTest {
        // Given
        coEvery { repository.getById(4) } returns null
        coEvery { repository.upsert(any()) } returns Result.Success(4L)
        val viewModel = buildViewModel(productId = 4L)
        viewModel.onAction(ProductFormAction.OnNameChange("Caneta"))
        viewModel.onAction(ProductFormAction.OnDescriptionChange("Caneta azul"))
        viewModel.onAction(ProductFormAction.OnUnitPriceChange("500"))

        // When
        viewModel.onAction(ProductFormAction.OnSaveClick)
        dispatcher.scheduler.advanceUntilIdle()

        // Then
        verify { analyticsTracker.logEvent("product_updated", mapOf("productId" to 4L)) }
    }

    @Test
    fun `given a blank name, when save fails validation, then logs product_save_failed with the failure reason`() = runTest {
        // Given
        val viewModel = buildViewModel(productId = 0L)
        viewModel.onAction(ProductFormAction.OnDescriptionChange("Caneta azul"))
        viewModel.onAction(ProductFormAction.OnUnitPriceChange("500"))

        // When
        viewModel.onAction(ProductFormAction.OnSaveClick)
        dispatcher.scheduler.advanceUntilIdle()

        // Then
        verify { analyticsTracker.logEvent("product_save_failed", mapOf("reason" to "BLANK_PRODUCT_NAME")) }
    }
}
