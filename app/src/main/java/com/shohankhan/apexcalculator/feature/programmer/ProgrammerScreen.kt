package com.shohankhan.apexcalculator.feature.programmer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shohankhan.apexcalculator.core.designsystem.ApexCard
import com.shohankhan.apexcalculator.core.designsystem.ApexIcons
import com.shohankhan.apexcalculator.core.designsystem.ApexRadius
import com.shohankhan.apexcalculator.core.designsystem.ApexSpacing
import com.shohankhan.apexcalculator.core.designsystem.ApexTextField
import com.shohankhan.apexcalculator.core.designsystem.BannerTone
import com.shohankhan.apexcalculator.core.designsystem.IconAction
import com.shohankhan.apexcalculator.core.designsystem.InfoBanner
import com.shohankhan.apexcalculator.core.designsystem.SelectableChip
import com.shohankhan.apexcalculator.core.designsystem.apexColors
import com.shohankhan.apexcalculator.core.domain.model.ApexTab
import com.shohankhan.apexcalculator.core.math.BitWidth
import com.shohankhan.apexcalculator.core.math.NumberRadix
import com.shohankhan.apexcalculator.core.math.ProgrammerMath
import com.shohankhan.apexcalculator.core.math.ProgrammerOp
import com.shohankhan.apexcalculator.core.math.ProgrammerOutcome
import com.shohankhan.apexcalculator.core.math.ProgrammerValue
import com.shohankhan.apexcalculator.navigation.TabScaffold
import java.math.BigInteger

