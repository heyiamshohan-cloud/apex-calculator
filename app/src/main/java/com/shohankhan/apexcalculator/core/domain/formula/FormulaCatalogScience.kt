package com.shohankhan.apexcalculator.core.domain.formula

import com.shohankhan.apexcalculator.core.math.Probability
import kotlin.math.PI
import kotlin.math.sqrt

internal fun physicsFormulas(): List<FormulaDefinition> = listOf(
    formula(
        id = "density",
        name = "Density",
        category = FormulaCategory.PHYSICS,
        expression = "ρ = m ÷ V",
        description = "Mass per unit volume.",
        inputs = listOf(input("m", "Mass", "kg"), input("v", "Volume", "m³")),
        output = FormulaOutput("Density", "kg/m³"),
    ) { values ->
        val volume = requirePositive(values.value("v", "Volume"), "Volume")
        FormulaResult(values.value("m", "Mass") / volume)
    },
    formula(
        id = "force",
        name = "Force (Newton's second law)",
        category = FormulaCategory.PHYSICS,
        expression = "F = m × a",
        description = "Net force on an object with constant acceleration.",
        inputs = listOf(input("m", "Mass", "kg"), input("a", "Acceleration", "m/s²")),
        output = FormulaOutput("Force", "N"),
        assumptions = "Constant mass and acceleration in an inertial frame.",
    ) { values -> FormulaResult(values.value("m", "Mass") * values.value("a", "Acceleration")) },
    formula(
        id = "work",
        name = "Work",
        category = FormulaCategory.PHYSICS,
        expression = "W = F × d",
        description = "Work done by a constant force along the displacement.",
        inputs = listOf(input("f", "Force", "N"), input("d", "Displacement", "m")),
        output = FormulaOutput("Work", "J"),
        assumptions = "Force and displacement are in the same direction.",
    ) { values -> FormulaResult(values.value("f", "Force") * values.value("d", "Displacement")) },
    formula(
        id = "power",
        name = "Power",
        category = FormulaCategory.PHYSICS,
        expression = "P = W ÷ t",
        description = "Rate of doing work.",
        inputs = listOf(input("w", "Work", "J"), input("t", "Time", "s")),
        output = FormulaOutput("Power", "W"),
    ) { values ->
        val time = requirePositive(values.value("t", "Time"), "Time")
        FormulaResult(values.value("w", "Work") / time)
    },
    formula(
        id = "kinetic_energy",
        name = "Kinetic energy",
        category = FormulaCategory.PHYSICS,
        expression = "KE = ½mv²",
        description = "Energy of a moving mass.",
        inputs = listOf(input("m", "Mass", "kg"), input("v", "Velocity", "m/s")),
        output = FormulaOutput("Kinetic energy", "J"),
    ) { values ->
        val mass = requirePositive(values.value("m", "Mass"), "Mass")
        val velocity = values.value("v", "Velocity")
        FormulaResult(0.5 * mass * velocity * velocity)
    },
    formula(
        id = "potential_energy",
        name = "Gravitational potential energy",
        category = FormulaCategory.PHYSICS,
        expression = "PE = m × g × h",
        description = "Potential energy near the Earth's surface.",
        inputs = listOf(
            input("m", "Mass", "kg"),
            input("h", "Height", "m"),
            input("g", "Gravity", "m/s²", hint = "9.81 on Earth"),
        ),
        output = FormulaOutput("Potential energy", "J"),
        assumptions = "Constant gravitational field strength.",
    ) { values ->
        FormulaResult(
            requirePositive(values.value("m", "Mass"), "Mass") *
                requirePositive(values.value("g", "Gravity"), "Gravity") *
                values.value("h", "Height"),
        )
    },
    formula(
        id = "pressure",
        name = "Pressure",
        category = FormulaCategory.PHYSICS,
        expression = "p = F ÷ A",
        description = "Force per unit area.",
        inputs = listOf(input("f", "Force", "N"), input("a", "Area", "m²")),
        output = FormulaOutput("Pressure", "Pa"),
    ) { values ->
        val area = requirePositive(values.value("a", "Area"), "Area")
        FormulaResult(values.value("f", "Force") / area)
    },
    formula(
        id = "momentum",
        name = "Momentum",
        category = FormulaCategory.PHYSICS,
        expression = "p = m × v",
        description = "Linear momentum of a body.",
        inputs = listOf(input("m", "Mass", "kg"), input("v", "Velocity", "m/s")),
        output = FormulaOutput("Momentum", "kg·m/s"),
    ) { values -> FormulaResult(values.value("m", "Mass") * values.value("v", "Velocity")) },
    formula(
        id = "impulse",
        name = "Impulse",
        category = FormulaCategory.PHYSICS,
        expression = "J = F × t",
        description = "Impulse delivered by a constant force.",
        inputs = listOf(input("f", "Force", "N"), input("t", "Time", "s")),
        output = FormulaOutput("Impulse", "N·s"),
    ) { values -> FormulaResult(values.value("f", "Force") * values.value("t", "Time")) },
    formula(
        id = "frequency_period",
        name = "Frequency and period",
        category = FormulaCategory.PHYSICS,
        expression = "f = 1 ÷ T",
        description = "Frequency of a periodic motion from its period.",
        inputs = listOf(input("t", "Period", "s")),
        output = FormulaOutput("Frequency", "Hz"),
    ) { values ->
        val period = requirePositive(values.value("t", "Period"), "Period")
        FormulaResult(1 / period)
    },
    formula(
        id = "wave_speed",
        name = "Wave speed",
        category = FormulaCategory.PHYSICS,
        expression = "v = f × λ",
        description = "Speed of a wave from frequency and wavelength.",
        inputs = listOf(input("f", "Frequency", "Hz"), input("l", "Wavelength", "m")),
        output = FormulaOutput("Speed", "m/s"),
    ) { values -> FormulaResult(values.value("f", "Frequency") * values.value("l", "Wavelength")) },
    formula(
        id = "ohms_law_voltage",
        name = "Ohm's law: voltage",
        category = FormulaCategory.PHYSICS,
        expression = "V = I × R",
        description = "Voltage across a resistor.",
        inputs = listOf(input("i", "Current", "A"), input("r", "Resistance", "Ω")),
        output = FormulaOutput("Voltage", "V"),
        tags = listOf("ohms law", "electricity"),
    ) { values -> FormulaResult(values.value("i", "Current") * values.value("r", "Resistance")) },
    formula(
        id = "ohms_law_current",
        name = "Ohm's law: current",
        category = FormulaCategory.PHYSICS,
        expression = "I = V ÷ R",
        description = "Current through a resistor.",
        inputs = listOf(input("v", "Voltage", "V"), input("r", "Resistance", "Ω")),
        output = FormulaOutput("Current", "A"),
    ) { values ->
        val resistance = requirePositive(values.value("r", "Resistance"), "Resistance")
        FormulaResult(values.value("v", "Voltage") / resistance)
    },
    formula(
        id = "ohms_law_resistance",
        name = "Ohm's law: resistance",
        category = FormulaCategory.PHYSICS,
        expression = "R = V ÷ I",
        description = "Resistance from voltage and current.",
        inputs = listOf(input("v", "Voltage", "V"), input("i", "Current", "A")),
        output = FormulaOutput("Resistance", "Ω"),
    ) { values ->
        val current = requirePositive(values.value("i", "Current"), "Current")
        FormulaResult(values.value("v", "Voltage") / current)
    },
    formula(
        id = "electric_power",
        name = "Electrical power",
        category = FormulaCategory.PHYSICS,
        expression = "P = V × I",
        description = "Power dissipated by a component.",
        inputs = listOf(input("v", "Voltage", "V"), input("i", "Current", "A")),
        output = FormulaOutput("Power", "W"),
    ) { values -> FormulaResult(values.value("v", "Voltage") * values.value("i", "Current")) },
    formula(
        id = "series_resistance",
        name = "Series resistance",
        category = FormulaCategory.PHYSICS,
        expression = "R = R₁ + R₂",
        description = "Total resistance of two resistors in series.",
        inputs = listOf(input("r1", "R₁", "Ω"), input("r2", "R₂", "Ω")),
        output = FormulaOutput("Total resistance", "Ω"),
    ) { values ->
        FormulaResult(
            requireNonNegative(values.value("r1", "R₁"), "R₁") +
                requireNonNegative(values.value("r2", "R₂"), "R₂"),
        )
    },
    formula(
        id = "parallel_resistance",
        name = "Parallel resistance",
        category = FormulaCategory.PHYSICS,
        expression = "R = (R₁ × R₂) ÷ (R₁ + R₂)",
        description = "Total resistance of two resistors in parallel.",
        inputs = listOf(input("r1", "R₁", "Ω"), input("r2", "R₂", "Ω")),
        output = FormulaOutput("Total resistance", "Ω"),
    ) { values ->
        val r1 = requirePositive(values.value("r1", "R₁"), "R₁")
        val r2 = requirePositive(values.value("r2", "R₂"), "R₂")
        FormulaResult(r1 * r2 / (r1 + r2))
    },
    formula(
        id = "charge_current_time",
        name = "Charge, current and time",
        category = FormulaCategory.PHYSICS,
        expression = "Q = I × t",
        description = "Charge moved by a current over time.",
        inputs = listOf(input("i", "Current", "A"), input("t", "Time", "s")),
        output = FormulaOutput("Charge", "C"),
    ) { values -> FormulaResult(values.value("i", "Current") * values.value("t", "Time")) },
    formula(
        id = "kinematics_velocity",
        name = "Kinematics: final velocity",
        category = FormulaCategory.PHYSICS,
        expression = "v = u + a·t",
        description = "Final velocity under constant acceleration.",
        inputs = listOf(
            input("u", "Initial velocity", "m/s"),
            input("a", "Acceleration", "m/s²"),
            input("t", "Time", "s"),
        ),
        output = FormulaOutput("Final velocity", "m/s"),
        assumptions = "Constant acceleration in a straight line.",
    ) { values ->
        FormulaResult(
            values.value("u", "Initial velocity") +
                values.value("a", "Acceleration") * values.value("t", "Time"),
        )
    },
    formula(
        id = "kinematics_displacement",
        name = "Kinematics: displacement",
        category = FormulaCategory.PHYSICS,
        expression = "s = u·t + ½a·t²",
        description = "Displacement under constant acceleration.",
        inputs = listOf(
            input("u", "Initial velocity", "m/s"),
            input("a", "Acceleration", "m/s²"),
            input("t", "Time", "s"),
        ),
        output = FormulaOutput("Displacement", "m"),
        assumptions = "Constant acceleration in a straight line.",
    ) { values ->
        val time = values.value("t", "Time")
        FormulaResult(values.value("u", "Initial velocity") * time + 0.5 * values.value("a", "Acceleration") * time * time)
    },
    formula(
        id = "kinematics_third",
        name = "Kinematics: velocity squared",
        category = FormulaCategory.PHYSICS,
        expression = "v² = u² + 2as",
        description = "Final velocity from displacement without time.",
        inputs = listOf(
            input("u", "Initial velocity", "m/s"),
            input("a", "Acceleration", "m/s²"),
            input("s", "Displacement", "m"),
        ),
        output = FormulaOutput("Final velocity", "m/s"),
    ) { values ->
        val square = values.value("u", "Initial velocity") * values.value("u", "Initial velocity") +
            2 * values.value("a", "Acceleration") * values.value("s", "Displacement")
        if (square < 0) throw FormulaException("These values imply a negative squared velocity")
        FormulaResult(sqrt(square))
    },
    formula(
        id = "angular_velocity",
        name = "Angular velocity",
        category = FormulaCategory.PHYSICS,
        expression = "ω = 2πf",
        description = "Angular velocity from frequency.",
        inputs = listOf(input("f", "Frequency", "Hz")),
        output = FormulaOutput("Angular velocity", "rad/s"),
    ) { values -> FormulaResult(2 * PI * values.value("f", "Frequency")) },
    formula(
        id = "torque",
        name = "Torque",
        category = FormulaCategory.PHYSICS,
        expression = "τ = F × r",
        description = "Torque from a perpendicular force at a radius.",
        inputs = listOf(input("f", "Force", "N"), input("r", "Radius", "m")),
        output = FormulaOutput("Torque", "N·m"),
        assumptions = "The force acts perpendicular to the radius.",
    ) { values -> FormulaResult(values.value("f", "Force") * values.value("r", "Radius")) },
    formula(
        id = "efficiency",
        name = "Efficiency",
        category = FormulaCategory.PHYSICS,
        expression = "η = (useful output ÷ input) × 100",
        description = "Percentage efficiency of a device.",
        inputs = listOf(input("output", "Useful output"), input("input", "Total input")),
        output = FormulaOutput("Efficiency", "%"),
    ) { values ->
        val total = requirePositive(values.value("input", "Total input"), "Total input")
        FormulaResult(values.value("output", "Useful output") / total * 100)
    },
)

