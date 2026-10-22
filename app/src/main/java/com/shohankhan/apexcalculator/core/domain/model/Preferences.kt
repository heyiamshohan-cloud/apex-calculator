package com.shohankhan.apexcalculator.core.domain.model

/** Theme selection. SYSTEM follows the device setting. */
enum class ThemeMode(val label: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark"),
}

/** Accent identity of the product. Each option has a light and a dark tuned palette. */
enum class AccentChoice(val label: String) {
    OCEAN("Ocean Blue"),
    VIOLET("Violet"),
    TEAL("Teal"),
    AMBER("Amber"),
}

enum class AngleMode(val label: String, val shortLabel: String) {
    DEG("Degrees", "DEG"),
    RAD("Radians", "RAD"),
}

/** How numeric results are rendered. The numeric value is never derived from this setting. */
enum class NumberFormatMode(val label: String) {
    AUTO("Automatic"),
    SCIENTIFIC("Scientific"),
    ENGINEERING("Engineering"),
    FIXED("Fixed decimals"),
    SIGNIFICANT("Significant figures"),
}

/** Automatic history retention options. NEVER keeps records until the user deletes them. */
enum class RetentionPolicy(val days: Int, val label: String) {
    NEVER(0, "Never"),
    ONE_DAY(1, "After 1 day"),
    SEVEN_DAYS(7, "After 7 days"),
    THIRTY_DAYS(30, "After 30 days"),
    NINETY_DAYS(90, "After 90 days"),
    ONE_YEAR(365, "After 1 year");

    companion object {
        val DEFAULT = NINETY_DAYS
        fun fromDays(days: Int): RetentionPolicy = entries.firstOrNull { it.days == days } ?: DEFAULT
    }
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val accent: AccentChoice = AccentChoice.OCEAN,
    val angleMode: AngleMode = AngleMode.DEG,
    val numberFormat: NumberFormatMode = NumberFormatMode.AUTO,
    val decimalPlaces: Int = 8,
    val significantFigures: Int = 10,
    val thousandsSeparator: Boolean = true,
    val fractionDisplay: Boolean = false,
    val scientificThreshold: Int = 12,
    val defaultTabIndex: Int = 0,
    val hapticsEnabled: Boolean = true,
    val reducedMotion: Boolean = false,
    val historyRetention: RetentionPolicy = RetentionPolicy.DEFAULT,
    val persistMemory: Boolean = true,
    val autoSaveHistory: Boolean = true,
)

/** The six primary destinations, in navigation order. */
enum class ApexTab(val label: String, val route: String, val index: Int) {
    STANDARD("Standard", "standard", 0),
    SCIENTIFIC("Scientific", "scientific", 1),
    FORMULA("Formula Lab", "formula", 2),
    QUICKSOLVE("Quick Solve", "quick", 3),
    PROGRAMMER("Programmer", "programmer", 4),
    TOOLS("Math Tools", "tools", 5);

    companion object {
        fun fromIndex(index: Int): ApexTab = entries.firstOrNull { it.index == index } ?: STANDARD
        fun fromRoute(route: String?): ApexTab? = entries.firstOrNull { it.route == route }
    }
}
