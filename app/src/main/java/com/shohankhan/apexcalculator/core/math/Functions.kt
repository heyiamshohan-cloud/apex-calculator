package com.shohankhan.apexcalculator.core.math

import java.math.BigInteger
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.acosh
import kotlin.math.asin
import kotlin.math.asinh
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.atanh
import kotlin.math.cbrt
import kotlin.math.cosh
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.ln
import kotlin.math.sinh
import kotlin.math.tanh
import kotlin.random.Random

enum class AngleUnit(val label: String) {
    DEG("Degrees"),
    RAD("Radians"),
    ;

    fun toRadians(value: Double): Double = if (this == DEG) Math.toRadians(value) else value

    fun fromRadians(value: Double): Double = if (this == DEG) Math.toDegrees(value) else value
}

data class EvalContext(
    val angleUnit: AngleUnit = AngleUnit.DEG,
    val complexMode: Boolean = false,
    val random: Random = Random.Default,
)

data class FunctionSpec(
    val name: String,
    val minArgs: Int,
    val maxArgs: Int,
    val description: String,
    val apply: (List<Num>, EvalContext) -> Num,
) {
    fun requireArity(count: Int) {
        if (count < minArgs || count > maxArgs) {
            val expected = if (minArgs == maxArgs) "$minArgs" else "$minArgs..$maxArgs"
            throw MathEvalException(MathError.Arity(name, expected))
        }
    }
}

/** Registry of every function understood by the expression engine. */
object MathFunctions {

