package com.shohankhan.apexcalculator.core.math

import com.shohankhan.apexcalculator.core.domain.model.NumberFormatMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

/**
 * Regression suite for the shared expression engine. These are the expected results required by
 * the product specification plus a wider set of behaviour checks.
 *
 * Supported syntax: decimal and scientific literals, + - * / ^, unary minus, parentheses,
 * implicit multiplication, postfix ! and %, and the function registry in [MathFunctions].
 * Numeric tolerance for transcendental comparisons is 1e-12 relative.
 */
class ExpressionEngineTest {

    private val tolerance = 1e-12

    private fun value(expression: String, degrees: Boolean = true, complex: Boolean = false): Num =
        CalculatorEngine.evaluateOrThrow(
            expression,
            EvalOptions(
                angleUnit = if (degrees) AngleUnit.DEG else AngleUnit.RAD,
                complexMode = complex,
            ),
        )

    private fun number(expression: String, degrees: Boolean = true): Double =
        NumMath.toDouble(value(expression, degrees))

    private fun text(
        expression: String,
        settings: FormatSettings = FormatSettings(),
        degrees: Boolean = true,
    ): String = ResultFormatter(settings).format(value(expression, degrees))

    private fun assertNearly(expected: Double, expression: String) {
        val actual = number(expression)
        assertEquals("$expression", expected, actual, tolerance * maxOf(1.0, kotlin.math.abs(expected)))
    }

    private fun assertFails(expression: String, degrees: Boolean = true): MathError {
        val outcome = CalculatorEngine.evaluate(
            expression,
            EvalOptions(angleUnit = if (degrees) AngleUnit.DEG else AngleUnit.RAD),
        )
        assertTrue("Expected $expression to fail", outcome is EvalOutcome.Failure)
        return (outcome as EvalOutcome.Failure).error
    }

    @Test
    fun `operator precedence is respected`() {
        assertEquals(14.0, number("2 + 3 × 4"), 0.0)
        assertEquals(20.0, number("(2 + 3) × 4"), 0.0)
        assertEquals(2.5, number("10 ÷ 4"), 0.0)
        assertEquals(3.0, number("-5 + 8"), 0.0)
    }

    @Test
    fun `powers are right associative and unary minus binds looser`() {
        assertEquals(1024.0, number("2^10"), 0.0)
        assertEquals(512.0, number("2^3^2"), 0.0)
        assertEquals(-4.0, number("-2^2"), 0.0)
        assertEquals(0.25, number("2^-2"), 0.0)
        assertEquals(2.0, number("4^(1/2)"), tolerance)
    }

    @Test
    fun `roots logarithms and trigonometry`() {
        assertEquals(12.0, number("sqrt(144)"), 0.0)
        assertEquals(3.0, number("log10(1000)"), tolerance)
        assertNearly(1.0, "ln(e)")
        assertNearly(0.5, "sin(30)")
        assertNearly(0.5, "cos(60)")
        assertNearly(1.0, "tan(45)")
        assertNearly(30.0, "asin(0.5)")
        assertNearly(60.0, "acos(0.5)")
        assertNearly(1.0, "sin(90)")
        assertNearly(1.0, "cos(0)")
        assertNearly(1.0, "sinh(0) + cosh(0)")
        assertEquals(2.0, number("log2(4)"), tolerance)
        assertEquals(2.0, number("log(100, 10)"), tolerance)
    }

    @Test
    fun `radian mode changes trigonometric interpretation`() {
        assertNearly(0.0, "sin(0)")
        assertEquals(-0.7568024953079282, number("sin(4)", degrees = false), 1e-12)
        assertEquals(0.0697564737441253, number("sin(4)"), 1e-12)
    }

    @Test
    fun `factorials combinatorics and number theory`() {
        assertEquals(120.0, number("5!"), 0.0)
        assertEquals(1.0, number("0!"), 0.0)
        assertEquals(10.0, number("ncr(5, 2)"), 0.0)
        assertEquals(20.0, number("npr(5, 2)"), 0.0)
        assertEquals(6.0, number("gcd(48, 18)"), 0.0)
        assertEquals(36.0, number("lcm(12, 18)"), 0.0)
        assertEquals(3.0, number("mod(15, 4)"), 0.0)
        assertEquals(-3.0, number("rem(-15, 4)"), 0.0)
    }

    @Test
    fun `fractions stay exact`() {
        val result = value("1/3 + 1/6")
        assertTrue(result is Num.Rat)
        val rational = (result as Num.Rat).r
        assertEquals("1", rational.numerator.toString())
        assertEquals("2", rational.denominator.toString())
        assertEquals("1/2", ResultFormatter(FormatSettings(fractionDisplay = true)).format(result))
        assertEquals("0.5", ResultFormatter(FormatSettings()).format(result))
    }

