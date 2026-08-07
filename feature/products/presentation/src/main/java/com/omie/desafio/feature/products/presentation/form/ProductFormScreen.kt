package com.omie.desafio.feature.products.presentation.form

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.omie.desafio.core.designsystem.CurrencyVisualTransformation
import com.omie.desafio.core.presentation.ObserveAsEvents
import com.omie.desafio.feature.products.presentation.R

@Composable
fun ProductFormScreenRoot(
    onProductSaved: () -> Unit,
    onCancel: () -> Unit,
    viewModel: ProductFormViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            ProductFormEvent.ProductSaved -> onProductSaved()
        }
    }

    ProductFormScreen(state = state, onAction = viewModel::onAction, onCancel = onCancel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormScreen(state: ProductFormState, onAction: (ProductFormAction) -> Unit, onCancel: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.product_form_title)) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.product_back))
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = state.name,
                onValueChange = { onAction(ProductFormAction.OnNameChange(it)) },
                label = { Text(stringResource(R.string.product_name_label)) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            )
            OutlinedTextField(
                value = state.description,
                onValueChange = { onAction(ProductFormAction.OnDescriptionChange(it)) },
                label = { Text(stringResource(R.string.product_description_label)) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            )
            OutlinedTextField(
                value = state.unitPriceCents.toString(),
                onValueChange = { onAction(ProductFormAction.OnUnitPriceChange(it.filter(Char::isDigit))) },
                visualTransformation = CurrencyVisualTransformation(),
                label = { Text(stringResource(R.string.product_unit_price_label)) },
                modifier = Modifier.padding(bottom = 8.dp),
            )
            state.errorMessage?.let {
                Text(it.asString(), color = MaterialTheme.colorScheme.error)
            }
            Button(
                onClick = { onAction(ProductFormAction.OnSaveClick) },
                enabled = !state.isSaving,
            ) {
                Text(stringResource(R.string.product_save))
            }
        }
    }
}