    private val functions: Map<String, FunctionSpec> = buildList {
        add(trig("sin", "Sine of an angle") { x -> Math.sin(x) })
        add(trig("cos", "Cosine of an angle") { x -> Math.cos(x) })
        add(
            FunctionSpec("tan", 1, 1, "Tangent of an angle") { args, context ->
                val value = args[0]
                if (value is Num.Cpx) {
                    Num.Cpx(ComplexMath.sin(value.c) / ComplexMath.cos(value.c))
                } else {
                    val radians = context.angleUnit.toRadians(NumMath.toDouble(value))
                    if (abs(cos(radians)) < 1e-12) {
                        throw MathEvalException(MathError.Undefined("tan is undefined at this angle"))
                    }
                    Num.Real(Math.tan(radians))
                }
            },
        )
        add(inverseTrig("asin", "Inverse sine (-1 ≤ x ≤ 1)") { x -> asin(x) })
        add(inverseTrig("acos", "Inverse cosine (-1 ≤ x ≤ 1)") { x -> acos(x) })
        add(inverseTrig("atan", "Inverse tangent") { x -> atan(x) })
        add(
            FunctionSpec("atan2", 2, 2, "Angle of the point (x, y)") { args, context ->
                val y = NumMath.toDouble(args[0])
                val x = NumMath.toDouble(args[1])
                if (x == 0.0 && y == 0.0) {
                    throw MathEvalException(MathError.Undefined("atan2(0, 0) is undefined"))
                }
                Num.Real(context.angleUnit.fromRadians(atan2(y, x)))
            },
        )
        add(hyperbolic("sinh", "Hyperbolic sine") { x -> sinh(x) })
        add(hyperbolic("cosh", "Hyperbolic cosine") { x -> cosh(x) })
        add(hyperbolic("tanh", "Hyperbolic tangent") { x -> tanh(x) })
        add(
            FunctionSpec("asinh", 1, 1, "Inverse hyperbolic sine") { args, _ ->
                Num.Real(asinh(NumMath.toDouble(args[0])))
            },
        )
        add(
            FunctionSpec("acosh", 1, 1, "Inverse hyperbolic cosine (x ≥ 1)") { args, _ ->
                val value = NumMath.toDouble(args[0])
                if (value < 1.0) {
                    throw MathEvalException(MathError.Domain("acosh is defined for x ≥ 1"))
                }
                Num.Real(acosh(value))
            },
        )
        add(
            FunctionSpec("atanh", 1, 1, "Inverse hyperbolic tangent (-1 < x < 1)") { args, _ ->
                val value = NumMath.toDouble(args[0])
                if (abs(value) >= 1.0) {
                    throw MathEvalException(MathError.Domain("atanh is defined for -1 < x < 1"))
                }
                Num.Real(atanh(value))
            },
        )
        add(
            FunctionSpec("ln", 1, 1, "Natural logarithm") { args, context ->
                logarithm(args[0], Math.E, context, "ln")
            },
        )
        add(
            FunctionSpec("log", 1, 2, "Logarithm, base 10 by default") { args, context ->
                if (args.size == 1) {
                    logarithm(args[0], 10.0, context, "log")
                } else {
                    val base = NumMath.toDouble(args[1])
                    logarithm(args[0], base, context, "log")
                }
            },
        )
        add(
            FunctionSpec("log10", 1, 1, "Logarithm base 10") { args, context ->
                logarithm(args[0], 10.0, context, "log10")
            },
        )
        add(
            FunctionSpec("log2", 1, 1, "Logarithm base 2") { args, context ->
                logarithm(args[0], 2.0, context, "log2")
            },
        )
        add(
            FunctionSpec("exp", 1, 1, "e raised to the power x") { args, context ->
                val value = args[0]
                if (value is Num.Cpx) Num.Cpx(ComplexMath.exp(value.c)) else {
                    val result = exp(NumMath.toDouble(value))
                    if (result.isInfinite()) throw MathEvalException(MathError.Overflow("exp overflowed"))
                    Num.Real(result)
                }
            },
        )
        add(
            FunctionSpec("sqrt", 1, 1, "Square root") { args, context ->
                val value = args[0]
                when {
                    value is Num.Cpx -> Num.Cpx(value.c.sqrt())
                    NumMath.toDouble(value) < 0 -> {
                        if (!context.complexMode) {
                            throw MathEvalException(
                                MathError.Domain("Square root of a negative number is not real. Enable complex mode."),
                            )
                        }
                        Num.Cpx(ComplexNumber(NumMath.toDouble(value), 0.0).sqrt())
                    }
                    else -> Num.Real(Math.sqrt(NumMath.toDouble(value)))
                }
            },
        )
        add(
            FunctionSpec("cbrt", 1, 1, "Cube root") { args, _ ->
                Num.Real(cbrt(NumMath.toDouble(args[0])))
            },
        )
        add(
            FunctionSpec("root", 2, 2, "n-th root: root(x, n)") { args, context ->
                val value = NumMath.toDouble(args[0])
                val degree = NumMath.toDouble(args[1])
                if (degree == 0.0) throw MathEvalException(MathError.Domain("The root degree cannot be zero"))
                if (value < 0 && abs(degree % 2.0) > 1e-9) {
                    if (!context.complexMode) {
                        throw MathEvalException(MathError.Domain("An even root of a negative number is not real"))
                    }
                    Num.Cpx(ComplexNumber(value, 0.0).pow(ComplexNumber(1.0 / degree, 0.0)))
                } else if (value < 0) {
                    Num.Real(-Math.pow(-value, 1.0 / degree))
                } else {
                    Num.Real(Math.pow(value, 1.0 / degree))
                }
            },
        )
        add(
            FunctionSpec("abs", 1, 1, "Absolute value / modulus") { args, _ ->
                NumMath.abs(args[0])
            },
        )
        add(
            FunctionSpec("sign", 1, 1, "Sign: -1, 0 or 1") { args, _ -> NumMath.signum(args[0]) },
        )
        add(
            FunctionSpec("floor", 1, 1, "Greatest integer ≤ x") { args, _ -> NumMath.floor(args[0]) },
        )
        add(
            FunctionSpec("ceil", 1, 1, "Smallest integer ≥ x") { args, _ -> NumMath.ceil(args[0]) },
        )
        add(
            FunctionSpec("round", 1, 1, "Nearest integer") { args, _ -> NumMath.round(args[0]) },
        )
        add(
            FunctionSpec("trunc", 1, 1, "Integer part") { args, _ -> NumMath.truncate(args[0]) },
        )
        add(
            FunctionSpec("frac", 1, 1, "Fractional part") { args, _ ->
                NumMath.subtract(args[0], NumMath.truncate(args[0]))
            },
        )
        add(
            FunctionSpec("min", 2, 16, "Smallest value") { args, _ ->
                args.reduce { a, b -> if (NumMath.compare(a, b) <= 0) a else b }
            },
        )
        add(
            FunctionSpec("max", 2, 16, "Largest value") { args, _ ->
                args.reduce { a, b -> if (NumMath.compare(a, b) >= 0) a else b }
            },
        )
        add(
            FunctionSpec("hypot", 2, 2, "Euclidean norm") { args, _ ->
                Num.Real(hypot(NumMath.toDouble(args[0]), NumMath.toDouble(args[1])))
            },
        )
        add(
            FunctionSpec("gcd", 2, 8, "Greatest common divisor") { args, _ ->
                val values = args.map { requireNonNegativeInteger(it, "gcd") }
                Num.IntVal(values.reduce { a, b -> a.gcd(b) })
            },
        )
        add(
            FunctionSpec("lcm", 2, 8, "Least common multiple") { args, _ ->
                val values = args.map { requireNonNegativeInteger(it, "lcm") }
                Num.IntVal(
                    values.reduce { a, b ->
                        if (a.signum() == 0 || b.signum() == 0) BigInteger.ZERO
                        else a.divide(a.gcd(b)).multiply(b).abs()
                    },
                )
            },
        )
        add(
            FunctionSpec("mod", 2, 2, "Remainder with the sign of the divisor") { args, _ ->
                val divisor = NumMath.toDouble(args[1])
                if (divisor == 0.0) throw MathEvalException(MathError.DivisionByZero)
                if (isIntegral(args[0]) && isIntegral(args[1])) {
                    val a = NumMath.asBigInteger(args[0])!!
                    val b = NumMath.asBigInteger(args[1])!!
                    Num.IntVal(a.mod(b.abs()).let { if (b.signum() < 0 && it.signum() != 0) it - b.abs() else it })
                } else {
                    val a = NumMath.toDouble(args[0])
                    Num.Real(a - divisor * floor(a / divisor))
                }
            },
        )
        add(
            FunctionSpec("rem", 2, 2, "Remainder with the sign of the dividend") { args, _ ->
                val divisor = NumMath.toDouble(args[1])
                if (divisor == 0.0) throw MathEvalException(MathError.DivisionByZero)
                if (isIntegral(args[0]) && isIntegral(args[1])) {
                    Num.IntVal(NumMath.asBigInteger(args[0])!!.rem(NumMath.asBigInteger(args[1])!!))
                } else {
                    val a = NumMath.toDouble(args[0])
                    Num.Real(a % divisor)
                }
            },
        )
        add(
            FunctionSpec("ncr", 2, 2, "Combinations C(n, r)") { args, _ ->
                Num.IntVal(combinatorics(args, combinations = true))
            },
        )
        add(
            FunctionSpec("npr", 2, 2, "Permutations P(n, r)") { args, _ ->
                Num.IntVal(combinatorics(args, combinations = false))
            },
        )
        add(
            FunctionSpec("rand", 0, 0, "Random number in [0, 1)") { _, context ->
                Num.Real(context.random.nextDouble())
            },
        )
        add(
            FunctionSpec("randint", 2, 2, "Random integer between two bounds") { args, context ->
                val low = requireInteger(args[0], "randint")
                val high = requireInteger(args[1], "randint")
                if (low > high) throw MathEvalException(MathError.Domain("randint requires the first bound to be smaller"))
                val span = high.subtract(low).add(BigInteger.ONE)
                if (span.bitLength() > 62) throw MathEvalException(MathError.Limit("randint bounds are too wide"))
                Num.IntVal(low.add(BigInteger.valueOf(context.random.nextLong(span.toLong()))))
            },
        )
        add(
            FunctionSpec("deg", 1, 1, "Radians to degrees") { args, _ ->
                Num.Real(Math.toDegrees(NumMath.toDouble(args[0])))
            },
        )
        add(
            FunctionSpec("rad", 1, 1, "Degrees to radians") { args, _ ->
                Num.Real(Math.toRadians(NumMath.toDouble(args[0])))
            },
        )
        add(
            FunctionSpec("re", 1, 1, "Real part") { args, _ ->
                if (args[0] is Num.Cpx) Num.Real((args[0] as Num.Cpx).c.re) else args[0]
            },
        )
        add(
            FunctionSpec("im", 1, 1, "Imaginary part") { args, _ ->
                if (args[0] is Num.Cpx) Num.Real((args[0] as Num.Cpx).c.im) else Num.IntVal(BigInteger.ZERO)
            },
        )
        add(
            FunctionSpec("arg", 1, 1, "Argument (angle) of a complex number") { args, context ->
                val complex = NumMath.toComplex(args[0])
                Num.Real(context.angleUnit.fromRadians(complex.argument))
            },
        )
        add(
            FunctionSpec("conj", 1, 1, "Complex conjugate") { args, _ ->
                val complex = NumMath.toComplex(args[0])
                Num.Cpx(complex.conjugate())
            },
        )
        add(
            FunctionSpec("factorial", 1, 1, "Factorial n!") { args, _ -> NumMath.factorial(args[0]) },
        )
    }.associateBy { it.name }

