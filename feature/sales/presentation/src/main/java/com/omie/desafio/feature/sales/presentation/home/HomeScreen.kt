package com.omie.desafio.feature.sales.presentation.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.omie.desafio.core.presentation.ObserveAsEvents
import com.omie.desafio.core.presentation.centsToBrl
import com.omie.desafio.feature.sales.domain.model.Sale
import com.omie.desafio.feature.sales.presentation.R

@Composable
fun HomeScreenRoot(
    onNavigateToNewSale: () -> Unit,
    onNavigateToProducts: () -> Unit,
    onNavigateToSaleDetail: (Long) -> Unit,
    onNavigateToDeveloperMode: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            HomeEvent.NavigateToNewSale -> onNavigateToNewSale()
            HomeEvent.NavigateToProducts -> onNavigateToProducts()
            is HomeEvent.NavigateToSaleDetail -> onNavigateToSaleDetail(event.saleId)
            HomeEvent.NavigateToDeveloperMode -> onNavigateToDeveloperMode()
        }
    }

    HomeScreen(state = state, onAction = viewModel::onAction)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(state: HomeState, onAction: (HomeAction) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_title)) },
                actions = {
                    IconButton(onClick = { onAction(HomeAction.OnManageProductsClick) }) {
                        Icon(Icons.Default.Inventory2, contentDescription = stringResource(R.string.home_products_action))
                    }
                    IconButton(onClick = { onAction(HomeAction.OnDeveloperModeClick) }) {
                        Icon(Icons.Default.BugReport, contentDescription = stringResource(R.string.home_developer_mode_action))
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Button(onClick = { onAction(HomeAction.OnNewSaleClick) }) {
                Text(stringResource(R.string.home_new_sale))
            }

            Text(
                text = stringResource(R.string.home_total_sales, state.totalValueCents.centsToBrl()),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 32.dp, bottom = 16.dp),
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(state.sales, key = { it.id }) { sale ->
                    SaleRow(sale = sale, onClick = { onAction(HomeAction.OnSaleClick(sale.id)) })
                }
            }
        }
    }
}

@Composable
private fun SaleRow(sale: Sale, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp)) {
        Text("#${sale.id} - ${sale.clientName}", modifier = Modifier.padding(end = 8.dp))
        Text(sale.totalValueCents.centsToBrl())
    }
}
