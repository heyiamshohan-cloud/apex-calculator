package com.shohankhan.apexcalculator.core.math

import java.math.BigInteger

enum class BitWidth(val bits: Int) {
    W8(8),
    W16(16),
    W32(32),
    W64(64),
    ;

    val modulus: BigInteger get() = BigInteger.ONE.shiftLeft(bits)
    val mask: BigInteger get() = modulus.subtract(BigInteger.ONE)
    val minSigned: BigInteger get() = BigInteger.ONE.shiftLeft(bits - 1).negate()
    val maxSigned: BigInteger get() = BigInteger.ONE.shiftLeft(bits - 1).subtract(BigInteger.ONE)

    companion object {
        fun fromBits(bits: Int): BitWidth = entries.firstOrNull { it.bits == bits } ?: W32
    }
}

enum class NumberRadix(val base: Int, val label: String) {
    BIN(2, "BIN"),
    OCT(8, "OCT"),
    DEC(10, "DEC"),
    HEX(16, "HEX"),
}

/**
 * A fixed-width integer stored as its unsigned bit pattern. The signed interpretation is derived
 * with two's complement, so both views always describe the same bits.
 */
data class ProgrammerValue(
    val pattern: BigInteger,
    val width: BitWidth,
    val signed: Boolean,
) {
    init {
        require(pattern.signum() >= 0 && pattern <= width.mask) { "Pattern outside the selected width" }
    }

    val unsigned: BigInteger get() = pattern

    val decimal: BigInteger
        get() = if (signed && pattern.testBit(width.bits - 1)) pattern.subtract(width.modulus) else pattern

    fun isBitSet(index: Int): Boolean = index in 0 until width.bits && pattern.testBit(index)

    fun toggleBit(index: Int): ProgrammerValue {
        require(index in 0 until width.bits) { "Bit index outside the selected width" }
        return copy(pattern = pattern.xor(BigInteger.ONE.shiftLeft(index)))
    }

    fun withBit(index: Int, value: Boolean): ProgrammerValue {
        require(index in 0 until width.bits) { "Bit index outside the selected width" }
        val bit = BigInteger.ONE.shiftLeft(index)
        return copy(pattern = if (value) pattern.or(bit) else pattern.and(bit.not()).and(width.mask))
    }

    fun format(radix: NumberRadix, grouped: Boolean = true): String = when (radix) {
        NumberRadix.DEC -> {
            val value = decimal.toString()
            if (grouped) groupDecimal(value) else value
        }
        NumberRadix.BIN -> {
            val raw = pattern.toString(2).padStart(width.bits, '0')
            if (grouped) raw.chunked(4).joinToString(" ") else raw
        }
        NumberRadix.OCT -> pattern.toString(8)
        NumberRadix.HEX -> {
            val raw = pattern.toString(16).uppercase()
            if (grouped) raw.reversed().chunked(4).joinToString(" ").reversed() else raw
        }
    }

    private fun groupDecimal(value: String): String {
        val negative = value.startsWith("-")
        val digits = if (negative) value.substring(1) else value
        val grouped = digits.reversed().chunked(3).joinToString(",").reversed()
        return if (negative) "-$grouped" else grouped
    }

    companion object {
        fun zero(width: BitWidth, signed: Boolean): ProgrammerValue =
            ProgrammerValue(BigInteger.ZERO, width, signed)

        fun fromDecimal(value: BigInteger, width: BitWidth, signed: Boolean): ProgrammerValue =
            ProgrammerValue(value.mod(width.modulus), width, signed)

        fun fromLong(value: Long, width: BitWidth, signed: Boolean): ProgrammerValue =
            fromDecimal(BigInteger.valueOf(value), width, signed)

        /** Parses digits in the given radix. Returns null when the text is not valid for the radix. */
        fun parse(text: String, radix: NumberRadix, width: BitWidth, signed: Boolean): ProgrammerValue? {
            val cleaned = text.trim().replace(" ", "").replace(",", "").replace("_", "")
            if (cleaned.isEmpty()) return null
            val negative = cleaned.startsWith("-")
            val body = if (negative) cleaned.substring(1) else cleaned
            // 0b / 0o / 0x prefixes select the radix explicitly, whatever is highlighted.
            val effectiveRadix = when {
                body.startsWith("0x", ignoreCase = true) -> NumberRadix.HEX
                body.startsWith("0b", ignoreCase = true) -> NumberRadix.BIN
                body.startsWith("0o", ignoreCase = true) -> NumberRadix.OCT
                else -> radix
            }
            val digits = when {
                body.startsWith("0x", ignoreCase = true) -> body.substring(2)
                body.startsWith("0b", ignoreCase = true) -> body.substring(2)
                body.startsWith("0o", ignoreCase = true) -> body.substring(2)
                else -> body
            }
            if (digits.isEmpty()) return null
            val validDigits = "0123456789ABCDEF".take(effectiveRadix.base)
            if (!digits.uppercase().all { it in validDigits }) return null
            val magnitude = digits.toBigIntegerOrNull(effectiveRadix.base) ?: return null
            val signedValue = if (negative) magnitude.negate() else magnitude
            if (effectiveRadix == NumberRadix.DEC && signed) {
                if (signedValue > width.maxSigned || signedValue < width.minSigned) return null
            } else if (signedValue > width.mask) {
                return null
            }
            return fromDecimal(signedValue, width, signed)
        }
    }
}

