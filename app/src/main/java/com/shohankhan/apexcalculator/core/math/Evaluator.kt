package com.shohankhan.apexcalculator.core.math

import kotlin.random.Random

data class EvalOptions(
    val angleUnit: AngleUnit = AngleUnit.DEG,
    val complexMode: Boolean = false,
    val variables: Map<String, Num> = emptyMap(),
    val random: Random = Random.Default,
    val maxNodes: Int = 4096,
)

/**
 * Deterministic tree-walking evaluator.
 *
 * Percentage convention (documented and shared by every workspace):
 *  - `a + p%` evaluates to `a + a·p/100` (200 + 10% = 220)
 *  - `a − p%` evaluates to `a − a·p/100` (200 − 10% = 180)
 *  - in every other position `p%` simply means `p/100`, so 200 × 10% = 20.
 */
class Evaluator(private val options: EvalOptions) {

    private val context = EvalContext(options.angleUnit, options.complexMode, options.random)
    private var nodeCount = 0
    private var depth = 0

    fun evaluate(expression: Expr): Num {
        nodeCount = 0
        depth = 0
        val value = eval(expression)
        if (!options.complexMode && value is Num.Cpx && !value.c.isReal) {
            throw MathEvalException(
                MathError.Domain(
                    "The result is complex. Turn on complex mode to calculate with complex numbers.",
                ),
            )
        }
        if (value is Num.Real && !value.d.isFinite()) {
            throw if (value.d.isNaN()) {
                MathEvalException(MathError.Domain("The result is not a real number"))
            } else {
                MathEvalException(MathError.Overflow("The result is too large to represent"))
            }
        }
        return value
    }

    private fun eval(node: Expr): Num {
        nodeCount++
        if (nodeCount > options.maxNodes) {
            throw MathEvalException(MathError.Limit("The expression is too large to evaluate"))
        }
        depth++
        if (depth > MAX_DEPTH) {
            throw MathEvalException(MathError.Limit("The expression is nested too deeply"))
        }
        try {
            return when (node) {
                is Expr.Literal -> node.value
                is Expr.Variable -> resolveVariable(node.name)
                is Expr.Unary -> when (node.op) {
                    UnaryOp.NEGATE -> NumMath.negate(eval(node.operand))
                    UnaryOp.PLUS -> eval(node.operand)
                }
                is Expr.Binary -> evalBinary(node)
                is Expr.Call -> evalCall(node)
                is Expr.Postfix -> evalPostfix(node)
            }
        } finally {
            depth--
        }
    }

    private fun evalBinary(node: Expr.Binary): Num {
        if (node.op == BinaryOp.ADD || node.op == BinaryOp.SUBTRACT) {
            val right = node.right
            if (right is Expr.Postfix && right.op == PostfixOp.PERCENT) {
                val base = eval(node.left)
                val percent = eval(right.operand)
                val delta = NumMath.divide(NumMath.multiply(base, percent), Num.of(100))
                return if (node.op == BinaryOp.ADD) {
                    NumMath.add(base, delta)
                } else {
                    NumMath.subtract(base, delta)
                }
            }
        }
        val left = eval(node.left)
        val right = eval(node.right)
        return when (node.op) {
            BinaryOp.ADD -> NumMath.add(left, right)
            BinaryOp.SUBTRACT -> NumMath.subtract(left, right)
            BinaryOp.MULTIPLY -> NumMath.multiply(left, right)
            BinaryOp.DIVIDE -> NumMath.divide(left, right)
            BinaryOp.POWER -> {
                if (NumMath.isZero(left) && NumMath.isNegative(right)) {
                    throw MathEvalException(MathError.DivisionByZero("Zero raised to a negative power is undefined"))
                }
                NumMath.pow(left, right, options.complexMode)
            }
        }
    }

    private fun evalCall(node: Expr.Call): Num {
        val spec = MathFunctions.lookup(node.name)
            ?: throw MathEvalException(MathError.UnknownIdentifier(node.name))
        spec.requireArity(node.args.size)
        val arguments = node.args.map { eval(it) }
        return spec.apply(arguments, context)
    }

    private fun evalPostfix(node: Expr.Postfix): Num {
        val value = eval(node.operand)
        return when (node.op) {
            PostfixOp.FACTORIAL -> NumMath.factorial(value)
            PostfixOp.PERCENT -> NumMath.divide(value, Num.of(100))
        }
    }

    private fun resolveVariable(name: String): Num {
        val key = name.lowercase()
        options.variables[key]?.let { return it }
        return when (key) {
            "pi" -> Num.Real(Math.PI)
            "e" -> Num.Real(Math.E)
            "tau" -> Num.Real(2 * Math.PI)
            "phi" -> Num.Real((1 + Math.sqrt(5.0)) / 2)
            "ans" -> options.variables["ans"] ?: Num.of(0)
            "i" -> if (options.complexMode) {
                Num.Cpx(ComplexNumber(0.0, 1.0))
            } else {
                throw MathEvalException(
                    MathError.Domain("The imaginary unit i needs complex mode. Enable it to continue."),
                )
            }
            else -> throw MathEvalException(MathError.UnknownIdentifier(name))
        }
    }

    companion object {
        const val MAX_DEPTH = 128
    }
}
