package com.shohankhan.apexcalculator.core.math

import java.math.BigDecimal
import java.math.BigInteger

/**
 * Recursive-descent parser.
 *
 * Grammar (highest binding last):
 *   expression   := additive
 *   additive     := multiplicative (("+" | "-") multiplicative)*
 *   multiplicative := unary (("*" | "/") unary | implicit unary)*
 *   unary        := ("+" | "-") unary | power
 *   power        := postfix ("^" unary)?          // right associative: 2^3^2 = 512
 *   postfix      := primary ("!" | "%")*
 *   primary      := NUMBER | IDENT | IDENT "(" args ")" | "(" expression ")" | "|" expression "|"
 *
 * Implicit multiplication is supported between adjacent factors, e.g. 2π, 3(4+1), (1+2)(3+4).
 * Unary minus binds looser than exponentiation, so -2^2 evaluates to -4.
 */
class ExpressionParser(private val tokens: List<Token>) {

    private var index = 0
    private var depth = 0

    fun parse(): Expr {
        if (peek().type == TokenType.END) {
            throw MathEvalException(MathError.Incomplete("Enter an expression"))
        }
        val expression = parseAdditive()
        val token = peek()
        if (token.type != TokenType.END) {
            throw MathEvalException(
                MathError.Syntax(token.position, "Unexpected \"${token.text}\""),
            )
        }
        return expression
    }

    private fun parseAdditive(): Expr {
        var left = parseMultiplicative()
        while (true) {
            when (peek().type) {
                TokenType.PLUS -> {
                    advance()
                    left = Expr.Binary(BinaryOp.ADD, left, parseMultiplicative())
                }
                TokenType.MINUS -> {
                    advance()
                    left = Expr.Binary(BinaryOp.SUBTRACT, left, parseMultiplicative())
                }
                else -> return left
            }
        }
    }

    private fun parseMultiplicative(): Expr {
        var left = parseUnary()
        while (true) {
            when (peek().type) {
                TokenType.STAR -> {
                    advance()
                    left = Expr.Binary(BinaryOp.MULTIPLY, left, parseUnary())
                }
                TokenType.SLASH -> {
                    advance()
                    left = Expr.Binary(BinaryOp.DIVIDE, left, parseUnary())
                }
                TokenType.NUMBER, TokenType.IDENT, TokenType.LPAREN, TokenType.PIPE -> {
                    // Implicit multiplication: 2π, 3(4+1), (1+2)(3+4)
                    left = Expr.Binary(BinaryOp.MULTIPLY, left, parseUnary())
                }
                else -> return left
            }
        }
    }

    private fun parseUnary(): Expr {
        return when (peek().type) {
            TokenType.MINUS -> {
                advance()
                Expr.Unary(UnaryOp.NEGATE, parseUnary())
            }
            TokenType.PLUS -> {
                advance()
                Expr.Unary(UnaryOp.PLUS, parseUnary())
            }
            else -> parsePower()
        }
    }

    private fun parsePower(): Expr {
        val base = parsePostfix()
        if (peek().type == TokenType.CARET) {
            advance()
            val exponent = parseUnary()
            return Expr.Binary(BinaryOp.POWER, base, exponent)
        }
        return base
    }

    private fun parsePostfix(): Expr {
        var expression = parsePrimary()
        while (true) {
            when (peek().type) {
                TokenType.BANG -> {
                    advance()
                    expression = Expr.Postfix(PostfixOp.FACTORIAL, expression)
                }
                TokenType.PERCENT -> {
                    advance()
                    expression = Expr.Postfix(PostfixOp.PERCENT, expression)
                }
                else -> return expression
            }
        }
    }

    private fun parsePrimary(): Expr {
        val token = peek()
        return when (token.type) {
            TokenType.NUMBER -> {
                advance()
                Expr.Literal(parseNumber(token.text, token.position))
            }
            TokenType.IDENT -> {
                advance()
                if (peek().type == TokenType.LPAREN) {
                    parseCall(token.text, token.position)
                } else {
                    Expr.Variable(token.text)
                }
            }
            TokenType.LPAREN -> {
                enter(token.position)
                advance()
                val inner = parseAdditive()
                expectClosing(TokenType.RPAREN, token.position)
                depth--
                inner
            }
            TokenType.PIPE -> {
                enter(token.position)
                advance()
                val inner = parseAdditive()
                expectClosing(TokenType.PIPE, token.position)
                depth--
                Expr.Call("abs", listOf(inner))
            }
            TokenType.END -> throw MathEvalException(MathError.Incomplete("Finish the expression"))
            else -> throw MathEvalException(
                MathError.Syntax(token.position, "Unexpected \"${token.text}\""),
            )
        }
    }

    private fun parseCall(name: String, position: Int): Expr {
        enter(position)
        advance() // consume "("
        val arguments = ArrayList<Expr>()
        if (peek().type == TokenType.RPAREN) {
            advance()
            depth--
            return Expr.Call(name, arguments)
        }
        while (true) {
            arguments += parseAdditive()
            when (peek().type) {
                TokenType.COMMA -> advance()
                TokenType.RPAREN -> {
                    advance()
                    depth--
                    return Expr.Call(name, arguments)
                }
                TokenType.END -> throw MathEvalException(
                    MathError.Incomplete("Close the parenthesis after \"$name\""),
                )
                else -> throw MathEvalException(
                    MathError.Syntax(peek().position, "Unexpected \"${peek().text}\" in \"$name\""),
                )
            }
        }
    }

    private fun enter(position: Int) {
        depth++
        if (depth > MAX_DEPTH) {
            throw MathEvalException(MathError.Limit("The expression is nested too deeply (limit $MAX_DEPTH)"))
        }
    }

    private fun expectClosing(type: TokenType, openPosition: Int) {
        val token = peek()
        if (token.type != type) {
            if (token.type == TokenType.END) {
                throw MathEvalException(
                    MathError.Incomplete("Close the bracket opened at position ${openPosition + 1}"),
                )
            }
            throw MathEvalException(MathError.Syntax(token.position, "Unexpected \"${token.text}\""))
        }
        advance()
    }

    private fun peek(): Token = tokens[index.coerceAtMost(tokens.lastIndex)]

    private fun advance() {
        if (index < tokens.lastIndex) index++
    }

    companion object {
        const val MAX_DEPTH = 96

        fun parseNumber(text: String, position: Int): Num {
            val normalised = when {
                text.endsWith(".") -> text + "0"
                text.startsWith(".") -> "0$text"
                else -> text
            }
            return try {
                if (normalised.none { it == '.' || it == 'e' || it == 'E' }) {
                    Num.IntVal(BigInteger(normalised))
                } else {
                    val decimal = BigDecimal(normalised)
                    if (decimal.scale() > 5000 || decimal.scale() < -5000) {
                        Num.Real(decimal.toDouble())
                    } else {
                        val stripped = decimal.stripTrailingZeros()
                        Num.Dec(if (stripped.scale() < 0) stripped.setScale(0) else stripped)
                    }
                }
            } catch (exception: NumberFormatException) {
                throw MathEvalException(MathError.Syntax(position, "Invalid number \"$text\""))
            }
        }
    }
}
