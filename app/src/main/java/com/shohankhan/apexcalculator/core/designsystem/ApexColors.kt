package com.shohankhan.apexcalculator.core.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.shohankhan.apexcalculator.core.domain.model.AccentChoice

/**
 * Semantic colour tokens. Screens must never reference raw hex values; they read these tokens
 * through [LocalApexColors] so that light, dark and accent variants stay consistent.
 */
@Immutable
data class ApexColors(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val surfaceSunken: Color,
    val surfaceVariant: Color,
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val success: Color,
    val onSuccess: Color,
    val warning: Color,
    val error: Color,
    val onError: Color,
    val errorContainer: Color,
    val divider: Color,
    val keyDigit: Color,
    val keyDigitText: Color,
    val keyOperator: Color,
    val keyOperatorText: Color,
    val keyFunction: Color,
    val keyFunctionText: Color,
    val keyAccent: Color,
    val keyAccentText: Color,
    val keyDangerText: Color,
    val focusRing: Color,
)

private data class AccentTones(
    val primaryDark: Color,
    val primaryLight: Color,
    val containerDark: Color,
    val containerLight: Color,
    val secondary: Color,
)

private val accents: Map<AccentChoice, AccentTones> = mapOf(
    AccentChoice.OCEAN to AccentTones(
        primaryDark = Color(0xFF5AA9FF),
        primaryLight = Color(0xFF1B5FD0),
        containerDark = Color(0xFF16304F),
        containerLight = Color(0xFFDCE9FF),
        secondary = Color(0xFF38BDF8),
    ),
    AccentChoice.VIOLET to AccentTones(
        primaryDark = Color(0xFFB39DFF),
        primaryLight = Color(0xFF5B3BC4),
        containerDark = Color(0xFF2C2350),
        containerLight = Color(0xFFE9E2FF),
        secondary = Color(0xFF8B7CF6),
    ),
    AccentChoice.TEAL to AccentTones(
        primaryDark = Color(0xFF4FD1C5),
        primaryLight = Color(0xFF0F766E),
        containerDark = Color(0xFF123B3A),
        containerLight = Color(0xFFD5F5F1),
        secondary = Color(0xFF2DD4BF),
    ),
    AccentChoice.AMBER to AccentTones(
        primaryDark = Color(0xFFF5C24B),
        primaryLight = Color(0xFF9A5B00),
        containerDark = Color(0xFF3B2C12),
        containerLight = Color(0xFFFDECD0),
        secondary = Color(0xFFFBBF24),
    ),
)

fun apexDarkColors(accent: AccentChoice): ApexColors {
    val tones = accents[accent] ?: accents.getValue(AccentChoice.OCEAN)
    return ApexColors(
        isDark = true,
        background = Color(0xFF080D16),
        surface = Color(0xFF0E1622),
        surfaceElevated = Color(0xFF162030),
        surfaceSunken = Color(0xFF060A12),
        surfaceVariant = Color(0xFF1B2637),
        primary = tones.primaryDark,
        onPrimary = Color(0xFF04101F),
        primaryContainer = tones.containerDark,
        onPrimaryContainer = Color(0xFFDCE9FF),
        secondary = tones.secondary,
        onSecondary = Color(0xFF04101F),
        textPrimary = Color(0xFFEFF4FB),
        textSecondary = Color(0xFFA3B1C6),
        textTertiary = Color(0xFF74849B),
        success = Color(0xFF4ADE80),
        onSuccess = Color(0xFF05210F),
        warning = Color(0xFFFBBF24),
        error = Color(0xFFFF7A7A),
        onError = Color(0xFF2A0606),
        errorContainer = Color(0xFF3A1618),
        divider = Color(0xFF1F2B3D),
        keyDigit = Color(0xFF141E2C),
        keyDigitText = Color(0xFFEFF4FB),
        keyOperator = Color(0xFF1A2B44),
        keyOperatorText = tones.primaryDark,
        keyFunction = Color(0xFF101A26),
        keyFunctionText = Color(0xFFA3B1C6),
        keyAccent = tones.primaryDark,
        keyAccentText = Color(0xFF04101F),
        keyDangerText = Color(0xFFFF9A9A),
        focusRing = tones.primaryDark.copy(alpha = 0.55f),
    )
}

fun apexLightColors(accent: AccentChoice): ApexColors {
    val tones = accents[accent] ?: accents.getValue(AccentChoice.OCEAN)
    return ApexColors(
        isDark = false,
        background = Color(0xFFF4F7FC),
        surface = Color(0xFFFFFFFF),
        surfaceElevated = Color(0xFFFFFFFF),
        surfaceSunken = Color(0xFFE9EFF7),
        surfaceVariant = Color(0xFFEDF2F9),
        primary = tones.primaryLight,
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = tones.containerLight,
        onPrimaryContainer = Color(0xFF0B1B33),
        secondary = tones.secondary,
        onSecondary = Color(0xFFFFFFFF),
        textPrimary = Color(0xFF0D1522),
        textSecondary = Color(0xFF57657A),
        textTertiary = Color(0xFF8493A6),
        success = Color(0xFF15803D),
        onSuccess = Color(0xFFFFFFFF),
        warning = Color(0xFFB45309),
        error = Color(0xFFB42318),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFDE7E7),
        divider = Color(0xFFDCE4EF),
        keyDigit = Color(0xFFFFFFFF),
        keyDigitText = Color(0xFF0D1522),
        keyOperator = Color(0xFFE7EFFB),
        keyOperatorText = tones.primaryLight,
        keyFunction = Color(0xFFF0F4FA),
        keyFunctionText = Color(0xFF57657A),
        keyAccent = tones.primaryLight,
        keyAccentText = Color(0xFFFFFFFF),
        keyDangerText = Color(0xFFB42318),
        focusRing = tones.primaryLight.copy(alpha = 0.5f),
    )
}

val LocalApexColors = staticCompositionLocalOf { apexDarkColors(AccentChoice.OCEAN) }
