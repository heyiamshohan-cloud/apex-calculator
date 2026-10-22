package com.shohankhan.apexcalculator.feature.scientific

import android.app.Application
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shohankhan.apexcalculator.ApexApplication
import com.shohankhan.apexcalculator.core.domain.model.AppSettings
import com.shohankhan.apexcalculator.core.domain.model.AngleMode
import com.shohankhan.apexcalculator.core.domain.toEvalOptions
import com.shohankhan.apexcalculator.core.domain.toFormatSettings
import com.shohankhan.apexcalculator.core.math.CalculatorEngine
import com.shohankhan.apexcalculator.core.math.EvalOutcome
import com.shohankhan.apexcalculator.core.math.ExpressionPreview
import com.shohankhan.apexcalculator.core.math.MathError
import com.shohankhan.apexcalculator.core.math.Num
import com.shohankhan.apexcalculator.core.math.NumMath
import com.shohankhan.apexcalculator.core.math.Numerical
import com.shohankhan.apexcalculator.core.math.ResultFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CalculusUiState(
    val function: String = "x^2",
    val point: String = "2",
    val lower: String = "0",
    val upper: String = "1",
    val output: String? = null,
    val error: String? = null,
)

data class ScientificUiState(
    val expression: TextFieldValue = TextFieldValue(""),
    val settings: AppSettings = AppSettings(),
    val committed: Num? = null,
    val preview: Num? = null,
    val error: MathError? = null,
    val justEvaluated: Boolean = false,
    val lastAnswer: String? = null,
    val shift: Boolean = false,
    val complexMode: Boolean = false,
    val calculus: CalculusUiState = CalculusUiState(),
    val calculusVisible: Boolean = false,
) {
    val angleMode: AngleMode get() = settings.angleMode

    val canBackspace: Boolean get() = expression.text.isNotEmpty()
}

