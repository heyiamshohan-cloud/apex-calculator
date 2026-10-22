package com.shohankhan.apexcalculator.core.math

/** Abstract syntax tree produced by [ExpressionParser] and consumed by [Evaluator]. */
sealed interface Expr {

    data class Literal(val value: Num) : Expr

    data class Variable(val name: String) : Expr

    data class Unary(val op: UnaryOp, val operand: Expr) : Expr

    data class Binary(val op: BinaryOp, val left: Expr, val right: Expr) : Expr

    data class Call(val name: String, val args: List<Expr>) : Expr

    data class Postfix(val op: PostfixOp, val operand: Expr) : Expr
}

enum class UnaryOp(val symbol: String) {
    NEGATE("-"),
    PLUS("+"),
}

enum class BinaryOp(val symbol: String, val precedence: Int, val rightAssociative: Boolean) {
    ADD("+", 1, false),
    SUBTRACT("-", 1, false),
    MULTIPLY("×", 2, false),
    DIVIDE("÷", 2, false),
    POWER("^", 4, true),
}

enum class PostfixOp { FACTORIAL, PERCENT }
