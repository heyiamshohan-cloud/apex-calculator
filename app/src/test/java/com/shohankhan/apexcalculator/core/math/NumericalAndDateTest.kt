package com.shohankhan.apexcalculator.core.math

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** Numerical calculus: derivatives, integrals, root finding and tables. */
class NumericalTest {

    private val unit = AngleUnit.RAD

    @Test
    fun `derivative of x squared at 3 is 6`() {
        val f = Numerical.functionOf("x^2", unit)
        assertEquals(6.0, Numerical.derivative(f, 3.0), 1e-6)
    }

    @Test
    fun `derivative of sine at zero is one`() {
        val f = Numerical.functionOf("sin(x)", unit)
        assertEquals(1.0, Numerical.derivative(f, 0.0), 1e-6)
    }

    @Test
    fun `definite integral of x squared from 0 to 1`() {
        val f = Numerical.functionOf("x^2", unit)
        assertEquals(1.0 / 3.0, Numerical.integrate(f, 0.0, 1.0), 1e-7)
    }

    @Test
    fun `integral is orientation aware`() {
        val f = Numerical.functionOf("x", unit)
        assertEquals(-0.5, Numerical.integrate(f, 1.0, 0.0), 1e-9)
    }

    @Test
    fun `root finding locates pi over two`() {
        val f = Numerical.functionOf("cos(x)", unit)
        val result = Numerical.findRoot(f, 0.0, 3.0)
        assertEquals(Math.PI / 2.0, result.root, 1e-9)
        assertTrue(result.iterations > 0)
    }

    @Test
    fun `root finding reports when no sign change exists`() {
        val f = Numerical.functionOf("x^2 + 1", unit)
        try {
            Numerical.findRoot(f, -1.0, 1.0)
            throw AssertionError("Expected a failure")
        } catch (expected: IllegalArgumentException) {
            // expected
        } catch (expected: MathEvalException) {
            // expected
        }
    }

    @Test
    fun `function table covers the requested range`() {
        val f = Numerical.functionOf("2*x", unit)
        val rows = Numerical.table(f, 0.0, 4.0, 1.0)
        assertEquals(5, rows.size)
        assertEquals(0.0, rows.first().x, 1e-12)
        assertEquals(8.0, rows.last().y, 1e-12)
        assertEquals(2.0, rows[1].y, 1e-12)
    }

    @Test
    fun `degree mode is respected by the engine functions`() {
        val f = Numerical.functionOf("sin(x)", AngleUnit.DEG)
        assertEquals(0.5, f(30.0), 1e-12)
        assertEquals(0.5, Numerical.derivative(f, 30.0) * 180.0 / Math.PI, 1e-9)
    }
}

/** Offline calendar arithmetic. */
class DateMathTest {

    private fun date(text: String): LocalDate = DateMath.parseDate(text)

    @Test
    fun `parses iso dates and rejects malformed text`() {
        assertEquals(LocalDate.of(2024, 2, 29), date("2024-02-29"))
        try {
            date("29/02/2024")
            throw AssertionError("Expected a rejection")
        } catch (expected: MathEvalException) {
            // expected
        }
    }

    @Test
    fun `day difference is exact`() {
        val difference = DateMath.difference(date("2024-01-01"), date("2024-01-31"))
        assertEquals(30L, difference.totalDays)
        assertEquals(4L, difference.weeks)
        assertEquals(22L, difference.weekdays)
    }

    @Test
    fun `month components follow calendar arithmetic`() {
        val difference = DateMath.difference(date("2023-01-15"), date("2024-03-20"))
        assertEquals(1, difference.yearsComponent)
        assertEquals(2, difference.monthsComponent)
        assertEquals(5, difference.daysComponent)
    }

    @Test
    fun `weekday counting is half open and orientation independent`() {
        // 2024-01-01 is a Monday, 2024-01-08 is the next Monday: five working days.
        assertEquals(5L, DateMath.countWeekdays(date("2024-01-01"), date("2024-01-08")))
        assertEquals(5L, DateMath.countWeekdays(date("2024-01-08"), date("2024-01-01")))
        assertEquals(0L, DateMath.countWeekdays(date("2024-01-06"), date("2024-01-07")))
    }

    @Test
    fun `additions clamp to the end of shorter months`() {
        assertEquals(LocalDate.of(2024, 2, 29), DateMath.addMonths(date("2024-01-31"), 1))
        assertEquals(LocalDate.of(2024, 3, 31), DateMath.addDays(date("2024-03-01"), 30))
        assertEquals(LocalDate.of(2025, 2, 28), DateMath.addYears(date("2024-02-29"), 1))
        assertEquals(LocalDate.of(2024, 1, 15), DateMath.addWeeks(date("2024-01-01"), 2))
    }

    @Test
    fun `duration splitting handles midnight crossing`() {
        val parts = DateMath.durationBetween(
            DateMath.parseTime("22:30"),
            DateMath.parseTime("01:15"),
            crossesMidnight = true,
        )
        assertEquals(0L, parts.days)
        assertEquals(2L, parts.hours)
        assertEquals(45L, parts.minutes)
        assertEquals(0L, parts.seconds)
    }

    @Test
    fun `seconds are expanded into days hours minutes and seconds`() {
        val parts = DateMath.fromSeconds(90_061)
        assertEquals(1L, parts.days)
        assertEquals(1L, parts.hours)
        assertEquals(1L, parts.minutes)
        assertEquals(1L, parts.seconds)
    }

    @Test
    fun `leap year and month lengths`() {
        assertTrue(DateMath.isLeapYear(2024))
        assertEquals(29, DateMath.daysInMonth(2024, 2))
        assertEquals(28, DateMath.daysInMonth(2023, 2))
        assertEquals(31, DateMath.daysInMonth(2023, 12))
    }
}