internal fun statisticsFormulas(): List<FormulaDefinition> = listOf(
    formula(
        id = "mean_from_sum",
        name = "Mean from sum and count",
        category = FormulaCategory.STATISTICS,
        expression = "Mean = sum ÷ count",
        description = "Arithmetic mean when the sum is already known.",
        inputs = listOf(input("sum", "Sum of values"), input("count", "Number of values", kind = InputKind.INTEGER)),
        output = FormulaOutput("Mean"),
    ) { values ->
        val count = requireInteger(values.value("count", "Number of values"), "Number of values")
        if (count <= 0) throw FormulaException("The number of values must be positive")
        FormulaResult(values.value("sum", "Sum of values") / count)
    },
    formula(
        id = "weighted_mean",
        name = "Weighted mean",
        category = FormulaCategory.STATISTICS,
        expression = "x̄ = (x₁w₁ + x₂w₂) ÷ (w₁ + w₂)",
        description = "Mean of two values with different weights.",
        inputs = listOf(
            input("x1", "Value 1"),
            input("w1", "Weight 1"),
            input("x2", "Value 2"),
            input("w2", "Weight 2"),
        ),
        output = FormulaOutput("Weighted mean"),
    ) { values ->
        val w1 = requireNonNegative(values.value("w1", "Weight 1"), "Weight 1")
        val w2 = requireNonNegative(values.value("w2", "Weight 2"), "Weight 2")
        if (w1 + w2 == 0.0) throw FormulaException("The total weight must be greater than zero")
        FormulaResult((values.value("x1", "Value 1") * w1 + values.value("x2", "Value 2") * w2) / (w1 + w2))
    },
    formula(
        id = "z_score",
        name = "Z-score",
        category = FormulaCategory.STATISTICS,
        expression = "z = (x − μ) ÷ σ",
        description = "How many standard deviations a value is from the mean.",
        inputs = listOf(input("x", "Value x"), input("mean", "Mean μ"), input("sd", "Standard deviation σ")),
        output = FormulaOutput("Z-score"),
    ) { values ->
        val sd = requirePositive(values.value("sd", "Standard deviation σ"), "Standard deviation")
        FormulaResult((values.value("x", "Value x") - values.value("mean", "Mean μ")) / sd)
    },
    formula(
        id = "permutations",
        name = "Permutations nPr",
        category = FormulaCategory.STATISTICS,
        expression = "P(n, r) = n! ÷ (n − r)!",
        description = "Number of ordered arrangements.",
        inputs = listOf(
            input("n", "n", kind = InputKind.INTEGER),
            input("r", "r", kind = InputKind.INTEGER),
        ),
        output = FormulaOutput("Permutations"),
    ) { values ->
        val n = requireInteger(values.value("n", "n"), "n")
        val r = requireInteger(values.value("r", "r"), "r")
        if (n < 0 || r < 0) throw FormulaException("n and r must be non-negative")
        if (r > n) throw FormulaException("r cannot be greater than n")
        FormulaResult(Probability.permutations(n.toInt(), r.toInt()).toDouble())
    },
    formula(
        id = "combinations",
        name = "Combinations nCr",
        category = FormulaCategory.STATISTICS,
        expression = "C(n, r) = n! ÷ (r!(n − r)!)",
        description = "Number of unordered selections.",
        inputs = listOf(
            input("n", "n", kind = InputKind.INTEGER),
            input("r", "r", kind = InputKind.INTEGER),
        ),
        output = FormulaOutput("Combinations"),
    ) { values ->
        val n = requireInteger(values.value("n", "n"), "n")
        val r = requireInteger(values.value("r", "r"), "r")
        if (n < 0 || r < 0) throw FormulaException("n and r must be non-negative")
        if (r > n) throw FormulaException("r cannot be greater than n")
        FormulaResult(Probability.combinations(n.toInt(), r.toInt()).toDouble())
    },
    formula(
        id = "factorial",
        name = "Factorial",
        category = FormulaCategory.STATISTICS,
        expression = "n! = n × (n−1) × … × 1",
        description = "Product of all positive integers up to n.",
        inputs = listOf(input("n", "n", kind = InputKind.INTEGER)),
        output = FormulaOutput("n!"),
    ) { values ->
        val n = requireInteger(values.value("n", "n"), "n")
        if (n < 0) throw FormulaException("Factorial requires a non-negative integer")
        FormulaResult(Probability.factorial(n.toInt()).toDouble())
    },
    formula(
        id = "event_probability",
        name = "Event probability",
        category = FormulaCategory.STATISTICS,
        expression = "P(E) = favourable ÷ total",
        description = "Probability from a count of equally likely outcomes.",
        inputs = listOf(
            input("favourable", "Favourable outcomes", kind = InputKind.INTEGER),
            input("total", "Total outcomes", kind = InputKind.INTEGER),
        ),
        output = FormulaOutput("Probability"),
    ) { values ->
        val favourable = requireInteger(values.value("favourable", "Favourable outcomes"), "Favourable outcomes")
        val total = requireInteger(values.value("total", "Total outcomes"), "Total outcomes")
        if (total <= 0) throw FormulaException("Total outcomes must be greater than zero")
        if (favourable < 0 || favourable > total) throw FormulaException("Favourable outcomes must be between 0 and the total")
        FormulaResult(favourable.toDouble() / total)
    },
    formula(
        id = "complement_probability",
        name = "Complement probability",
        category = FormulaCategory.STATISTICS,
        expression = "P(not E) = 1 − P(E)",
        description = "Probability that an event does not happen.",
        inputs = listOf(input("p", "P(E)")),
        output = FormulaOutput("P(not E)"),
    ) { values ->
        val probability = values.value("p", "P(E)")
        if (probability < 0 || probability > 1) throw FormulaException("A probability must be between 0 and 1")
        FormulaResult(1 - probability)
    },
    formula(
        id = "independent_events",
        name = "Independent events (both)",
        category = FormulaCategory.STATISTICS,
        expression = "P(A and B) = P(A) × P(B)",
        description = "Probability that two independent events both occur.",
        inputs = listOf(input("pa", "P(A)"), input("pb", "P(B)")),
        output = FormulaOutput("P(A and B)"),
    ) { values ->
        val a = values.value("pa", "P(A)")
        val b = values.value("pb", "P(B)")
        if (a !in 0.0..1.0 || b !in 0.0..1.0) throw FormulaException("Probabilities must be between 0 and 1")
        FormulaResult(a * b)
    },
    formula(
        id = "conditional_probability",
        name = "Conditional probability",
        category = FormulaCategory.STATISTICS,
        expression = "P(A|B) = P(A and B) ÷ P(B)",
        description = "Probability of A given that B occurred.",
        inputs = listOf(input("joint", "P(A and B)"), input("pb", "P(B)")),
        output = FormulaOutput("P(A|B)"),
    ) { values ->
        val joint = values.value("joint", "P(A and B)")
        val b = values.value("pb", "P(B)")
        if (joint !in 0.0..1.0) throw FormulaException("P(A and B) must be between 0 and 1")
        if (b <= 0.0 || b > 1.0) throw FormulaException("P(B) must be greater than zero and at most 1")
        if (joint > b) throw FormulaException("P(A and B) cannot exceed P(B)")
        FormulaResult(joint / b)
    },
    formula(
        id = "binomial_probability",
        name = "Binomial probability",
        category = FormulaCategory.STATISTICS,
        expression = "P(X = k) = C(n,k) p^k (1−p)^(n−k)",
        description = "Exactly k successes in n independent trials.",
        inputs = listOf(
            input("n", "Trials n", kind = InputKind.INTEGER),
            input("k", "Successes k", kind = InputKind.INTEGER),
            input("p", "Success probability p"),
        ),
        output = FormulaOutput("Probability"),
        assumptions = "Trials are independent with a constant success probability.",
    ) { values ->
        val trials = requireInteger(values.value("n", "Trials n"), "Trials n")
        val successes = requireInteger(values.value("k", "Successes k"), "Successes k")
        val probability = values.value("p", "Success probability p")
        if (trials < 0 || trials > 50_000) throw FormulaException("Trials must be between 0 and 50,000")
        if (successes < 0 || successes > trials) throw FormulaException("Successes must be between 0 and the number of trials")
        if (probability !in 0.0..1.0) throw FormulaException("The success probability must be between 0 and 1")
        FormulaResult(Probability.binomialProbability(trials.toInt(), successes.toInt(), probability))
    },
)
