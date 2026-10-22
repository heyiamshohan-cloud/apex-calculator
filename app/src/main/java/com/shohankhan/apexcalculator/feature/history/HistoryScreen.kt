package com.shohankhan.apexcalculator.feature.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shohankhan.apexcalculator.core.designsystem.ApexCard
import com.shohankhan.apexcalculator.core.designsystem.ApexIcons
import com.shohankhan.apexcalculator.core.designsystem.ApexRadius
import com.shohankhan.apexcalculator.core.designsystem.ApexSpacing
import com.shohankhan.apexcalculator.core.designsystem.ConfirmDialog
import com.shohankhan.apexcalculator.core.designsystem.EmptyState
import com.shohankhan.apexcalculator.core.designsystem.IconAction
import com.shohankhan.apexcalculator.core.designsystem.InfoBanner
import com.shohankhan.apexcalculator.core.designsystem.SmallPill
import com.shohankhan.apexcalculator.core.designsystem.apexColors
import com.shohankhan.apexcalculator.core.domain.ExpressionHandoff
import com.shohankhan.apexcalculator.core.domain.model.HistoryRecord
import com.shohankhan.apexcalculator.navigation.SecondaryScaffold

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    onUseExpression: () -> Unit,
    viewModel: HistoryViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = MaterialTheme.apexColors
    var confirmClear by remember { mutableStateOf(false) }

    SecondaryScaffold(
        title = "Calculation history",
        subtitle = if (state.isSelecting) {
            "${state.selectedIds.size} selected"
        } else {
            "${state.records.size} records · ${state.retention.label.lowercase()} cleanup"
        },
        onBack = if (state.isSelecting) viewModel::clearSelection else onBack,
        actions = {
            when {
                state.isSelecting -> {
                    IconAction(
                        icon = ApexIcons.Delete,
                        description = "Delete selected records",
                        tint = colors.error,
                        onClick = viewModel::deleteSelected,
                        enabled = state.selectedIds.isNotEmpty(),
                    )
                }
                else -> {
                    IconAction(
                        icon = ApexIcons.Delete,
                        description = "Delete all history",
                        tint = colors.error,
                        enabled = state.records.isNotEmpty(),
                        onClick = { confirmClear = true },
                    )
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = ApexSpacing.md),
        ) {
            com.shohankhan.apexcalculator.core.ui.SearchBar(
                query = state.query,
                onQueryChange = viewModel::onQueryChange,
                placeholder = "Search expressions and results",
            )
            Spacer(Modifier.height(ApexSpacing.sm))
            if (state.query.isBlank() && state.records.isNotEmpty()) {
                InfoBanner(
                    text = "Records older than ${state.retention.label.lowercase()} are removed automatically. Favorites are never affected.",
                    tone = com.shohankhan.apexcalculator.core.designsystem.BannerTone.INFO,
                )
                Spacer(Modifier.height(ApexSpacing.sm))
            }
            if (state.records.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    EmptyState(
                        icon = ApexIcons.History,
                        title = if (state.query.isBlank()) "No calculations yet" else "Nothing matched",
                        message = if (state.query.isBlank()) {
                            "Results you calculate are stored on this device only."
                        } else {
                            "Try a shorter search term."
                        },
                        actionLabel = if (state.query.isBlank()) null else "Clear search",
                        onAction = { viewModel.onQueryChange("") },
                        modifier = Modifier.padding(top = ApexSpacing.xl),
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = ApexSpacing.xxl),
                    verticalArrangement = Arrangement.spacedBy(ApexSpacing.xs),
                ) {
                    state.groups.forEach { (label, records) ->
                        item(key = "header-$label") {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.textSecondary,
                                modifier = Modifier.padding(
                                    start = ApexSpacing.xs,
                                    top = ApexSpacing.md,
                                    bottom = ApexSpacing.xs,
                                ),
                            )
                        }
                        items(records, key = { it.id }) { record ->
                            HistoryRow(
                                record = record,
                                selected = record.id in state.selectedIds,
                                selecting = state.isSelecting,
                                onLongClick = { viewModel.toggleSelection(record) },
                                onClick = {
                                    if (state.isSelecting) {
                                        viewModel.toggleSelection(record)
                                    } else {
                                        ExpressionHandoff.offer(record.expression)
                                        onUseExpression()
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    if (confirmClear) {
        ConfirmDialog(
            title = "Delete all history?",
            message = "This removes every stored calculation from this device. Saved favorites and settings are kept.",
            confirmLabel = "Delete all",
            destructive = true,
            onConfirm = {
                confirmClear = false
                viewModel.clearAll()
            },
            onDismiss = { confirmClear = false },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HistoryRow(
    record: HistoryRecord,
    selected: Boolean,
    selecting: Boolean,
    onLongClick: () -> Unit,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.apexColors
    ApexCard(
        border = !selected,
        color = if (selected) colors.primaryContainer else colors.surface,
        contentPadding = PaddingValues(ApexSpacing.md),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = onClick, onLongClick = onLongClick),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (selecting) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(
                            if (selected) colors.primary else Color.Transparent,
                            RoundedCornerShape(5.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (selected) {
                        androidx.compose.material3.Icon(
                            imageVector = ApexIcons.Check,
                            contentDescription = null,
                            tint = colors.onPrimary,
                            modifier = Modifier.size(12.dp),
                        )
                    }
                }
                Spacer(Modifier.width(ApexSpacing.sm))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.expression,
                    style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                    color = colors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "= ${record.result}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                    color = colors.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SmallPill(record.source.label)
                    Spacer(Modifier.width(ApexSpacing.xs))
                    Text(
                        text = HistoryGroups.time(record.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textTertiary,
                    )
                }
            }
        }
    }
}
