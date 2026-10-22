package com.shohankhan.apexcalculator.core.designsystem

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ApexTopBar(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    navigationIcon: ImageVector? = null,
    navigationDescription: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val colors = MaterialTheme.apexColors
    Surface(color = colors.background) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .heightIn(min = 60.dp)
                    .padding(horizontal = ApexSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (onBack != null) {
                    IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                        Icon(
                            imageVector = navigationIcon ?: ApexIcons.Back,
                            contentDescription = navigationDescription ?: "Back",
                            tint = colors.textPrimary,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Spacer(Modifier.width(ApexSpacing.xxs))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                actions()
            }
        }
    }
}

@Composable
fun ApexScreen(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    content: @Composable (PaddingValues) -> Unit,
) {
    val colors = MaterialTheme.apexColors
    Scaffold(
        containerColor = colors.background,
        topBar = {
            ApexTopBar(
                title = title,
                subtitle = subtitle,
                onBack = onBack,
                actions = actions,
            )
        },
        bottomBar = bottomBar,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        content(padding)
    }
}

@Composable
fun ApexCard(
    modifier: Modifier = Modifier,
    color: Color? = null,
    border: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(ApexSpacing.lg),
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val colors = MaterialTheme.apexColors
    val shape = RoundedCornerShape(ApexRadius.lg)
    val base = modifier
        .fillMaxWidth()
        .clip(shape)
        .background(color ?: colors.surface)
        .then(
            if (border) {
                Modifier.border(BorderStroke(1.dp, colors.divider), shape)
            } else {
                Modifier
            },
        )
        .then(
            if (onClick != null) {
                Modifier.clickable(onClick = onClick)
            } else {
                Modifier
            },
        )
    Box(modifier = base.padding(contentPadding)) {
        content()
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
) {
    val colors = MaterialTheme.apexColors
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                )
            }
        }
        if (trailing != null) trailing()
    }
}

enum class KeyStyle { DIGIT, OPERATOR, FUNCTION, ACCENT, DANGER }

@Composable
fun KeyButton(
    label: String,
    style: KeyStyle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    labelSize: Int = 22,
    enabled: Boolean = true,
    hapticsEnabled: Boolean = true,
    subLabel: String? = null,
) {
    val colors = MaterialTheme.apexColors
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.955f else 1f,
        animationSpec = tween(durationMillis = 90),
        label = "key-scale",
    )
    val background = when (style) {
        KeyStyle.DIGIT -> colors.keyDigit
        KeyStyle.OPERATOR -> colors.keyOperator
        KeyStyle.FUNCTION -> colors.keyFunction
        KeyStyle.ACCENT -> colors.keyAccent
        KeyStyle.DANGER -> colors.keyFunction
    }
    val textColor = when (style) {
        KeyStyle.DIGIT -> colors.keyDigitText
        KeyStyle.OPERATOR -> colors.keyOperatorText
        KeyStyle.FUNCTION -> colors.keyFunctionText
        KeyStyle.ACCENT -> colors.keyAccentText
        KeyStyle.DANGER -> colors.keyDangerText
    }
    val haptic = LocalHapticFeedback.current
    val shape = RoundedCornerShape(ApexRadius.md)
    Box(
        modifier = modifier
            .heightIn(min = 48.dp)
            .scale(scale)
            .clip(shape)
            .background(if (enabled) background else background.copy(alpha = 0.4f))
            .clickable(
                interactionSource = interactionSource,
                enabled = enabled,
                role = Role.Button,
                onClick = {
                    if (hapticsEnabled) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                    onClick()
                },
            )
            .semantics { if (contentDescription != null) this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                color = if (enabled) textColor else textColor.copy(alpha = 0.5f),
                fontSize = labelSize.sp,
                lineHeight = (labelSize + 4).sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
            if (subLabel != null) {
                Text(
                    text = subLabel,
                    color = (if (enabled) textColor else textColor.copy(alpha = 0.5f)).copy(alpha = 0.7f),
                    fontSize = (labelSize - 6).coerceAtLeast(9).sp,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
fun IconAction(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    tint: Color? = null,
    enabled: Boolean = true,
) {
    val colors = MaterialTheme.apexColors
    IconButton(onClick = onClick, enabled = enabled, modifier = Modifier.size(44.dp)) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = (tint ?: colors.textSecondary).copy(alpha = if (enabled) 1f else 0.4f),
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val colors = MaterialTheme.apexColors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(ApexSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(colors.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(26.dp))
        }
        Spacer(Modifier.height(ApexSpacing.lg))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(ApexSpacing.xs))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(ApexSpacing.lg))
            TextButton(onClick = onAction) { Text(actionLabel) }
        }
    }
}

enum class BannerTone { INFO, SUCCESS, WARNING, ERROR }

