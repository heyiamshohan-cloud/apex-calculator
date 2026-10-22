package com.shohankhan.apexcalculator.feature.standard

import android.app.Application
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shohankhan.apexcalculator.ApexApplication
import com.shohankhan.apexcalculator.core.domain.model.AppSettings
import com.shohankhan.apexcalculator.core.domain.model.CalculationSource
import com.shohankhan.apexcalculator.core.domain.model.HistoryRecord
import com.shohankhan.apexcalculator.core.domain.model.MemoryState
import com.shohankhan.apexcalculator.core.domain.toEvalOptions
import com.shohankhan.apexcalculator.core.domain.toFormatSettings
import com.shohankhan.apexcalculator.core.math.CalculatorEngine
import com.shohankhan.apexcalculator.core.math.EvalOutcome
import com.shohankhan.apexcalculator.core.math.ExpressionPreview
import com.shohankhan.apexcalculator.core.math.MathError
import com.shohankhan.apexcalculator.core.math.Num
import com.shohankhan.apexcalculator.core.math.NumMath
import com.shohankhan.apexcalculator.core.math.ResultFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StandardUiState(
    val expression: TextFieldValue = TextFieldValue(""),
    val settings: AppSettings = AppSettings(),
    val memory: MemoryState = MemoryState.EMPTY,
    val lastAnswer: String? = null,
    val committed: Num? = null,
    val preview: Num? = null,
    val error: MathError? = null,
    val justEvaluated: Boolean = false,
    val previous: HistoryRecord? = null,
    val undoStack: List<String> = emptyList(),
    val redoStack: List<String> = emptyList(),
) {
    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()
    val hasContent: Boolean get() = expression.text.isNotEmpty()
}

enum class MemoryOp { CLEAR, RECALL, ADD, SUBTRACT, STORE }

class StandardViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as ApexApplication).container
    private val _state = MutableStateFlow(StandardUiState())
    val state: StateFlow<StandardUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            container.settingsRepository.settings.collect { settings ->
                _state.update { it.copy(settings = settings).recalculated() }
            }
        }
        viewModelScope.launch {
            container.memoryRepository.memory.collect { memory ->
                _state.update { it.copy(memory = memory) }
            }
        }
        viewModelScope.launch {
            container.memoryRepository.lastAnswer.collect { answer ->
                _state.update { it.copy(lastAnswer = answer) }
            }
        }
        viewModelScope.launch {
            container.historyRepository.observe().collect { records ->
                _state.update { it.copy(previous = records.firstOrNull()) }
            }
        }
    }

    fun onExpressionChange(value: TextFieldValue) {
        _state.update { current ->
            current.copy(
                expression = value,
                undoStack = (current.undoStack + current.expression.text).takeLast(MAX_UNDO),
                redoStack = emptyList(),
                justEvaluated = false,
            ).recalculated()
        }
    }

    fun insert(text: String) {
        _state.update { current ->
            var base = current
            if (current.justEvaluated) {
                val startsOperator = text.firstOrNull()?.let { it in "+-×÷*/^" } == true
                base = if (startsOperator) {
                    val answer = current.committed?.let { ResultFormatter(current.settings.toFormatSettings()).toInput(it) }
                    current.copy(
                        expression = TextFieldValue(answer ?: "", TextRange((answer ?: "").length)),
                        justEvaluated = false,
                    )
                } else {
                    current.copy(expression = TextFieldValue(""), justEvaluated = false)
                }
            }
            val value = base.expression
            val start = value.selection.min.coerceIn(0, value.text.length)
            val end = value.selection.max.coerceIn(0, value.text.length)
            val updated = value.text.substring(0, start) + text + value.text.substring(end)
            base.copy(
                expression = TextFieldValue(updated, TextRange(start + text.length)),
                undoStack = (base.undoStack + value.text).takeLast(MAX_UNDO),
                redoStack = emptyList(),
            ).recalculated()
        }
    }

    fun onDigit(digit: String) = insert(digit)

    fun onOperator(symbol: String) {
        _state.update { current ->
            if (current.justEvaluated && current.committed != null) {
                val answer = ResultFormatter(current.settings.toFormatSettings()).toInput(current.committed) ?: ""
                return@update current.copy(
                    expression = TextFieldValue(answer + symbol, TextRange(answer.length + symbol.length)),
                    justEvaluated = false,
                    undoStack = (current.undoStack + current.expression.text).takeLast(MAX_UNDO),
                ).recalculated()
            }
            val text = current.expression.text
            if (text.isEmpty()) {
                if (symbol != "-") return@update current
                return@update current.copy(
                    expression = TextFieldValue("-", TextRange(1)),
                    justEvaluated = false,
                ).recalculated()
            }
            val lastChar = text.last()
            val trailingOperator = lastChar in "+-×÷*/^"
            val newText = when {
                trailingOperator && lastChar != '(' -> text.dropLast(1) + symbol
                lastChar == '(' && symbol == "-" -> text + symbol
                lastChar == '(' -> text
                else -> text + symbol
            }
            current.copy(
                expression = TextFieldValue(newText, TextRange(newText.length)),
                undoStack = (current.undoStack + text).takeLast(MAX_UNDO),
                redoStack = emptyList(),
                justEvaluated = false,
            ).recalculated()
        }
    }

    /** Inserts "(" or ")" depending on the current nesting balance. */
    fun onParen() {
        val text = _state.value.expression.text
        val opens = text.count { it == '(' }
        val closes = text.count { it == ')' }
        insert(if (opens > closes) ")" else "(")
    }

    fun onDecimal() {
        _state.update { current ->
            val text = current.expression.text
            val start = current.expression.selection.min
            val segmentStart = text.take(start).lastIndexOfAny(charArrayOf('+', '-', '×', '÷', '*', '/', '(', '^')).let { if (it < 0) 0 else it + 1 }
            val segmentEnd = text.indexOfAny(charArrayOf('+', '-', '×', '÷', '*', '/', ')', '^'), segmentStart).let { if (it < 0) text.length else it }
            val segment = text.substring(segmentStart.coerceAtMost(segmentEnd), segmentEnd)
            if (segment.contains('.')) current else {
                val value = if (segment.isEmpty()) "0." else "."
                val updated = text.substring(0, start) + value + text.substring(start)
                current.copy(
                    expression = TextFieldValue(updated, TextRange(start + value.length)),
                    undoStack = (current.undoStack + text).takeLast(MAX_UNDO),
                    redoStack = emptyList(),
                ).recalculated()
            }
        }
    }

    fun onToggleSign() {
        _state.update { current ->
            val value = current.expression
            val text = value.text
            if (text.isEmpty()) {
                return@update current.copy(expression = TextFieldValue("-", TextRange(1))).recalculated()
            }
            val cursor = value.selection.min.coerceIn(0, text.length)
            var index = cursor - 1
            while (index >= 0 && (text[index].isDigit() || text[index] == '.')) index--
            val numberStart = index + 1
            if (numberStart >= cursor) {
                val updated = text.substring(0, cursor) + "-" + text.substring(cursor)
                return@update current.copy(
                    expression = TextFieldValue(updated, TextRange(cursor + 1)),
                    undoStack = (current.undoStack + text).takeLast(MAX_UNDO),
                ).recalculated()
            }
            val updated = if (numberStart > 0 && text[numberStart - 1] == '-') {
                text.removeRange(numberStart - 1, numberStart)
            } else {
                text.substring(0, numberStart) + "-" + text.substring(numberStart)
            }
            current.copy(
                expression = TextFieldValue(updated, TextRange(updated.length)),
                undoStack = (current.undoStack + text).takeLast(MAX_UNDO),
                redoStack = emptyList(),
            ).recalculated()
        }
    }

    fun onBackspace() {
        _state.update { current ->
            val value = current.expression
            val start = value.selection.min
            val end = value.selection.max
            val text = value.text
            val updated = when {
                start != end -> text.removeRange(start, end)
                start > 0 -> text.removeRange(start - 1, start)
                else -> return@update current
            }
            val cursor = if (start != end) start else (start - 1).coerceAtLeast(0)
            current.copy(
                expression = TextFieldValue(updated, TextRange(cursor)),
                undoStack = (current.undoStack + text).takeLast(MAX_UNDO),
                redoStack = emptyList(),
                justEvaluated = false,
            ).recalculated()
        }
    }

    fun onClear() {
        _state.update { current ->
            current.copy(
                expression = TextFieldValue(""),
                committed = null,
                preview = null,
                error = null,
                justEvaluated = false,
                undoStack = (current.undoStack + current.expression.text).takeLast(MAX_UNDO),
                redoStack = emptyList(),
            )
        }
    }

    fun onUndo() {
        _state.update { current ->
            val previous = current.undoStack.lastOrNull() ?: return@update current
            current.copy(
                expression = TextFieldValue(previous, TextRange(previous.length)),
                undoStack = current.undoStack.dropLast(1),
                redoStack = current.redoStack + current.expression.text,
                justEvaluated = false,
            ).recalculated()
        }
    }

    fun onRedo() {
        _state.update { current ->
            val next = current.redoStack.lastOrNull() ?: return@update current
            current.copy(
                expression = TextFieldValue(next, TextRange(next.length)),
                undoStack = current.undoStack + current.expression.text,
                redoStack = current.redoStack.dropLast(1),
                justEvaluated = false,
            ).recalculated()
        }
    }

    fun onEquals() {
        val current = _state.value
        val text = current.expression.text
        if (text.isBlank()) return
        when (val outcome = CalculatorEngine.evaluate(text, options(current))) {
            is EvalOutcome.Success -> {
                val formatted = ResultFormatter(current.settings.toFormatSettings()).format(outcome.value)
                val reusable = ResultFormatter(current.settings.toFormatSettings()).toInput(outcome.value)
                _state.update {
                    it.copy(
                        committed = outcome.value,
                        preview = null,
                        error = null,
                        justEvaluated = true,
                    )
                }
                container.memoryRepository.rememberAnswer(reusable)
                if (current.settings.autoSaveHistory) {
                    viewModelScope.launch {
                        runCatching {
                            container.historyRepository.add(
                                HistoryRecord(
                                    expression = text,
                                    result = formatted,
                                    source = CalculationSource.STANDARD,
                                    createdAt = System.currentTimeMillis(),
                                ),
                            )
                        }
                    }
                }
            }
            is EvalOutcome.Failure -> {
                _state.update {
                    it.copy(committed = null, preview = null, error = outcome.error, justEvaluated = false)
                }
            }
        }
    }

    fun onMemory(op: MemoryOp) {
        val current = _state.value
        val formatter = ResultFormatter(current.settings.toFormatSettings())
        val currentValue: Num? = current.committed ?: current.preview
        when (op) {
            MemoryOp.CLEAR -> container.memoryRepository.clearMemory()
            MemoryOp.STORE -> {
                val input = currentValue?.let { formatter.toInput(it) }
                if (input != null) container.memoryRepository.setMemory(input)
            }
            MemoryOp.RECALL -> {
                val stored = current.memory.text ?: return
                insert(stored)
            }
            MemoryOp.ADD, MemoryOp.SUBTRACT -> {
                val stored = current.memory.text
                val value = currentValue ?: return
                if (stored.isNullOrBlank()) {
                    val input = formatter.toInput(value) ?: return
                    container.memoryRepository.setMemory(input)
                    return
                }
                val storedValue = CalculatorEngine.evaluateOrNull(stored, options(current)) ?: return
                val result = if (op == MemoryOp.ADD) {
                    NumMath.add(storedValue, value)
                } else {
                    NumMath.subtract(storedValue, value)
                }
                container.memoryRepository.setMemory(formatter.toInput(result))
            }
        }
    }

    fun useLastAnswer() {
        currentAnswerText()?.let { insert(it) }
    }

    private fun currentAnswerText(): String? {
        val answer = _state.value.lastAnswer ?: return null
        return answer
    }

    private fun options(state: StandardUiState) = state.settings.toEvalOptions(
        variables = buildMap {
            state.lastAnswer?.let { put("ans", CalculatorEngine.evaluateOrNull(it, state.settings.toEvalOptions()) ?: Num.of(0)) }
        },
    )

    private fun StandardUiState.recalculated(): StandardUiState {
        if (expression.text.isBlank()) {
            return copy(preview = null, error = null, committed = null)
        }
        return when (val result = CalculatorEngine.preview(expression.text, options(this))) {
            is ExpressionPreview.Value -> copy(preview = result.value, error = null)
            is ExpressionPreview.Error -> copy(preview = null, error = result.error)
            ExpressionPreview.Incomplete -> copy(preview = null, error = null)
            ExpressionPreview.Idle -> copy(preview = null, error = null)
        }
    }

    companion object {
        private const val MAX_UNDO = 80
    }
}
