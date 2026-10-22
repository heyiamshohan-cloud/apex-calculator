package com.shohankhan.apexcalculator.core.math

/**
 * Typed mathematical failures. The engine never returns a magic value for an invalid
 * operation; every failure is one of these cases with a human readable explanation.
 */
sealed class MathError(val message: String) {

    data class Syntax(val position: Int, val detail: String) :
        MathError("$detail${if (position >= 0) " (position ${position + 1})" else ""}")

    data class Incomplete(val detail: String) : MathError(detail)

    data class DivisionByZero(val detail: String = "Division by zero is undefined") : MathError(detail)

    data class Domain(val detail: String) : MathError(detail)

    data class Overflow(val detail: String) : MathError(detail)

    data class Undefined(val detail: String) : MathError(detail)

    data class Limit(val detail: String) : MathError(detail)

    data class UnknownIdentifier(val name: String) : MathError("Unknown name \"$name\"")

    data class Arity(val function: String, val expected: String) :
        MathError("$function expects $expected argument(s)")

    data class Unsupported(val detail: String) : MathError(detail)
}

class MathEvalException(val error: MathError) : Exception(error.message)
