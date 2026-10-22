package com.shohankhan.apexcalculator.core.math

enum class TokenType {
    NUMBER,
    IDENT,
    PLUS,
    MINUS,
    STAR,
    SLASH,
    CARET,
    PERCENT,
    BANG,
    LPAREN,
    RPAREN,
    COMMA,
    PIPE,
    END,
}

data class Token(val type: TokenType, val text: String, val position: Int)

/**
 * Tokenizer for the calculator grammar.
 *
 * Accepted symbols: digits, decimal point, scientific "e" notation, identifiers (functions,
 * constants and variables), + - * / ^ % ! ( ) , and | for absolute value. Typographic
 * variants produced by the keypad (× ÷ − √ π ² ³) are normalised before scanning.
 */
class Lexer(private val input: String) {

    fun tokenize(): List<Token> {
        val tokens = ArrayList<Token>()
        var index = 0
        while (index < input.length) {
            val ch = input[index]
            when {
                ch.isWhitespace() -> index++
                ch.isDigit() || (ch == '.' && index + 1 < input.length && input[index + 1].isDigit()) -> {
                    val start = index
                    index = scanNumber(index)
                    tokens += Token(TokenType.NUMBER, input.substring(start, index), start)
                }
                ch.isLetter() || ch == 'π' -> {
                    val start = index
                    if (ch == 'π') {
                        index++
                        tokens += Token(TokenType.IDENT, "pi", start)
                    } else {
                        while (index < input.length && (input[index].isLetterOrDigit() || input[index] == '_')) {
                            index++
                        }
                        val word = input.substring(start, index)
                        // "15% of 200" is a widely used percent idiom; treat "of" as multiplication.
                        tokens += if (word.equals("of", ignoreCase = true)) {
                            Token(TokenType.STAR, "*", start)
                        } else {
                            Token(TokenType.IDENT, word, start)
                        }
                    }
                }
                ch == '√' -> {
                    tokens += Token(TokenType.IDENT, "sqrt", index)
                    index++
                }
                ch == '²' -> {
                    tokens += Token(TokenType.CARET, "^", index)
                    tokens += Token(TokenType.NUMBER, "2", index)
                    index++
                }
                ch == '³' -> {
                    tokens += Token(TokenType.CARET, "^", index)
                    tokens += Token(TokenType.NUMBER, "3", index)
                    index++
                }
                else -> {
                    val type = when (ch) {
                        '+' -> TokenType.PLUS
                        '-' -> TokenType.MINUS
                        '−' -> TokenType.MINUS
                        '–' -> TokenType.MINUS
                        '×' -> TokenType.STAR
                        '·' -> TokenType.STAR
                        '*' -> TokenType.STAR
                        '÷' -> TokenType.SLASH
                        '/' -> TokenType.SLASH
                        '^' -> TokenType.CARET
                        '%' -> TokenType.PERCENT
                        '!' -> TokenType.BANG
                        '(' -> TokenType.LPAREN
                        ')' -> TokenType.RPAREN
                        ',' -> TokenType.COMMA
                        '|' -> TokenType.PIPE
                        else -> throw MathEvalException(
                            MathError.Syntax(index, "Unexpected character \"$ch\""),
                        )
                    }
                    tokens += Token(type, ch.toString(), index)
                    index++
                }
            }
        }
        tokens += Token(TokenType.END, "", input.length)
        return tokens
    }

    private fun scanNumber(start: Int): Int {
        var index = start
        var digits = 0
        var dots = 0
        while (index < input.length && (input[index].isDigit() || input[index] == '.')) {
            if (input[index] == '.') {
                dots++
                if (dots > 1) {
                    throw MathEvalException(MathError.Syntax(index, "A number can only contain one decimal point"))
                }
            } else {
                digits++
            }
            index++
        }
        if (digits == 0) {
            throw MathEvalException(MathError.Syntax(start, "Malformed number"))
        }
        if (index < input.length && (input[index] == 'e' || input[index] == 'E')) {
            val exponentStart = index
            var probe = index + 1
            if (probe < input.length && (input[probe] == '+' || input[probe] == '-')) probe++
            val digitsStart = probe
            while (probe < input.length && input[probe].isDigit()) probe++
            if (probe > digitsStart) {
                index = probe
            } else {
                index = exponentStart
            }
        }
        return index
    }
}