    fun lookup(name: String): FunctionSpec? = functions[name.lowercase()]

    fun names(): List<String> = functions.keys.sorted()

    private fun trig(
        name: String,
        description: String,
        operation: (Double) -> Double,
    ) = FunctionSpec(name, 1, 1, description) { args, context ->
        val value = args[0]
        if (value is Num.Cpx) {
            val result = when (name) {
                "sin" -> ComplexMath.sin(value.c)
                "cos" -> ComplexMath.cos(value.c)
                else -> ComplexMath.sin(value.c)
            }
            Num.Cpx(result)
        } else {
            val radians = context.angleUnit.toRadians(NumMath.toDouble(value))
            Num.Real(operation(radians))
        }
    }

    private fun inverseTrig(
        name: String,
        description: String,
        operation: (Double) -> Double,
    ) = FunctionSpec(name, 1, 1, description) { args, context ->
        val value = args[0]
        if (value is Num.Cpx) {
            throw MathEvalException(
                MathError.Unsupported("$name of a complex number is not supported in this version"),
            )
        }
        val x = NumMath.toDouble(value)
        if (abs(x) > 1.0 && (name == "asin" || name == "acos")) {
            throw MathEvalException(MathError.Domain("$name is defined for -1 ≤ x ≤ 1 in real mode"))
        }
        Num.Real(context.angleUnit.fromRadians(operation(x)))
    }

