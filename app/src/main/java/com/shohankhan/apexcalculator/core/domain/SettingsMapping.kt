package com.shohankhan.apexcalculator.core.domain

import com.shohankhan.apexcalculator.core.domain.model.AngleMode
import com.shohankhan.apexcalculator.core.domain.model.AppSettings
import com.shohankhan.apexcalculator.core.math.AngleUnit
import com.shohankhan.apexcalculator.core.math.EvalOptions
import com.shohankhan.apexcalculator.core.math.FormatSettings
import com.shohankhan.apexcalculator.core.math.Num

fun AngleMode.toAngleUnit(): AngleUnit = if (this == AngleMode.DEG) AngleUnit.DEG else AngleUnit.RAD

fun AngleUnit.toAngleMode(): AngleMode = if (this == AngleUnit.DEG) AngleMode.DEG else AngleMode.RAD

fun AppSettings.toFormatSettings(): FormatSettings = FormatSettings(
    decimalPlaces = decimalPlaces,
    numberFormat = numberFormat,
    thousandsSeparator = thousandsSeparator,
    fractionDisplay = fractionDisplay,
    scientificThreshold = scientificThreshold,
    significantFigures = significantFigures,
)

fun AppSettings.toEvalOptions(
    variables: Map<String, Num> = emptyMap(),
    complexMode: Boolean = false,
): EvalOptions = EvalOptions(
    angleUnit = angleMode.toAngleUnit(),
    complexMode = complexMode,
    variables = variables,
)
