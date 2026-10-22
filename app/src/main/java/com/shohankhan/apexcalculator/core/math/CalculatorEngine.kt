package com.shohankhan.apexcalculator.core.math

sealed interface EvalOutcome {
    data class Success(val value: Num) : EvalOutcome

    data class Failure(val error: MathError) : EvalOutcome
}

/** Live preview state shown under the expression while the user types. */
sealed interface ExpressionPreview {
    data object Idle : ExpressionPreview

    data object Incomplete : ExpressionPreview

    data class Value(val value: Num) : ExpressionPreview

    data class Error(val error: MathError) : ExpressionPreview
}

/**
 * Single entry point for expression parsing and evaluation. Nothing else in the application
 * is allowed to evaluate user input, which keeps semantics identical in every workspace.
 */
object CalculatorEngine {

    fun parse(expression: String): Expr {
        val tokens = Lexer(expression).tokenize()
        return ExpressionParser(tokens).parse()
    }

    fun evaluate(expression: String, options: EvalOptions = EvalOptions()): EvalOutcome {
        if (expression.isBlank()) {
            return EvalOutcome.Failure(MathError.Incomplete("Enter an expression"))
        }
        return try {
            EvalOutcome.Success(Evaluator(options).evaluate(parse(expression)))
        } catch (exception: MathEvalException) {
            EvalOutcome.Failure(exception.error)
        }
    }

    fun evaluateOrThrow(expression: String, options: EvalOptions = EvalOptions()): Num {
        if (expression.isBlank()) throw MathEvalException(MathError.Incomplete("Enter an expression"))
        return Evaluator(options).evaluate(parse(expression))
    }

    /** Evaluates and returns null when the expression is invalid. */
    fun evaluateOrNull(expression: String, options: EvalOptions = EvalOptions()): Num? =
        (evaluate(expression, options) as? EvalOutcome.Success)?.value

    fun preview(expression: String, options: EvalOptions = EvalOptions()): ExpressionPreview {
        if (expression.isBlank()) return ExpressionPreview.Idle
        return when (val outcome = evaluate(expression, options)) {
            is EvalOutcome.Success -> ExpressionPreview.Value(outcome.value)
            is EvalOutcome.Failure -> when (val error = outcome.error) {
                is MathError.Incomplete -> ExpressionPreview.Incomplete
                else -> ExpressionPreview.Error(error)
            }
        }
    }

    /** True when the trailing token can not end an expression, used to avoid noisy previews. */
    fun looksIncomplete(expression: String): Boolean {
        val trimmed = expression.trimEnd()
        if (trimmed.isEmpty()) return true
        val last = trimmed.last()
        return last in "+-×÷*/^−(" || last == ','
    }
}