    private fun hyperbolic(
        name: String,
        description: String,
        operation: (Double) -> Double,
    ) = FunctionSpec(name, 1, 1, description) { args, _ ->
        val result = operation(NumMath.toDouble(args[0]))
        if (result.isInfinite()) throw MathEvalException(MathError.Overflow("$name overflowed"))
        Num.Real(result)
    }

    private fun logarithm(value: Num, base: Double, context: EvalContext, name: String): Num {
        val x = NumMath.toDouble(value)
        if (value is Num.Cpx) {
            if (base != 10.0 && base != Math.E) {
                throw MathEvalException(MathError.Unsupported("Complex logarithms only support base e or 10"))
            }
            val lnZ = ComplexMath.ln(value.c)
            return Num.Cpx(if (base == 10.0) lnZ / ComplexNumber(ln(10.0), 0.0) else lnZ)
        }
        if (x < 0) {
            if (!context.complexMode) {
                throw MathEvalException(
                    MathError.Domain("$name is not defined for negative numbers in real mode"),
                )
            }
            val lnZ = ComplexMath.ln(ComplexNumber(x, 0.0))
            return Num.Cpx(if (base == 10.0) lnZ / ComplexNumber(ln(10.0), 0.0) else lnZ)
        }
        if (x == 0.0) {
            throw MathEvalException(MathError.Undefined("$name(0) is undefined"))
        }
        if (base <= 0.0 || base == 1.0) {
            throw MathEvalException(MathError.Domain("The logarithm base must be positive and not equal to 1"))
        }
        return Num.Real(kotlin.math.log(x, base))
    }

    private fun requireInteger(value: Num, function: String): BigInteger =
        NumMath.asBigInteger(value)
            ?: throw MathEvalException(MathError.Domain("$function requires integer arguments"))

    private fun requireNonNegativeInteger(value: Num, function: String): BigInteger {
        val integer = requireInteger(value, function)
        if (integer.signum() < 0) {
            throw MathEvalException(MathError.Domain("$function requires non-negative integers"))
        }
        return integer
    }

    private fun isIntegral(value: Num): Boolean = NumMath.asBigInteger(value) != null

    private fun combinatorics(args: List<Num>, combinations: Boolean): BigInteger {
        val n = requireNonNegativeInteger(args[0], if (combinations) "ncr" else "npr")
        val r = requireNonNegativeInteger(args[1], if (combinations) "ncr" else "npr")
        if (r > n) throw MathEvalException(MathError.Domain("r cannot be greater than n"))
        if (n.bitLength() > 63) throw MathEvalException(MathError.Limit("n is too large"))
        var result = BigInteger.ONE
        val nLong = n.toLong()
        val rLong = r.toLong()
        if (combinations) {
            val k = minOf(rLong, nLong - rLong)
            for (i in 0 until k) {
                result = result.multiply(BigInteger.valueOf(nLong - i)).divide(BigInteger.valueOf(i + 1))
            }
        } else {
            for (i in 0 until rLong) {
                result = result.multiply(BigInteger.valueOf(nLong - i))
            }
        }
        return result
    }

    private fun cos(radians: Double): Double = Math.cos(radians)
}

/** Complex implementations of the transcendental functions supported by the engine. */
internal object ComplexMath {

    fun exp(z: ComplexNumber): ComplexNumber {
        val magnitude = exp(z.re)
        return ComplexNumber(magnitude * kotlin.math.cos(z.im), magnitude * kotlin.math.sin(z.im))
    }

    fun ln(z: ComplexNumber): ComplexNumber {
        if (z.modulus == 0.0) throw MathEvalException(MathError.Undefined("The logarithm of zero is undefined"))
        return ComplexNumber(ln(z.modulus), z.argument)
    }

    fun sin(z: ComplexNumber): ComplexNumber =
        ComplexNumber(kotlin.math.sin(z.re) * cosh(z.im), kotlin.math.cos(z.re) * sinh(z.im))

    fun cos(z: ComplexNumber): ComplexNumber =
        ComplexNumber(kotlin.math.cos(z.re) * cosh(z.im), -kotlin.math.sin(z.re) * sinh(z.im))
}
