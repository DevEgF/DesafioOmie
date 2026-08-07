package com.omie.desafio.feature.sales.presentation.newsale

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.omie.desafio.core.designsystem.CurrencyVisualTransformation
import com.omie.desafio.core.presentation.ObserveAsEvents
import com.omie.desafio.core.presentation.centsToBrl
import com.omie.desafio.feature.sales.domain.model.SaleItem
import com.omie.desafio.feature.sales.presentation.R

@Composable
fun NewSaleScreenRoot(
    onSaleSaved: () -> Unit,
    onCancelled: () -> Unit,
    viewModel: NewSaleViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            NewSaleEvent.SaleSaved -> onSaleSaved()
            NewSaleEvent.Cancelled -> onCancelled()
        }
    }

    NewSaleScreen(state = state, onAction = viewModel::onAction)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewSaleScreen(state: NewSaleState, onAction: (NewSaleAction) -> Unit) {
    var dropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.new_sale_title)) }) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = state.clientName,
                onValueChange = { onAction(NewSaleAction.OnClientNameChange(it)) },
                label = { Text(stringResource(R.string.new_sale_client_label)) },
                modifier = Modifier.fillMaxWidth(),
            )

            Text(
                stringResource(R.string.new_sale_product_section),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
            )

            ExposedDropdownMenuBox(expanded = dropdownExpanded, onExpandedChange = { dropdownExpanded = it }) {
                OutlinedTextField(
                    value = state.selectedProduct?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.new_sale_product_name_label)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                )
                ExposedDropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false },
                ) {
                    state.products.forEach { product ->
                        DropdownMenuItem(
                            text = { Text(product.name) },
                            onClick = {
                                onAction(NewSaleAction.OnProductSelected(product))
                                dropdownExpanded = false
                            },
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = state.quantityText,
                    onValueChange = { onAction(NewSaleAction.OnQuantityChange(it)) },
                    label = { Text(stringResource(R.string.new_sale_quantity_label)) },
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = state.unitPriceCents.toString(),
                    onValueChange = { onAction(NewSaleAction.OnUnitPriceChange(it.filter(Char::isDigit))) },
                    visualTransformation = CurrencyVisualTransformation(),
                    label = { Text(stringResource(R.string.new_sale_unit_price_label)) },
                    modifier = Modifier.weight(1f),
                )
            }

            state.errorMessage?.let { Text(it.asString(), color = MaterialTheme.colorScheme.error) }

            OutlinedButton(
                onClick = { onAction(NewSaleAction.OnIncludeItemClick) },
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Text(stringResource(R.string.new_sale_include))
            }

            Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                Text(
                    stringResource(R.string.new_sale_column_name),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    stringResource(R.string.new_sale_column_quantity),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    stringResource(R.string.new_sale_column_unit_price),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    stringResource(R.string.new_sale_column_total),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
            }
            HorizontalDivider()

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(state.items, key = { it.productId }) { item ->
                    SaleItemRow(item = item, onRemove = { onAction(NewSaleAction.OnRemoveItemClick(item)) })
                    HorizontalDivider()
                }
            }

            Text(
                stringResource(
                    R.string.new_sale_totals,
                    state.totalQuantity,
                    state.totalValueCents.centsToBrl(),
                ),
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(onClick = { onAction(NewSaleAction.OnCancelClick) }, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.new_sale_cancel))
                }
                Button(
                    onClick = { onAction(NewSaleAction.OnSaveClick) },
                    enabled = !state.isSaving,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.new_sale_save))
                }
            }
        }
    }
}

@Composable
private fun SaleItemRow(item: SaleItem, onRemove: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(item.productDescription, modifier = Modifier.weight(1f))
        Text(item.quantity.toString(), modifier = Modifier.weight(1f))
        Text(item.unitPriceCents.centsToBrl(), modifier = Modifier.weight(1f))
        Text(item.totalValueCents.centsToBrl(), modifier = Modifier.weight(1f))
        OutlinedButton(onClick = onRemove) { Text(stringResource(R.string.new_sale_remove_item)) }
    }
}