enum class ProgrammerOp(val label: String, val symbol: String) {
    AND("AND", "&"),
    OR("OR", "|"),
    XOR("XOR", "⊕"),
    NOT("NOT", "~"),
    SHIFT_LEFT("Left shift", "<<"),
    SHIFT_RIGHT("Logical right", ">>>"),
    SHIFT_ARITHMETIC("Arithmetic right", ">>"),
    ADD("Add", "+"),
    SUBTRACT("Subtract", "−"),
    MULTIPLY("Multiply", "×"),
    DIVIDE("Divide", "÷"),
    MODULO("Modulo", "mod"),
}

sealed interface ProgrammerOutcome {
    data class Success(val value: ProgrammerValue, val wrapped: Boolean = false, val note: String? = null) : ProgrammerOutcome

    data class Failure(val message: String) : ProgrammerOutcome
}

/**
 * Fixed-width integer operations. Results are always normalised back into the selected width so
 * overflow behaves exactly like a hardware register; callers are told when wrapping happened.
 */
object ProgrammerMath {

    fun apply(
        accumulator: ProgrammerValue,
        operand: ProgrammerValue?,
        operation: ProgrammerOp,
        shiftCount: Int = 0,
    ): ProgrammerOutcome {
        val width = accumulator.width
        if (operation == ProgrammerOp.NOT) {
            return ProgrammerOutcome.Success(
                accumulator.copy(pattern = accumulator.pattern.xor(width.mask)),
            )
        }
        if (operation == ProgrammerOp.SHIFT_LEFT ||
            operation == ProgrammerOp.SHIFT_RIGHT ||
            operation == ProgrammerOp.SHIFT_ARITHMETIC
        ) {
            if (shiftCount < 0 || shiftCount >= width.bits) {
                return ProgrammerOutcome.Failure("Shift count must be between 0 and ${width.bits - 1} for ${width.bits}-bit values")
            }
            return when (operation) {
                ProgrammerOp.SHIFT_LEFT -> {
                    val raw = accumulator.pattern.shiftLeft(shiftCount)
                    val overflow = raw > width.mask
                    ProgrammerOutcome.Success(
                        accumulator.copy(pattern = raw.and(width.mask)),
                        wrapped = overflow,
                        note = if (overflow) "Bits shifted out of the ${width.bits}-bit register were discarded" else null,
                    )
                }
                ProgrammerOp.SHIFT_RIGHT -> ProgrammerOutcome.Success(
                    accumulator.copy(pattern = accumulator.pattern.shiftRight(shiftCount)),
                )
                else -> ProgrammerOutcome.Success(
                    accumulator.copy(
                        pattern = accumulator.decimal.shiftRight(shiftCount).mod(width.modulus),
                    ),
                )
            }
        }

        val other = operand ?: return ProgrammerOutcome.Failure("Enter a second operand")
        if (other.width != width) {
            return ProgrammerOutcome.Failure("Both values must use the same bit width")
        }
        return when (operation) {
            ProgrammerOp.AND -> ok(accumulator.copy(pattern = accumulator.pattern.and(other.pattern)))
            ProgrammerOp.OR -> ok(accumulator.copy(pattern = accumulator.pattern.or(other.pattern)))
            ProgrammerOp.XOR -> ok(accumulator.copy(pattern = accumulator.pattern.xor(other.pattern)))
            ProgrammerOp.ADD -> arithmetic(accumulator.decimal.add(other.decimal), accumulator)
            ProgrammerOp.SUBTRACT -> arithmetic(accumulator.decimal.subtract(other.decimal), accumulator)
            ProgrammerOp.MULTIPLY -> arithmetic(accumulator.decimal.multiply(other.decimal), accumulator)
            ProgrammerOp.DIVIDE -> {
                if (other.decimal.signum() == 0) {
                    ProgrammerOutcome.Failure("Division by zero is undefined")
                } else {
                    arithmetic(accumulator.decimal.divide(other.decimal), accumulator)
                }
            }
            ProgrammerOp.MODULO -> {
                if (other.decimal.signum() == 0) {
                    ProgrammerOutcome.Failure("Modulo by zero is undefined")
                } else {
                    arithmetic(accumulator.decimal.remainder(other.decimal), accumulator)
                }
            }
            else -> ProgrammerOutcome.Failure("This operation is not supported")
        }
    }

    private fun ok(value: ProgrammerValue) = ProgrammerOutcome.Success(value)

    private fun arithmetic(raw: BigInteger, template: ProgrammerValue): ProgrammerOutcome {
        val width = template.width
        // A signed register overflows at maxSigned/minSigned, an unsigned one at the mask.
        val wrapped = if (template.signed) {
            raw > width.maxSigned || raw < width.minSigned
        } else {
            raw > width.mask || raw < BigInteger.ZERO
        }
        val pattern = raw.mod(width.modulus)
        return ProgrammerOutcome.Success(
            template.copy(pattern = pattern),
            wrapped = wrapped,
            note = if (wrapped) "The result overflowed and wrapped around ${width.bits} bits" else null,
        )
    }

    /** Bit positions from the most significant to the least significant. */
    fun bitPositions(width: BitWidth): List<Int> = (width.bits - 1 downTo 0).toList()

    fun binaryGroups(value: ProgrammerValue): List<String> =
        value.pattern.toString(2).padStart(value.width.bits, '0').chunked(4)
}
