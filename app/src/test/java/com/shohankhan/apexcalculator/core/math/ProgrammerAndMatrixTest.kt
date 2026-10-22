package com.shohankhan.apexcalculator.core.math

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigInteger

/** Fixed-width integer arithmetic, radix conversion and the bit inspector. */
class ProgrammerTest {

    private fun value(decimal: Long, width: BitWidth = BitWidth.W32, signed: Boolean = true): ProgrammerValue =
        ProgrammerValue.fromLong(decimal, width, signed)

    @Test
    fun `parses binary, octal and hexadecimal literals`() {
        assertEquals(BigInteger.valueOf(10), ProgrammerValue.parse("0b1010", NumberRadix.DEC, BitWidth.W32, false)?.decimal)
        assertEquals(BigInteger.valueOf(255), ProgrammerValue.parse("0xFF", NumberRadix.DEC, BitWidth.W32, false)?.decimal)
        assertEquals(BigInteger.valueOf(8), ProgrammerValue.parse("0o10", NumberRadix.DEC, BitWidth.W32, false)?.decimal)
        assertEquals(BigInteger.valueOf(8), ProgrammerValue.parse("1000", NumberRadix.BIN, BitWidth.W32, false)?.decimal)
    }

    @Test
    fun `radix formatting`() {
        val ten = value(10)
        assertEquals("00000000000000000000000000001010", ten.format(NumberRadix.BIN, grouped = false))
        assertEquals("0000 1010", value(10, BitWidth.W8, signed = false).format(NumberRadix.BIN, grouped = true))
        assertEquals("12", ten.format(NumberRadix.OCT, grouped = false))
        assertEquals("A", ten.format(NumberRadix.HEX, grouped = false))
        assertEquals("10", ten.format(NumberRadix.DEC, grouped = false))
        assertEquals("A0", value(160).format(NumberRadix.HEX, grouped = false))
        assertEquals("FF", value(255, BitWidth.W8, signed = false).format(NumberRadix.HEX, grouped = false))
    }

    @Test
    fun `bitwise AND and OR and XOR`() {
        val result = ProgrammerMath.apply(value(12), value(10), ProgrammerOp.AND)
        assertTrue(result is ProgrammerOutcome.Success)
        assertEquals(BigInteger.valueOf(8), (result as ProgrammerOutcome.Success).value.decimal)

        val or = ProgrammerMath.apply(value(12), value(10), ProgrammerOp.OR) as ProgrammerOutcome.Success
        assertEquals(BigInteger.valueOf(14), or.value.decimal)

        val xor = ProgrammerMath.apply(value(12), value(10), ProgrammerOp.XOR) as ProgrammerOutcome.Success
        assertEquals(BigInteger.valueOf(6), xor.value.decimal)
    }

    @Test
    fun `NOT is width relative`() {
        val eight = ProgrammerMath.apply(value(0, BitWidth.W8, false), null, ProgrammerOp.NOT) as ProgrammerOutcome.Success
        assertEquals(BigInteger.valueOf(255), eight.value.decimal)
        val wide = ProgrammerMath.apply(value(0, BitWidth.W32, false), null, ProgrammerOp.NOT) as ProgrammerOutcome.Success
        assertEquals(BigInteger.valueOf(4294967295L), wide.value.decimal)
    }

    @Test
    fun `two's complement signed interpretation`() {
        val allOnes = value(255, BitWidth.W8, signed = true)
        assertEquals(BigInteger.valueOf(-1), allOnes.decimal)
        assertEquals(BigInteger.valueOf(255), allOnes.unsigned)

        val min = value(-128, BitWidth.W8, signed = true)
        assertEquals(BigInteger.valueOf(-128), min.decimal)
        assertEquals(BigInteger.valueOf(128), min.unsigned)
    }

    @Test
    fun `arithmetic wraps inside the selected width`() {
        val sum = ProgrammerMath.apply(
            value(127, BitWidth.W8, signed = true),
            value(1, BitWidth.W8, signed = true),
            ProgrammerOp.ADD,
        )
        assertTrue(sum is ProgrammerOutcome.Success)
        assertEquals(BigInteger.valueOf(-128), (sum as ProgrammerOutcome.Success).value.decimal)
        assertTrue(sum.wrapped)
    }

    @Test
    fun `division and modulo by zero fail cleanly`() {
        val divide = ProgrammerMath.apply(value(10), value(0), ProgrammerOp.DIVIDE)
        assertTrue(divide is ProgrammerOutcome.Failure)
        val modulo = ProgrammerMath.apply(value(10), value(0), ProgrammerOp.MODULO)
        assertTrue(modulo is ProgrammerOutcome.Failure)
    }

    @Test
    fun `shifts respect the width`() {
        val left = ProgrammerMath.apply(value(1), null, ProgrammerOp.SHIFT_LEFT, shiftCount = 4) as ProgrammerOutcome.Success
        assertEquals(BigInteger.valueOf(16), left.value.decimal)

        val logical = ProgrammerMath.apply(value(-1, BitWidth.W8), null, ProgrammerOp.SHIFT_RIGHT, shiftCount = 4) as ProgrammerOutcome.Success
        assertEquals(BigInteger.valueOf(15), logical.value.decimal)

        val arithmetic = ProgrammerMath.apply(value(-16, BitWidth.W8), null, ProgrammerOp.SHIFT_ARITHMETIC, shiftCount = 2) as ProgrammerOutcome.Success
        assertEquals(BigInteger.valueOf(-4), arithmetic.value.decimal)

        val invalid = ProgrammerMath.apply(value(1, BitWidth.W8), null, ProgrammerOp.SHIFT_LEFT, shiftCount = 8)
        assertTrue(invalid is ProgrammerOutcome.Failure)
    }

