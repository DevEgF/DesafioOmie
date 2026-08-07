package com.omie.desafio.feature.devtools.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.omie.desafio.core.presentation.ObserveAsEvents
import kotlin.math.min

// Reference scales used only to size the bars — the app doesn't define hard
// budgets for these, so these are reasonable defaults for a small demo app.
private const val CRASH_COUNT_SCALE = 10f
private const val MEMORY_SCALE_MB = 512f
private const val STORAGE_SCALE_MB = 200f

@Composable
fun DeveloperModeScreenRoot(
    onBack: () -> Unit,
    viewModel: DeveloperModeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            DeveloperModeEvent.NavigateBack -> onBack()
        }
    }

    DeveloperModeScreen(state = state, onAction = viewModel::onAction)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperModeScreen(state: DeveloperModeState, onAction: (DeveloperModeAction) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.developer_mode_title)) },
                navigationIcon = {
                    IconButton(onClick = { onAction(DeveloperModeAction.OnBackClick) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            val crashColor = if (state.crashCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            MetricBar(
                label = stringResource(R.string.developer_mode_crash_count, state.crashCount),
                progress = min(state.crashCount / CRASH_COUNT_SCALE, 1f),
                color = crashColor,
            )
            MetricBar(
                label = stringResource(R.string.developer_mode_memory_usage, state.memoryUsageMb),
                progress = min(state.memoryUsageMb / MEMORY_SCALE_MB, 1f),
                color = MaterialTheme.colorScheme.primary,
            )
            MetricBar(
                label = stringResource(R.string.developer_mode_storage_usage, state.storageUsageMb),
                progress = min(state.storageUsageMb / STORAGE_SCALE_MB, 1f),
                color = MaterialTheme.colorScheme.tertiary,
            )

            HorizontalDivider()

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.developer_mode_sale_detail_toggle),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Switch(
                        checked = state.saleDetailEnabled,
                        onCheckedChange = { onAction(DeveloperModeAction.OnSaleDetailToggle(it)) },
                    )
                }

                val sourceLabel = if (state.saleDetailOverrideActive) {
                    stringResource(R.string.developer_mode_source_override)
                } else {
                    stringResource(R.string.developer_mode_source_remote)
                }
                val sourceColor = if (state.saleDetailOverrideActive) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
                Text(text = sourceLabel, style = MaterialTheme.typography.bodySmall, color = sourceColor)

                if (state.saleDetailOverrideActive) {
                    TextButton(onClick = { onAction(DeveloperModeAction.OnResetSaleDetailOverride) }) {
                        Text(stringResource(R.string.developer_mode_reset_override))
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricBar(label: String, progress: Float, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(10.dp),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round,
        )
    }
}