@Composable
fun ProgrammerScreen(
    onNavigate: (ApexTab) -> Unit = {},
    onOpenHistory: () -> Unit = {},
    onOpenFavorites: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenAbout: () -> Unit = {},
    onOpenPrivacy: () -> Unit = {},
) {
    val colors = MaterialTheme.apexColors
    val clipboard = LocalClipboardManager.current
    var radixName by rememberSaveable { mutableStateOf(NumberRadix.DEC.name) }
    var widthBits by rememberSaveable { mutableStateOf(32) }
    var signed by rememberSaveable { mutableStateOf(true) }
    var rawValue by rememberSaveable { mutableStateOf("12") }
    var operandText by rememberSaveable { mutableStateOf("10") }
    var operationName by rememberSaveable { mutableStateOf(ProgrammerOp.AND.name) }
    var shiftCountText by rememberSaveable { mutableStateOf("1") }
    var message by remember { mutableStateOf<String?>(null) }
    var note by remember { mutableStateOf<String?>(null) }

    val radix = NumberRadix.entries.firstOrNull { it.name == radixName } ?: NumberRadix.DEC
    val width = BitWidth.fromBits(widthBits)
    val operation = ProgrammerOp.entries.firstOrNull { it.name == operationName } ?: ProgrammerOp.AND
    val value = remember(rawValue, radix, widthBits, signed) {
        ProgrammerValue.parse(rawValue, radix, width, signed)
    }

    TabScaffold(
        tab = ApexTab.PROGRAMMER,
        onNavigate = onNavigate,
        onOpenHistory = onOpenHistory,
        onOpenFavorites = onOpenFavorites,
        onOpenSettings = onOpenSettings,
        onOpenAbout = onOpenAbout,
        onOpenPrivacy = onOpenPrivacy,
        subtitle = "${width.bits}-bit ${if (signed) "signed" else "unsigned"}",
        actions = {
            IconAction(
                icon = ApexIcons.Copy,
                description = "Copy value",
                onClick = {
                    value?.let { clipboard.setText(AnnotatedString(it.format(radix, grouped = false))) }
                },
            )
        },
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = ApexSpacing.md),
        ) {
            val landscape = maxWidth > maxHeight && maxHeight < 520.dp
            val input: @Composable (Modifier) -> Unit = { modifier ->
                Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(ApexSpacing.sm)) {
                    RadixPanel(value = value, active = radix, onSelect = { radixName = it.name })
                    ApexCard {
                        Column {
                            ApexTextField(
                                value = rawValue,
                                onValueChange = {
                                    rawValue = it
                                    message = null
                                    note = null
                                },
                                label = "Value (${radix.label})",
                                supportingText = "Digits 0–9${if (radix.base > 10) " and A–F" else ""}. A leading minus is allowed in DEC.",
                                isError = value == null,
                                singleLine = true,
                            )
                            Spacer(Modifier.height(ApexSpacing.sm))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(ApexSpacing.sm),
                            ) {
                                BitWidth.entries.forEach { entry ->
                                    SelectableChip(
                                        label = "${entry.bits}",
                                        selected = entry.bits == widthBits,
                                        onClick = {
                                            widthBits = entry.bits
                                            message = null
                                            note = null
                                        },
                                    )
                                }
                            }
                            Spacer(Modifier.height(ApexSpacing.sm))
                            Row(horizontalArrangement = Arrangement.spacedBy(ApexSpacing.sm)) {
                                SelectableChip(
                                    label = "Signed",
                                    selected = signed,
                                    onClick = {
                                        signed = true
                                        message = null
                                    },
                                )
                                SelectableChip(
                                    label = "Unsigned",
                                    selected = !signed,
                                    onClick = {
                                        signed = false
                                        message = null
                                    },
                                )
                            }
                        }
                    }
                }
            }
            val workspace: @Composable (Modifier) -> Unit = { modifier ->
                LazyColumn(
                    modifier = modifier,
                    contentPadding = PaddingValues(bottom = ApexSpacing.xxl),
                    verticalArrangement = Arrangement.spacedBy(ApexSpacing.sm),
                ) {
                    item {
                        ApexCard {
                            Column {
                                Text(
                                    text = "Bitwise operations",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = colors.textPrimary,
                                )
                                Spacer(Modifier.height(ApexSpacing.sm))
                                Row(
                                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(ApexSpacing.xs),
                                ) {
                                    ProgrammerOp.entries.forEach { op ->
                                        SelectableChip(
                                            label = op.label,
                                            selected = op == operation,
                                            onClick = {
                                                operationName = op.name
                                                message = null
                                                note = null
                                            },
                                        )
                                    }
                                }
                                if (operation != ProgrammerOp.NOT) {
                                    Spacer(Modifier.height(ApexSpacing.sm))
                                    if (operation == ProgrammerOp.SHIFT_LEFT ||
                                        operation == ProgrammerOp.SHIFT_RIGHT ||
                                        operation == ProgrammerOp.SHIFT_ARITHMETIC
                                    ) {
                                        ApexTextField(
                                            value = shiftCountText,
                                            onValueChange = { shiftCountText = it },
                                            label = "Shift count",
                                            supportingText = "0 to ${width.bits - 1}",
                                            singleLine = true,
                                        )
                                    } else {
                                        ApexTextField(
                                            value = operandText,
                                            onValueChange = {
                                                operandText = it
                                                message = null
                                                note = null
                                            },
                                            label = "Operand (${radix.label})",
                                            singleLine = true,
                                        )
                                    }
                                }
                                Spacer(Modifier.height(ApexSpacing.sm))
                                com.shohankhan.apexcalculator.core.designsystem.KeyButton(
                                    label = "Apply ${operation.symbol}",
                                    style = com.shohankhan.apexcalculator.core.designsystem.KeyStyle.ACCENT,
                                    onClick = {
                                        val current = value
                                        if (current == null) {
                                            message = "Enter a valid ${radix.label} value first"
                                            note = null
                                            return@KeyButton
                                        }
                                        val shift = shiftCountText.trim().toIntOrNull()
                                        val operand = if (operation == ProgrammerOp.NOT ||
                                            operation == ProgrammerOp.SHIFT_LEFT ||
                                            operation == ProgrammerOp.SHIFT_RIGHT ||
                                            operation == ProgrammerOp.SHIFT_ARITHMETIC
                                        ) {
                                            null
                                        } else {
                                            ProgrammerValue.parse(operandText, radix, width, signed)
                                        }
                                        if (operand == null &&
                                            operation != ProgrammerOp.NOT &&
                                            operation != ProgrammerOp.SHIFT_LEFT &&
                                            operation != ProgrammerOp.SHIFT_RIGHT &&
                                            operation != ProgrammerOp.SHIFT_ARITHMETIC
                                        ) {
                                            message = "Enter a valid operand"
                                            note = null
                                            return@KeyButton
                                        }
                                        val outcome = ProgrammerMath.apply(
                                            accumulator = current,
                                            operand = operand,
                                            operation = operation,
                                            shiftCount = shift ?: 0,
                                        )
                                        when (outcome) {
                                            is ProgrammerOutcome.Success -> {
                                                rawValue = outcome.value.format(radix, grouped = false)
                                                message = null
                                                note = outcome.note
                                            }
                                            is ProgrammerOutcome.Failure -> {
                                                message = outcome.message
                                                note = null
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp),
                                    labelSize = 16,
                                    hapticsEnabled = false,
                                )
                            }
                        }
                    }
                    message?.let { text ->
                        item { InfoBanner(text = text, tone = BannerTone.ERROR) }
                    }
                    note?.let { text ->
                        item { InfoBanner(text = text, tone = BannerTone.WARNING) }
                    }
                    value?.let { current ->
                        item {
                            ApexCard {
                                Column {
                                    Text(
                                        text = "Two's complement",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = colors.textPrimary,
                                    )
                                    Spacer(Modifier.height(ApexSpacing.xs))
                                    Text(
                                        text = "Unsigned ${current.unsigned}  ·  Signed ${current.decimal}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colors.textSecondary,
                                    )
                                    Text(
                                        text = "Representable range: ${width.minSigned} to ${width.maxSigned}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.textTertiary,
                                    )
                                }
                            }
                        }
                        item { BitInspector(current) }
                    }
                }
            }
            if (landscape) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(ApexSpacing.md),
                ) {
                    input(Modifier.weight(0.42f))
                    workspace(Modifier.weight(0.58f))
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    input(Modifier.fillMaxWidth())
                    Spacer(Modifier.height(ApexSpacing.sm))
                    workspace(Modifier.fillMaxWidth().weight(1f))
                }
            }
        }
    }
}