    @Test
    fun `bit toggling is reversible`() {
        val base = value(0, BitWidth.W8, false)
        val toggled = base.toggleBit(3)
        assertTrue(toggled.isBitSet(3))
        assertEquals(BigInteger.valueOf(8), toggled.unsigned)
        assertFalse(toggled.toggleBit(3).isBitSet(3))
        assertEquals(BigInteger.valueOf(15), base.withBit(3, true).withBit(2, true).withBit(1, true).withBit(0, true).unsigned)
    }

    @Test
    fun `invalid input is rejected`() {
        assertNull(ProgrammerValue.parse("", NumberRadix.DEC, BitWidth.W32, true))
        assertNull(ProgrammerValue.parse("XYZ", NumberRadix.DEC, BitWidth.W32, true))
        assertNull(ProgrammerValue.parse("256", NumberRadix.DEC, BitWidth.W8, false))
        assertNull(ProgrammerValue.parse("-129", NumberRadix.DEC, BitWidth.W8, true))
        assertNull(ProgrammerValue.parse("1F", NumberRadix.DEC, BitWidth.W32, true))
    }

    @Test
    fun `binary groups are always full width`() {
        val eight = value(10, BitWidth.W8, false)
        assertEquals(listOf("0000", "1010"), ProgrammerMath.binaryGroups(eight))
        assertEquals(64, ProgrammerMath.bitPositions(BitWidth.W64).size)
    }
}

/** Matrix operations: determinant, inverse, systems and rank. */
class MatrixTest {

    private fun matrix(vararg rows: DoubleArray): Matrix =
        Matrix.of(rows.map { row -> row.toList() })

    @Test
    fun `determinant of the mandated matrix`() {
        val det = MatrixMath.determinant(matrix(doubleArrayOf(1.0, 2.0), doubleArrayOf(3.0, 4.0)))
        assertTrue(det is MatrixResult.Scalar)
        assertEquals(-2.0, (det as MatrixResult.Scalar).value, 1e-12)
    }

    @Test
    fun `determinant of identity and singular matrices`() {
        val identity = MatrixMath.determinant(Matrix.identity(4)) as MatrixResult.Scalar
        assertEquals(1.0, identity.value, 1e-12)

        val singular = MatrixMath.determinant(matrix(doubleArrayOf(2.0, 4.0), doubleArrayOf(1.0, 2.0)))
        assertTrue(singular is MatrixResult.Scalar)
        assertEquals(0.0, (singular as MatrixResult.Scalar).value, 1e-12)
    }

    @Test
    fun `inverse multiplies back to the identity`() {
        val source = matrix(doubleArrayOf(4.0, 7.0), doubleArrayOf(2.0, 6.0))
        val inverse = MatrixMath.inverse(source)
        assertTrue(inverse is MatrixResult.MatrixValue)
        val product = MatrixMath.multiply(source, (inverse as MatrixResult.MatrixValue).matrix)
        assertTrue(product is MatrixResult.MatrixValue)
        val rows = (product as MatrixResult.MatrixValue).matrix.toRows()
        for (i in 0..1) {
            for (j in 0..1) {
                val expected = if (i == j) 1.0 else 0.0
                assertEquals(expected, rows[i][j], 1e-9)
            }
        }
    }

    @Test
    fun `inverse of a singular matrix fails`() {
        val singular = matrix(doubleArrayOf(1.0, 2.0), doubleArrayOf(2.0, 4.0))
        assertTrue(MatrixMath.inverse(singular) is MatrixResult.Failure)
    }

    @Test
    fun `solves a linear system`() {
        // 2x + y = 5, x + 3y = 10  ->  x = 1, y = 3
        val coefficients = matrix(doubleArrayOf(2.0, 1.0), doubleArrayOf(1.0, 3.0))
        val constants = Matrix.of(listOf(listOf(5.0), listOf(10.0)))
        val result = MatrixMath.solve(coefficients, constants)
        assertTrue(result is MatrixResult.MatrixValue)
        val values = (result as MatrixResult.MatrixValue).matrix.toRows()
        assertEquals(1.0, values[0][0], 1e-9)
        assertEquals(3.0, values[1][0], 1e-9)
    }

    @Test
    fun `rank and transpose`() {
        val source = matrix(doubleArrayOf(1.0, 2.0), doubleArrayOf(2.0, 4.0))
        assertEquals(1, MatrixMath.rank(source))
        val transpose = MatrixMath.transpose(source) as MatrixResult.MatrixValue
        assertEquals(2.0, transpose.matrix[0, 1], 0.0)
        assertEquals(2.0, transpose.matrix[1, 0], 0.0)
    }

    @Test
    fun `dimension mismatches fail`() {
        val row = matrix(doubleArrayOf(1.0, 2.0))
        val column = Matrix.of(listOf(listOf(1.0), listOf(2.0)))
        assertTrue(MatrixMath.multiply(row, row) is MatrixResult.Failure)
        assertTrue(MatrixMath.add(row, column) is MatrixResult.Failure)
    }

    @Test
    fun `rref of a dependent system`() {
        val source = matrix(
            doubleArrayOf(1.0, 2.0, 3.0),
            doubleArrayOf(2.0, 4.0, 6.0),
        )
        val rref = MatrixMath.rref(source)
        assertEquals(1.0, rref[0, 0], 1e-9)
        assertEquals(0.0, rref[1, 0], 1e-9)
        assertEquals(0.0, rref[1, 2], 1e-9)
    }
}