    @Test
    fun `percentage conventions`() {
        assertEquals(30.0, number("15% of 200"), 0.0)
        assertEquals(30.0, number("200 × 15%"), 0.0)
        assertEquals(220.0, number("200 + 10%"), 0.0)
        assertEquals(180.0, number("200 - 10%"), 0.0)
        assertEquals(0.5, number("50%"), 0.0)
        assertEquals(110.0, number("100 + 10% + 0%"), 0.0)
    }

    @Test
    fun `implicit multiplication and constants`() {
        assertEquals(6.0, number("2(3)"), 0.0)
        assertEquals(12.0, number("3(4)"), 0.0)
        assertEquals(2.0, number("2π/π"), tolerance)
        assertNearly(4 * Math.PI, "4π")
        assertEquals(9.0, number("(1+2)(3)"), 0.0)
    }

    @Test
    fun `decimal arithmetic is exact`() {
        assertEquals("0.3", text("0.1 + 0.2"))
        assertEquals("1.5", text("0.5 × 3"))
        assertEquals("2.5", text("10 ÷ 4"))
        assertEquals("1,024", text("2^10"))
    }

    @Test
    fun `large and small values use scientific notation`() {
        assertEquals("1E20", text("10^20"))
        assertEquals("1E-10", text("10^-10"))
        assertTrue(text("1000000").isNotEmpty())
        assertEquals("1,000,000", text("10^6"))
    }

    @Test
    fun `formatting options are honoured`() {
        val fixed = FormatSettings(decimalPlaces = 2, numberFormat = NumberFormatMode.FIXED)
        assertEquals("3.14", ResultFormatter(fixed).format(value("pi")))
        val grouping = FormatSettings(thousandsSeparator = true)
        assertEquals("123,456", ResultFormatter(grouping).format(value("123456")))
        val noGrouping = FormatSettings(thousandsSeparator = false)
        assertEquals("123456", ResultFormatter(noGrouping).format(value("123456")))
    }

    @Test
    fun `division by zero is reported`() {
        assertTrue(assertFails("1/0") is MathError.DivisionByZero)
        assertTrue(assertFails("1/(2-2)") is MathError.DivisionByZero)
    }

    @Test
    fun `domain errors are reported`() {
        assertTrue(assertFails("ln(0)") is MathError.Undefined)
        assertTrue(assertFails("log(-5)") is MathError.Domain)
        assertTrue(assertFails("sqrt(-4)") is MathError.Domain)
        assertTrue(assertFails("(-2)!") is MathError.Domain)
        assertTrue(assertFails("tan(90)") is MathError.Undefined)
        assertTrue(assertFails("asin(2)") is MathError.Domain)
        assertTrue(assertFails("acosh(0)") is MathError.Domain)
    }

    @Test
    fun `complex mode enables complex results`() {
        val root = CalculatorEngine.evaluateOrThrow("sqrt(-4)", EvalOptions(complexMode = true))
        assertTrue(root is Num.Cpx)
        assertEquals("2i", ResultFormatter(FormatSettings()).format(root))
        val product = CalculatorEngine.evaluateOrThrow("(1+2i)", EvalOptions(complexMode = true))
        assertNotNull(product)
    }

    @Test
    fun `incomplete input is distinguished from invalid input`() {
        assertTrue(CalculatorEngine.preview("2+") is ExpressionPreview.Incomplete)
        assertTrue(CalculatorEngine.preview("(2+3") is ExpressionPreview.Incomplete)
        assertTrue(CalculatorEngine.preview("2++") is ExpressionPreview.Incomplete)
        assertTrue(CalculatorEngine.preview("2$") is ExpressionPreview.Error)
        assertTrue(CalculatorEngine.preview("") is ExpressionPreview.Idle)
    }

    @Test
    fun `very large integers stay exact`() {
        val result = value("2^64")
        assertEquals("18446744073709551616", (result as Num.IntVal).v.toString())
        val wide = FormatSettings(scientificThreshold = 25)
        assertEquals("18,446,744,073,709,551,616", ResultFormatter(wide).format(result))
    }

    @Test
    fun `expression evaluation is deterministic`() {
        val first = text("sin(37)^2 + cos(37)^2")
        repeat(5) {
            assertEquals(first, text("sin(37)^2 + cos(37)^2"))
        }
    }

    @Test
    fun `scientific notation input is parsed`() {
        assertEquals(1500.0, number("1.5e3"), 0.0)
        assertEquals(0.0025, number("2.5E-3"), 0.0)
    }

    @Test
    fun `big decimal precision is preserved`() {
        val result = value("12345678901234567890 + 1")
        assertEquals(BigDecimal("12345678901234567891"), (result as Num.IntVal).v.toBigDecimal())
    }
}
