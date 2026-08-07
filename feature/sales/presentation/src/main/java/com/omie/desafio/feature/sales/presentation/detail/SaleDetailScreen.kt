package com.omie.desafio.feature.sales.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.omie.desafio.core.presentation.ObserveAsEvents
import com.omie.desafio.core.presentation.centsToBrl
import com.omie.desafio.feature.sales.domain.model.Sale
import com.omie.desafio.feature.sales.domain.model.SaleItem
import com.omie.desafio.feature.sales.presentation.R

@Composable
fun SaleDetailScreenRoot(
    onBack: () -> Unit,
    viewModel: SaleDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            SaleDetailEvent.NavigateBack -> onBack()
        }
    }

    SaleDetailScreen(state = state, onAction = viewModel::onAction)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaleDetailScreen(state: SaleDetailState, onAction: (SaleDetailAction) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.sale_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = { onAction(SaleDetailAction.OnBackClick) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        val sale = state.sale
        when {
            state.isLoading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

            sale == null -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text(stringResource(R.string.sale_detail_not_found))
            }

            else -> SaleDetailContent(sale = sale, modifier = Modifier.padding(padding))
        }
    }
}

@Composable
private fun SaleDetailContent(sale: Sale, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = stringResource(R.string.sale_detail_client_label) + ": " + sale.clientName,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                stringResource(R.string.sale_detail_column_name),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            Text(
                stringResource(R.string.sale_detail_column_quantity),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            Text(
                stringResource(R.string.sale_detail_column_unit_price),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            Text(
                stringResource(R.string.sale_detail_column_total),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
        }
        HorizontalDivider()

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(sale.items, key = { it.productId }) { item ->
                SaleDetailItemRow(item)
                HorizontalDivider()
            }
        }

        Text(
            stringResource(
                R.string.sale_detail_totals,
                sale.totalQuantity,
                sale.totalValueCents.centsToBrl(),
            ),
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}

@Composable
private fun SaleDetailItemRow(item: SaleItem) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Start,
    ) {
        Text(item.productDescription, modifier = Modifier.weight(1f))
        Text(item.quantity.toString(), modifier = Modifier.weight(1f))
        Text(item.unitPriceCents.centsToBrl(), modifier = Modifier.weight(1f))
        Text(item.totalValueCents.centsToBrl(), modifier = Modifier.weight(1f))
    }
}