@Composable
fun InfoBanner(
    text: String,
    tone: BannerTone,
    modifier: Modifier = Modifier,
    title: String? = null,
) {
    val colors = MaterialTheme.apexColors
    val accent = when (tone) {
        BannerTone.INFO -> colors.primary
        BannerTone.SUCCESS -> colors.success
        BannerTone.WARNING -> colors.warning
        BannerTone.ERROR -> colors.error
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ApexRadius.md))
            .background(accent.copy(alpha = 0.12f))
            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(ApexRadius.md))
            .padding(ApexSpacing.md),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = if (tone == BannerTone.ERROR || tone == BannerTone.WARNING) ApexIcons.Warning else ApexIcons.Info,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(ApexSpacing.sm))
        Column {
            if (title != null) {
                Text(text = title, style = MaterialTheme.typography.labelLarge, color = accent)
                Spacer(Modifier.height(ApexSpacing.xxs))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textPrimary,
            )
        }
    }
}

@Composable
fun ValueRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false,
) {
    val colors = MaterialTheme.apexColors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = ApexSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = if (emphasized) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
            color = if (emphasized) colors.primary else colors.textPrimary,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
fun ApexTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    supportingText: String? = null,
    isError: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    enabled: Boolean = true,
    suffix: String? = null,
) {
    val colors = MaterialTheme.apexColors
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        supportingText = supportingText?.let { { Text(it) } },
        isError = isError,
        singleLine = singleLine,
        minLines = minLines,
        enabled = enabled,
        suffix = suffix?.let { { Text(it) } },
        shape = RoundedCornerShape(ApexRadius.md),
        modifier = modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.primary,
            unfocusedBorderColor = colors.divider,
            errorBorderColor = colors.error,
            focusedLabelColor = colors.primary,
            unfocusedLabelColor = colors.textSecondary,
            focusedTextColor = colors.textPrimary,
            unfocusedTextColor = colors.textPrimary,
            disabledTextColor = colors.textTertiary,
            cursorColor = colors.primary,
            focusedContainerColor = colors.surface,
            unfocusedContainerColor = colors.surface,
            errorContainerColor = colors.surface,
            errorSupportingTextColor = colors.error,
            focusedSupportingTextColor = colors.textSecondary,
            unfocusedSupportingTextColor = colors.textSecondary,
        ),
    )
}

@Composable
fun SelectableChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.apexColors
    val background by animateColorAsState(
        targetValue = if (selected) colors.primaryContainer else colors.surfaceVariant,
        animationSpec = tween(160),
        label = "chip-bg",
    )
    val contentColor = if (selected) colors.primary else colors.textSecondary
    Box(
        modifier = modifier
            .heightIn(min = 40.dp)
            .clip(RoundedCornerShape(ApexRadius.pill))
            .background(background)
            .then(
                if (selected) Modifier.border(1.dp, colors.primary.copy(alpha = 0.5f), RoundedCornerShape(ApexRadius.pill))
                else Modifier,
            )
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = ApexSpacing.md, vertical = ApexSpacing.sm),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
            maxLines = 1,
        )
    }
}

@Composable
fun ChipRow(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ApexSpacing.sm),
    ) {
        options.forEachIndexed { index, option ->
            SelectableChip(
                label = option,
                selected = index == selectedIndex,
                onClick = { onSelect(index) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    destructive: Boolean = false,
) {
    val colors = MaterialTheme.apexColors
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceElevated,
        title = {
            Text(title, style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
        },
        text = {
            Text(message, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmLabel, color = if (destructive) colors.error else colors.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = colors.textSecondary)
            }
        },
        shape = RoundedCornerShape(ApexRadius.lg),
    )
}

@Composable
fun LoadingRow(modifier: Modifier = Modifier, label: String = "Calculating…") {
    val colors = MaterialTheme.apexColors
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(18.dp),
            strokeWidth = 2.dp,
            color = colors.primary,
        )
        Spacer(Modifier.width(ApexSpacing.sm))
        Text(label, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
    }
}

@Composable
fun DividerLine(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier.fillMaxWidth(),
        thickness = 1.dp,
        color = MaterialTheme.apexColors.divider,
    )
}

@Composable
fun SmallPill(text: String, tone: Color? = null) {
    val colors = MaterialTheme.apexColors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(ApexRadius.pill))
            .background((tone ?: colors.primary).copy(alpha = 0.14f))
            .widthIn(min = 28.dp)
            .padding(horizontal = ApexSpacing.sm, vertical = ApexSpacing.xxs),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = tone ?: colors.primary,
            maxLines = 1,
        )
    }
}

@Composable
fun MetricTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    tone: Color? = null,
) {
    val colors = MaterialTheme.apexColors
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(ApexRadius.md))
            .background(colors.surfaceVariant)
            .padding(ApexSpacing.md),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(ApexSpacing.xs))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = tone ?: colors.textPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun VerticalGap(height: Dp = ApexSpacing.md) {
    Spacer(Modifier.height(height))
}