@Composable
private fun RadixPanel(
    value: ProgrammerValue?,
    active: NumberRadix,
    onSelect: (NumberRadix) -> Unit,
) {
    val colors = MaterialTheme.apexColors
    ApexCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(ApexSpacing.sm)) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            NumberRadix.entries.forEach { radix ->
                val text = value?.format(radix) ?: "—"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (radix == active) colors.primaryContainer else androidx.compose.ui.graphics.Color.Transparent,
                            RoundedCornerShape(ApexRadius.sm),
                        )
                        .clickable(role = Role.Tab) { onSelect(radix) }
                        .padding(horizontal = ApexSpacing.sm, vertical = ApexSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = radix.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (radix == active) colors.primary else colors.textTertiary,
                        modifier = Modifier.width(42.dp),
                    )
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, fontSize = 14.sp),
                        color = if (radix == active) colors.textPrimary else colors.textSecondary,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                    )
                }
            }
        }
    }
}

@Composable
private fun BitInspector(value: ProgrammerValue) {
    val colors = MaterialTheme.apexColors
    var working by remember(value) { mutableStateOf(value) }
    val positions = remember(working) { ProgrammerMath.bitPositions(working.width) }
    ApexCard {
        Column {
            Row {
                Text(
                    text = "Bit inspector",
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "Tap to toggle",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary,
                )
            }
            Spacer(Modifier.height(ApexSpacing.sm))
            Column(verticalArrangement = Arrangement.spacedBy(ApexSpacing.xxs)) {
                positions.chunked(8).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        row.forEach { index ->
                            val set = working.isBitSet(index)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                                    .background(
                                        if (set) colors.primary else colors.surfaceVariant,
                                        RoundedCornerShape(6.dp),
                                    )
                                    .clickable { working = working.toggleBit(index) },
                                contentAlignment = Alignment.Center,
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$index",
                                        fontSize = 8.sp,
                                        color = if (set) colors.onPrimary.copy(alpha = 0.8f) else colors.textTertiary,
                                    )
                                    Text(
                                        text = if (set) "1" else "0",
                                        fontSize = 13.sp,
                                        color = if (set) colors.onPrimary else colors.textSecondary,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(ApexSpacing.sm))
            Text(
                text = "Pattern ${working.pattern.toString(2).padStart(working.width.bits, '0')}",
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                color = colors.textSecondary,
                maxLines = 2,
            )
            Text(
                text = "Hex ${working.format(NumberRadix.HEX, grouped = false)}  ·  Dec ${working.decimal}  ·  Unsigned ${working.unsigned}",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
            )
        }
    }
}