/** Scientific workspace state. Shares the engine and the settings with the Standard tab. */
class ScientificViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as ApexApplication).container
    private val _state = MutableStateFlow(ScientificUiState())
    val state: StateFlow<ScientificUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            container.settingsRepository.settings.collect { settings ->
                _state.update { it.copy(settings = settings).recalculated() }
            }
        }
        viewModelScope.launch {
            container.memoryRepository.lastAnswer.collect { answer ->
                _state.update { it.copy(lastAnswer = answer) }
            }
        }
    }

    fun formatter(): ResultFormatter = ResultFormatter(_state.value.settings.toFormatSettings())

    fun onExpressionChange(value: TextFieldValue) {
        _state.update { it.copy(expression = value, committed = null, justEvaluated = false).recalculated() }
    }

    fun insert(text: String) {
        _state.update { current ->
            val field = current.expression
            val start = field.selection.start.coerceIn(0, field.text.length)
            val end = field.selection.end.coerceIn(start, field.text.length)
            val updated = field.text.replaceRange(start, end, text)
            current.copy(
                expression = TextFieldValue(updated, TextRange(start + text.length)),
                committed = null,
                justEvaluated = false,
            ).recalculated()
        }
    }

    fun onDigit(digit: String) = insert(digit)

    fun onDecimal() = insert(".")

    fun onOperator(symbol: String) {
        _state.update { current ->
            val text = current.expression.text.trimEnd()
            if (text.isEmpty()) return@update current
            val last = text.last()
            val updated = if (last in "+-×÷*/^") "$text$symbol" else "$text$symbol"
            current.copy(
                expression = TextFieldValue(updated, TextRange(updated.length)),
                committed = null,
                justEvaluated = false,
            ).recalculated()
        }
    }

    fun onParen() {
        val text = _state.value.expression.text
        val opens = text.count { it == '(' }
        val closes = text.count { it == ')' }
        insert(if (closes >= opens) "(" else ")")
    }

    fun onContinuousToggle(offset: Int = -1) {
        _state.update { current ->
            val field = current.expression
            val updated = if (field.selection.start > offset) {
                TextFieldValue(
                    text = field.text,
                    selection = TextRange((field.selection.start + offset).coerceAtLeast(0)),
                )
            } else {
                field
            }
            current.copy(expression = updated)
        }
    }

    fun onBackspace() {
        _state.update { current ->
            val field = current.expression
            if (field.text.isEmpty()) return@update current
            val start = field.selection.start
            val end = field.selection.end
            val updated = if (start != end) {
                field.text.removeRange(start.coerceAtMost(end), maxOf(start, end))
            } else if (start > 0) {
                field.text.removeRange(start - 1, start)
            } else {
                field.text
            }
            current.copy(
                expression = TextFieldValue(updated, TextRange((if (start != end) start else start - 1).coerceIn(0, updated.length))),
                committed = null,
                justEvaluated = false,
            ).recalculated()
        }
    }

    fun onClear() {
        _state.update { it.copy(expression = TextFieldValue(""), committed = null, preview = null, error = null, justEvaluated = false) }
    }

    fun onEquals() {
        val current = _state.value
        val expression = current.expression.text
        if (expression.isBlank()) return
        when (val outcome = CalculatorEngine.evaluate(expression, current.options())) {
            is EvalOutcome.Success -> {
                val formatted = formatter().format(outcome.value)
                _state.update {
                    it.copy(
                        committed = outcome.value,
                        preview = outcome.value,
                        error = null,
                        justEvaluated = true,
                    )
                }
                viewModelScope.launch {
                    runCatching { container.memoryRepository.rememberAnswer(formatted) }
                }
                if (current.settings.autoSaveHistory) {
                    viewModelScope.launch {
                        runCatching {
                            container.historyRepository.add(
                                com.shohankhan.apexcalculator.core.domain.model.HistoryRecord(
                                    expression = expression,
                                    result = formatted,
                                    source = com.shohankhan.apexcalculator.core.domain.model.CalculationSource.SCIENTIFIC,
                                    createdAt = System.currentTimeMillis(),
                                ),
                            )
                        }
                    }
                }
            }
            is EvalOutcome.Failure -> {
                _state.update { it.copy(error = outcome.error, committed = null, justEvaluated = false) }
            }
        }
    }

    fun useAnswer() {
        val answer = _state.value.lastAnswer ?: return
        insert(answer)
    }

    fun toggleShift() {
        _state.update { it.copy(shift = !it.shift) }
    }

    fun toggleComplex() {
        _state.update { it.copy(complexMode = !it.complexMode, committed = null, justEvaluated = false).recalculated() }
    }

    fun toggleAngleMode() {
        val next = if (_state.value.angleMode == AngleMode.DEG) AngleMode.RAD else AngleMode.DEG
        _state.update { it.copy(settings = it.settings.copy(angleMode = next), committed = null, justEvaluated = false).recalculated() }
        viewModelScope.launch {
            runCatching { container.settingsRepository.update { it.copy(angleMode = next) } }
        }
    }

    fun openCalculus() {
        _state.update { it.copy(calculusVisible = true, calculus = it.calculus.copy(output = null, error = null)) }
    }

    fun closeCalculus() {
        _state.update { it.copy(calculusVisible = false) }
    }

    fun updateCalculus(transform: (CalculusUiState) -> CalculusUiState) {
        _state.update { it.copy(calculus = transform(it.calculus)) }
    }

    fun computeDerivative() {
        val calculus = _state.value.calculus
        _state.update {
            it.copy(
                calculus = it.calculus.copy(
                    output = null,
                    error = null,
                ),
            )
        }
        runCalcuate { engine ->
            val f = engine(calculus.function)
            val x = calculus.point.toDoubleOrNull() ?: throw IllegalArgumentException("The point must be a number")
            val result = Numerical.derivative(f, x)
            "f′(${calculus.point}) = ${ResultFormatter.compact(result)}"
        }
    }

    fun computeIntegral() {
        val calculus = _state.value.calculus
        _state.update { it.copy(calculus = it.calculus.copy(output = null, error = null)) }
        runCalcuate { engine ->
            val f = engine(calculus.function)
            val a = calculus.lower.toDoubleOrNull() ?: throw IllegalArgumentException("The lower limit must be a number")
            val b = calculus.upper.toDoubleOrNull() ?: throw IllegalArgumentException("The upper limit must be a number")
            if (a == b) throw IllegalArgumentException("The limits must be different")
            val (lo, hi) = if (a < b) a to b else b to a
            val result = Numerical.integrate(f, lo, hi)
            val sign = if (a < b) 1.0 else -1.0
            "∫ from $a to $b = ${ResultFormatter.compact(sign * result)}"
        }
    }

    fun computeRoot() {
        val calculus = _state.value.calculus
        _state.update { it.copy(calculus = it.calculus.copy(output = null, error = null)) }
        runCalcuate { engine ->
            val f = engine(calculus.function)
            val a = calculus.lower.toDoubleOrNull() ?: throw IllegalArgumentException("The lower bound must be a number")
            val b = calculus.upper.toDoubleOrNull() ?: throw IllegalArgumentException("The upper bound must be a number")
            if (a == b) throw IllegalArgumentException("The bounds must be different")
            val (lo, hi) = if (a < b) a to b else b to a
            val result = Numerical.findRoot(f, lo, hi)
            "x ≈ ${ResultFormatter.compact(result.root)} (${result.iterations} iterations)"
        }
    }

    private fun runCalcuate(block: ((String) -> (Double) -> Double) -> String) {
        try {
            val angle = _state.value.settings.angleMode.let {
                if (it == AngleMode.DEG) com.shohankhan.apexcalculator.core.math.AngleUnit.DEG
                else com.shohankhan.apexcalculator.core.math.AngleUnit.RAD
            }
            val message = block { expression -> Numerical.functionOf(expression, angle) }
            _state.update { it.copy(calculus = it.calculus.copy(output = message, error = null)) }
        } catch (exception: Exception) {
            _state.update {
                it.copy(
                    calculus = it.calculus.copy(
                        output = null,
                        error = exception.message ?: "The calculation could not be completed",
                    ),
                )
            }
        }
    }

    fun copyPayload(): String {
        val current = _state.value
        return when {
            current.committed != null -> formatter().format(current.committed)
            else -> current.expression.text
        }
    }

    private fun ScientificUiState.options() = settings.toEvalOptions(complexMode = complexMode)

    private fun ScientificUiState.recalculated(): ScientificUiState {
        val text = expression.text
        if (text.isBlank()) return copy(preview = null, error = null)
        return when (val preview = CalculatorEngine.preview(text, options())) {
            is ExpressionPreview.Value -> copy(preview = preview.value, error = null)
            is ExpressionPreview.Error -> copy(preview = null, error = preview.error)
            ExpressionPreview.Incomplete -> copy(preview = null, error = null)
            ExpressionPreview.Idle -> copy(preview = null, error = null)
        }
    }
}

/** Convenience used by the keypad to know whether a numeric token may follow the caret. */
internal fun ScientificUiState.isTrailingOperator(): Boolean {
    val text = expression.text.trimEnd()
    if (text.isEmpty()) return false
    val last = text.last()
    return last in "+-×÷*/^("
}
