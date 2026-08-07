package com.omie.desafio.feature.products.presentation.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.omie.desafio.core.presentation.ObserveAsEvents
import com.omie.desafio.core.presentation.centsToBrl
import com.omie.desafio.feature.products.domain.model.Product
import com.omie.desafio.feature.products.presentation.R

@Composable
fun ProductListScreenRoot(
    onNavigateToAddProduct: () -> Unit,
    onNavigateToEditProduct: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: ProductListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            ProductListEvent.NavigateToAddProduct -> onNavigateToAddProduct()
            is ProductListEvent.NavigateToEditProduct -> onNavigateToEditProduct(event.productId)
        }
    }

    ProductListScreen(state = state, onAction = viewModel::onAction, onBack = onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    state: ProductListState,
    onAction: (ProductListAction) -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.products_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.product_back))
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onAction(ProductListAction.OnAddProductClick) }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.product_new))
            }
        },
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            items(state.products, key = { it.id }) { product ->
                ProductRow(
                    product = product,
                    onEdit = { onAction(ProductListAction.OnEditProductClick(product)) },
                    onDelete = { onAction(ProductListAction.OnDeleteProduct(product)) },
                )
            }
        }
    }
}

@Composable
private fun ProductRow(product: Product, onEdit: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.padding(end = 8.dp)) {
            Text(product.name, style = MaterialTheme.typography.bodyLarge)
            Text(product.unitPriceCents.centsToBrl(), style = MaterialTheme.typography.bodyMedium)
        }
        IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.product_edit)) }
        IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.product_delete)) }
    }
}
