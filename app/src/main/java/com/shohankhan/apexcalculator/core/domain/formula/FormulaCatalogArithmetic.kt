package com.shohankhan.apexcalculator.core.domain.formula

import kotlin.math.pow

internal fun percentageFormulas(): List<FormulaDefinition> = listOf(
    formula(
        id = "percentage_of",
        name = "Percentage of a number",
        category = FormulaCategory.PERCENTAGE,
        expression = "P% of N = N × P ÷ 100",
        description = "Finds how much P percent of a number is.",
        inputs = listOf(input("p", "Percentage", "%"), input("n", "Number")),
        output = FormulaOutput("Result"),
        tags = listOf("percent", "of"),
    ) { values ->
        val percent = values.value("p", "Percentage")
        val number = values.value("n", "Number")
        FormulaResult.steps(
            number * percent / 100,
            listOf("${fmt(percent)}% of ${fmt(number)}", "= ${fmt(number)} × ${fmt(percent)} ÷ 100"),
        )
    },
    formula(
        id = "what_percentage",
        name = "What percentage is one number of another",
        category = FormulaCategory.PERCENTAGE,
        expression = "(A ÷ B) × 100%",
        description = "Expresses A as a percentage of B.",
        inputs = listOf(input("a", "Value A"), input("b", "Value B")),
        output = FormulaOutput("Percentage", "%"),
    ) { values ->
        val a = values.value("a", "Value A")
        val b = requireNonZero(values.value("b", "Value B"), "Value B")
        FormulaResult.steps(a / b * 100, listOf("${fmt(a)} ÷ ${fmt(b)} × 100"))
    },
    formula(
        id = "percentage_increase",
        name = "Percentage increase",
        category = FormulaCategory.PERCENTAGE,
        expression = "Increase % = ((New − Old) ÷ Old) × 100",
        description = "Percentage growth from an old value to a new value.",
        inputs = listOf(input("old", "Original value"), input("new", "New value")),
        output = FormulaOutput("Increase", "%"),
    ) { values ->
        val old = requireNonZero(values.value("old", "Original value"), "Original value")
        val new = values.value("new", "New value")
        FormulaResult.steps((new - old) / old * 100, listOf("(${fmt(new)} − ${fmt(old)}) ÷ ${fmt(old)} × 100"))
    },
    formula(
        id = "percentage_decrease",
        name = "Percentage decrease",
        category = FormulaCategory.PERCENTAGE,
        expression = "Decrease % = ((Old − New) ÷ Old) × 100",
        description = "Percentage reduction from an old value to a new value.",
        inputs = listOf(input("old", "Original value"), input("new", "New value")),
        output = FormulaOutput("Decrease", "%"),
    ) { values ->
        val old = requireNonZero(values.value("old", "Original value"), "Original value")
        val new = values.value("new", "New value")
        FormulaResult.steps((old - new) / old * 100, listOf("(${fmt(old)} − ${fmt(new)}) ÷ ${fmt(old)} × 100"))
    },
    formula(
        id = "percentage_change",
        name = "Percentage change",
        category = FormulaCategory.PERCENTAGE,
        expression = "Change % = ((New − Old) ÷ Old) × 100",
        description = "Signed percentage change; negative values mean a decrease.",
        inputs = listOf(input("old", "Original value"), input("new", "New value")),
        output = FormulaOutput("Change", "%"),
    ) { values ->
        val old = requireNonZero(values.value("old", "Original value"), "Original value")
        val new = values.value("new", "New value")
        FormulaResult((new - old) / old * 100)
    },
    formula(
        id = "reverse_percentage",
        name = "Original value before a percentage change",
        category = FormulaCategory.PERCENTAGE,
        expression = "Original = Final ÷ (1 ± P ÷ 100)",
        description = "Finds the original amount when the final amount and the change are known.",
        inputs = listOf(
            input("final", "Final value"),
            input("p", "Percentage", "%"),
            input("increase", "Is it an increase? (1 = yes, 0 = no)"),
        ),
        output = FormulaOutput("Original value"),
    ) { values ->
        val final = values.value("final", "Final value")
        val percent = values.value("p", "Percentage")
        val increase = values.value("increase", "Increase flag")
        val divisor = if (increase >= 0.5) 1 + percent / 100 else 1 - percent / 100
        if (divisor == 0.0) throw FormulaException("A 100% decrease has no original value")
        FormulaResult.steps(final / divisor, listOf("Divisor = 1 ${if (increase >= 0.5) "+" else "−"} ${fmt(percent)} ÷ 100"))
    },
    formula(
        id = "reverse_increase",
        name = "Original value before an increase",
        category = FormulaCategory.PERCENTAGE,
        expression = "Original = Final ÷ (1 + P ÷ 100)",
        description = "Removes a percentage increase from a final value.",
        inputs = listOf(input("final", "Increased value"), input("p", "Increase", "%")),
        output = FormulaOutput("Original value"),
    ) { values ->
        val final = values.value("final", "Increased value")
        val percent = values.value("p", "Increase")
        val divisor = 1 + percent / 100
        if (divisor == 0.0) throw FormulaException("The divisor cannot be zero")
        FormulaResult(final / divisor)
    },
    formula(
        id = "reverse_decrease",
        name = "Original value before a decrease",
        category = FormulaCategory.PERCENTAGE,
        expression = "Original = Final ÷ (1 − P ÷ 100)",
        description = "Removes a percentage decrease from a final value.",
        inputs = listOf(input("final", "Reduced value"), input("p", "Decrease", "%")),
        output = FormulaOutput("Original value"),
    ) { values ->
        val final = values.value("final", "Reduced value")
        val percent = values.value("p", "Decrease")
        if (percent >= 100) throw FormulaException("A decrease of 100% or more has no valid original value")
        FormulaResult(final / (1 - percent / 100))
    },
    formula(
        id = "percentage_points",
        name = "Percentage-point difference",
        category = FormulaCategory.PERCENTAGE,
        expression = "Difference (pp) = A% − B%",
        description = "Difference between two percentages measured in percentage points.",
        inputs = listOf(input("a", "First percentage", "%"), input("b", "Second percentage", "%")),
        output = FormulaOutput("Difference", "pp"),
    ) { values -> FormulaResult(values.value("a", "First percentage") - values.value("b", "Second percentage")) },
    formula(
        id = "weighted_percentage",
        name = "Weighted percentage",
        category = FormulaCategory.PERCENTAGE,
        expression = "Weighted % = Σ(score × weight) ÷ Σweight",
        description = "Combines two weighted percentage scores.",
        inputs = listOf(
            input("score1", "Score 1", "%"),
            input("weight1", "Weight 1"),
            input("score2", "Score 2", "%"),
            input("weight2", "Weight 2"),
        ),
        output = FormulaOutput("Weighted percentage", "%"),
    ) { values ->
        val w1 = requireNonNegative(values.value("weight1", "Weight 1"), "Weight 1")
        val w2 = requireNonNegative(values.value("weight2", "Weight 2"), "Weight 2")
        val total = w1 + w2
        if (total == 0.0) throw FormulaException("The total weight must be greater than zero")
        val weighted = (values.value("score1", "Score 1") * w1 + values.value("score2", "Score 2") * w2) / total
        FormulaResult.steps(weighted, listOf("Total weight = ${fmt(total)}"))
    },
    formula(
        id = "discount_amount",
        name = "Discount amount",
        category = FormulaCategory.PERCENTAGE,
        expression = "Discount = Price × Rate ÷ 100",
        description = "Money saved for a given discount rate.",
        inputs = listOf(input("price", "Original price"), input("rate", "Discount rate", "%")),
        output = FormulaOutput("Discount amount"),
    ) { values ->
        val price = requireNonNegative(values.value("price", "Original price"), "Original price")
        val rate = requireNonNegative(values.value("rate", "Discount rate"), "Discount rate")
        FormulaResult(price * rate / 100)
    },
    formula(
        id = "discounted_price",
        name = "Price after a discount",
        category = FormulaCategory.PERCENTAGE,
        expression = "Final = Price × (1 − Rate ÷ 100)",
        description = "Selling price after a single discount.",
        inputs = listOf(input("price", "Original price"), input("rate", "Discount rate", "%")),
        output = FormulaOutput("Final price"),
    ) { values ->
        val price = requireNonNegative(values.value("price", "Original price"), "Original price")
        val rate = values.value("rate", "Discount rate")
        if (rate > 100) throw FormulaException("A discount rate above 100% would give a negative price")
        FormulaResult.steps(price * (1 - rate / 100), listOf("Multiplier = 1 − ${fmt(rate)} ÷ 100"))
    },
    formula(
        id = "successive_discounts",
        name = "Successive discounts",
        category = FormulaCategory.PERCENTAGE,
        expression = "Final = Price × (1 − d₁) × (1 − d₂)",
        description = "Applies two discounts one after another.",
        inputs = listOf(
            input("price", "Original price"),
            input("d1", "First discount", "%"),
            input("d2", "Second discount", "%"),
        ),
        output = FormulaOutput("Final price"),
    ) { values ->
        val price = requireNonNegative(values.value("price", "Original price"), "Original price")
        val d1 = values.value("d1", "First discount")
        val d2 = values.value("d2", "Second discount")
        if (d1 > 100 || d2 > 100) throw FormulaException("Discount rates cannot exceed 100%")
        val final = price * (1 - d1 / 100) * (1 - d2 / 100)
        val effective = (1 - (1 - d1 / 100) * (1 - d2 / 100)) * 100
        FormulaResult(final, listOf("Effective discount" to "${fmt(effective)}%"))
    },
    formula(
        id = "tax_amount",
        name = "Tax amount",
        category = FormulaCategory.PERCENTAGE,
        expression = "Tax = Price × Rate ÷ 100",
        description = "Tax charged on a price at a given rate.",
        inputs = listOf(input("price", "Price"), input("rate", "Tax rate", "%")),
        output = FormulaOutput("Tax amount"),
    ) { values ->
        FormulaResult(
            requireNonNegative(values.value("price", "Price"), "Price") *
                values.value("rate", "Tax rate") / 100,
        )
    },
    formula(
        id = "price_with_tax",
        name = "Price including tax",
        category = FormulaCategory.PERCENTAGE,
        expression = "Total = Price × (1 + Rate ÷ 100)",
        description = "Final price once tax is added.",
        inputs = listOf(input("price", "Price before tax"), input("rate", "Tax rate", "%")),
        output = FormulaOutput("Total price"),
    ) { values ->
        val price = requireNonNegative(values.value("price", "Price before tax"), "Price")
        val rate = values.value("rate", "Tax rate")
        FormulaResult(price * (1 + rate / 100))
    },
    formula(
        id = "markup",
        name = "Markup percentage",
        category = FormulaCategory.PERCENTAGE,
        expression = "Markup % = ((Price − Cost) ÷ Cost) × 100",
        description = "Markup on cost, as opposed to margin on price.",
        inputs = listOf(input("cost", "Cost"), input("price", "Selling price")),
        output = FormulaOutput("Markup", "%"),
    ) { values ->
        val cost = requirePositive(values.value("cost", "Cost"), "Cost")
        val price = values.value("price", "Selling price")
        FormulaResult.steps((price - cost) / cost * 100, listOf("Profit = ${fmt(price - cost)}"))
    },
    formula(
        id = "profit",
        name = "Profit",
        category = FormulaCategory.PERCENTAGE,
        expression = "Profit = Selling price − Cost",
        description = "Absolute profit for a sale.",
        inputs = listOf(input("price", "Selling price"), input("cost", "Cost")),
        output = FormulaOutput("Profit"),
    ) { values -> FormulaResult(values.value("price", "Selling price") - values.value("cost", "Cost")) },
    formula(
        id = "loss",
        name = "Loss",
        category = FormulaCategory.PERCENTAGE,
        expression = "Loss = Cost − Selling price",
        description = "Absolute loss for a sale.",
        inputs = listOf(input("cost", "Cost"), input("price", "Selling price")),
        output = FormulaOutput("Loss"),
    ) { values -> FormulaResult(values.value("cost", "Cost") - values.value("price", "Selling price")) },
    formula(
        id = "profit_margin",
        name = "Profit margin",
        category = FormulaCategory.PERCENTAGE,
        expression = "Margin % = ((Price − Cost) ÷ Price) × 100",
        description = "Margin as a share of the selling price.",
        inputs = listOf(input("price", "Selling price"), input("cost", "Cost")),
        output = FormulaOutput("Margin", "%"),
    ) { values ->
        val price = requireNonZero(values.value("price", "Selling price"), "Selling price")
        val cost = values.value("cost", "Cost")
        FormulaResult((price - cost) / price * 100)
    },
    formula(
        id = "cost_from_margin",
        name = "Cost from selling price and margin",
        category = FormulaCategory.PERCENTAGE,
        expression = "Cost = Price × (1 − Margin ÷ 100)",
        description = "Derives the cost of an item from its margin.",
        inputs = listOf(input("price", "Selling price"), input("margin", "Margin", "%")),
        output = FormulaOutput("Cost"),
    ) { values ->
        val price = values.value("price", "Selling price")
        val margin = values.value("margin", "Margin")
        FormulaResult(price * (1 - margin / 100))
    },
    formula(
        id = "simplify_ratio",
        name = "Simplify a ratio",
        category = FormulaCategory.PERCENTAGE,
        expression = "a : b = (a ÷ gcd) : (b ÷ gcd)",
        description = "Reduces a two-part ratio to its lowest terms.",
        inputs = listOf(
            input("a", "First term", kind = InputKind.INTEGER),
            input("b", "Second term", kind = InputKind.INTEGER),
        ),
        output = FormulaOutput("Ratio (first term)"),
    ) { values ->
        val a = requireInteger(values.value("a", "First term"), "First term")
        val b = requireInteger(values.value("b", "Second term"), "Second term")
        if (a == 0L && b == 0L) throw FormulaException("A ratio cannot have both terms equal to zero")
        if (b == 0L) throw FormulaException("The second term cannot be zero")
        val gcd = gcdOf(a, b)
        FormulaResult(a / gcd.toDouble(), listOf("Simplified ratio" to "${a / gcd} : ${b / gcd}"))
    },
    formula(
        id = "proportion_solve",
        name = "Solve a proportion",
        category = FormulaCategory.PERCENTAGE,
        expression = "a ÷ b = c ÷ x  ⇒  x = b × c ÷ a",
        description = "Finds the missing term of a proportion.",
        inputs = listOf(input("a", "a"), input("b", "b"), input("c", "c")),
        output = FormulaOutput("x"),
    ) { values ->
        val a = requireNonZero(values.value("a", "a"), "a")
        val b = values.value("b", "b")
        val c = values.value("c", "c")
        FormulaResult.steps(b * c / a, listOf("x = ${fmt(b)} × ${fmt(c)} ÷ ${fmt(a)}"))
    },
    formula(
        id = "direct_proportion",
        name = "Direct proportion",
        category = FormulaCategory.PERCENTAGE,
        expression = "y = k × x",
        description = "Finds y for a known constant of proportionality.",
        inputs = listOf(input("k", "Constant k"), input("x", "x")),
        output = FormulaOutput("y"),
    ) { values -> FormulaResult(values.value("k", "Constant k") * values.value("x", "x")) },
    formula(
        id = "inverse_proportion",
        name = "Inverse proportion",
        category = FormulaCategory.PERCENTAGE,
        expression = "y = k ÷ x",
        description = "Finds y when it is inversely proportional to x.",
        inputs = listOf(input("k", "Constant k"), input("x", "x")),
        output = FormulaOutput("y"),
    ) { values ->
        val k = values.value("k", "Constant k")
        val x = requireNonZero(values.value("x", "x"), "x")
        FormulaResult(k / x)
    },
    formula(
        id = "average",
        name = "Average of two values",
        category = FormulaCategory.PERCENTAGE,
        expression = "Mean = (a + b) ÷ 2",
        description = "Arithmetic mean of two numbers. Use Math Tools for longer data sets.",
        inputs = listOf(input("a", "First value"), input("b", "Second value")),
        output = FormulaOutput("Average"),
    ) { values -> FormulaResult((values.value("a", "First value") + values.value("b", "Second value")) / 2) },
    formula(
        id = "weighted_average",
        name = "Weighted average of two values",
        category = FormulaCategory.PERCENTAGE,
        expression = "Weighted mean = (a·w₁ + b·w₂) ÷ (w₁ + w₂)",
        description = "Averages two values that carry different weights.",
        inputs = listOf(
            input("a", "Value a"),
            input("w1", "Weight of a"),
            input("b", "Value b"),
            input("w2", "Weight of b"),
        ),
        output = FormulaOutput("Weighted average"),
    ) { values ->
        val w1 = requireNonNegative(values.value("w1", "Weight of a"), "Weight of a")
        val w2 = requireNonNegative(values.value("w2", "Weight of b"), "Weight of b")
        if (w1 + w2 == 0.0) throw FormulaException("The total weight must be greater than zero")
        FormulaResult((values.value("a", "Value a") * w1 + values.value("b", "Value b") * w2) / (w1 + w2))
    },
    formula(
        id = "missing_average_value",
        name = "Missing value for a target average",
        category = FormulaCategory.PERCENTAGE,
        expression = "x = target × count − known sum",
        description = "Finds the value needed to reach a target average.",
        inputs = listOf(
            input("target", "Target average"),
            input("count", "Number of values", kind = InputKind.INTEGER),
            input("sum", "Sum of the known values"),
        ),
        output = FormulaOutput("Required value"),
    ) { values ->
        val count = requireInteger(values.value("count", "Number of values"), "Number of values")
        if (count <= 0) throw FormulaException("The number of values must be positive")
        val target = values.value("target", "Target average")
        val knownSum = values.value("sum", "Sum of the known values")
        FormulaResult.steps(target * count - knownSum, listOf("Target total = ${fmt(target)} × $count"))
    },
)

private fun requireNonZero(value: Double, label: String): Double {
    if (!value.isFinite()) throw FormulaException("$label must be a finite number")
    if (value == 0.0) throw FormulaException("$label cannot be zero")
    return value
}

private fun gcdOf(a: Long, b: Long): Long {
    var x = kotlin.math.abs(a)
    var y = kotlin.math.abs(b)
    while (y != 0L) {
        val temporary = y
        y = x % y
        x = temporary
    }
    return if (x == 0L) 1 else x
}

internal fun financeFormulas(): List<FormulaDefinition> = listOf(
    formula(
        id = "simple_interest",
        name = "Simple interest",
        category = FormulaCategory.FINANCE,
        expression = "I = P × R × T ÷ 100",
        description = "Interest that is calculated only on the principal.",
        inputs = listOf(
            input("p", "Principal"),
            input("r", "Annual rate", "%"),
            input("t", "Time", "years"),
        ),
        output = FormulaOutput("Interest"),
        assumptions = "Rate is an annual simple rate; interest does not compound.",
        tags = listOf("interest"),
    ) { values ->
        val principal = requirePositive(values.value("p", "Principal"), "Principal")
        val rate = values.value("r", "Annual rate")
        val time = requireNonNegative(values.value("t", "Time"), "Time")
        FormulaResult.steps(
            principal * rate * time / 100,
            listOf("${fmt(principal)} × ${fmt(rate)} × ${fmt(time)} ÷ 100"),
        )
    },
    formula(
        id = "total_amount_simple",
        name = "Total amount with simple interest",
        category = FormulaCategory.FINANCE,
        expression = "A = P × (1 + R × T ÷ 100)",
        description = "Principal plus simple interest.",
        inputs = listOf(
            input("p", "Principal"),
            input("r", "Annual rate", "%"),
            input("t", "Time", "years"),
        ),
        output = FormulaOutput("Total amount"),
    ) { values ->
        val principal = requirePositive(values.value("p", "Principal"), "Principal")
        val rate = values.value("r", "Annual rate")
        val time = requireNonNegative(values.value("t", "Time"), "Time")
        val interest = principal * rate * time / 100
        FormulaResult(principal + interest, listOf("Interest" to fmt(interest)))
    },
    formula(
        id = "compound_interest",
        name = "Compound interest",
        category = FormulaCategory.FINANCE,
        expression = "A = P × (1 + R ÷ (100·n))^(n·T)",
        description = "Future value with periodic compounding.",
        inputs = listOf(
            input("p", "Principal"),
            input("r", "Annual rate", "%"),
            input("t", "Time", "years"),
            input("n", "Compounds per year", hint = "1 = yearly, 12 = monthly, 4 = quarterly", kind = InputKind.INTEGER),
        ),
        output = FormulaOutput("Total amount"),
        assumptions = "The rate is nominal annual and compounds n times per year.",
    ) { values ->
        val principal = requirePositive(values.value("p", "Principal"), "Principal")
        val rate = values.value("r", "Annual rate")
        val time = requireNonNegative(values.value("t", "Time"), "Time")
        val compounds = requireInteger(values.value("n", "Compounds per year"), "Compounds per year")
        if (compounds <= 0) throw FormulaException("Compounds per year must be at least 1")
        val amount = principal * (1 + rate / 100 / compounds).pow((compounds * time).toDouble())
        if (!amount.isFinite()) throw FormulaException("The result is too large to represent")
        FormulaResult(amount, listOf("Interest" to fmt(amount - principal)))
    },
    formula(
        id = "principal_from_interest",
        name = "Principal from simple interest",
        category = FormulaCategory.FINANCE,
        expression = "P = I × 100 ÷ (R × T)",
        description = "Finds the principal that produces a given simple interest.",
        inputs = listOf(
            input("i", "Interest"),
            input("r", "Annual rate", "%"),
            input("t", "Time", "years"),
        ),
        output = FormulaOutput("Principal"),
    ) { values ->
        val interest = values.value("i", "Interest")
        val rate = values.value("r", "Annual rate")
        val time = requirePositive(values.value("t", "Time"), "Time")
        if (rate == 0.0) throw FormulaException("The rate cannot be zero")
        FormulaResult(interest * 100 / (rate * time))
    },
    formula(
        id = "rate_from_interest",
        name = "Interest rate from simple interest",
        category = FormulaCategory.FINANCE,
        expression = "R = I × 100 ÷ (P × T)",
        description = "Annual simple interest rate implied by a payment.",
        inputs = listOf(
            input("i", "Interest"),
            input("p", "Principal"),
            input("t", "Time", "years"),
        ),
        output = FormulaOutput("Annual rate", "%"),
    ) { values ->
        val interest = values.value("i", "Interest")
        val principal = requirePositive(values.value("p", "Principal"), "Principal")
        val time = requirePositive(values.value("t", "Time"), "Time")
        FormulaResult(interest * 100 / (principal * time))
    },
    formula(
        id = "time_from_interest",
        name = "Time from simple interest",
        category = FormulaCategory.FINANCE,
        expression = "T = I × 100 ÷ (P × R)",
        description = "Time needed to earn a given simple interest.",
        inputs = listOf(
            input("i", "Interest"),
            input("p", "Principal"),
            input("r", "Annual rate", "%"),
        ),
        output = FormulaOutput("Time", "years"),
    ) { values ->
        val interest = values.value("i", "Interest")
        val principal = requirePositive(values.value("p", "Principal"), "Principal")
        val rate = values.value("r", "Annual rate")
        if (rate == 0.0) throw FormulaException("The rate cannot be zero")
        FormulaResult(interest * 100 / (principal * rate))
    },
    formula(
        id = "effective_annual_rate",
        name = "Effective annual rate",
        category = FormulaCategory.FINANCE,
        expression = "EAR = (1 + R ÷ n)^n − 1",
        description = "Annual rate that includes the effect of compounding.",
        inputs = listOf(
            input("r", "Nominal annual rate", "%"),
            input("n", "Compounds per year", kind = InputKind.INTEGER),
        ),
        output = FormulaOutput("Effective annual rate", "%"),
    ) { values ->
        val rate = values.value("r", "Nominal annual rate")
        val compounds = requireInteger(values.value("n", "Compounds per year"), "Compounds per year")
        if (compounds <= 0) throw FormulaException("Compounds per year must be at least 1")
        FormulaResult(((1 + rate / 100 / compounds).pow(compounds.toDouble()) - 1) * 100)
    },
    formula(
        id = "present_value",
        name = "Present value",
        category = FormulaCategory.FINANCE,
        expression = "PV = FV ÷ (1 + r)^t",
        description = "Value today of a future amount.",
        inputs = listOf(
            input("fv", "Future value"),
            input("r", "Annual rate", "%"),
            input("t", "Time", "years"),
        ),
        output = FormulaOutput("Present value"),
    ) { values ->
        val future = values.value("fv", "Future value")
        val rate = values.value("r", "Annual rate") / 100
        val time = requireNonNegative(values.value("t", "Time"), "Time")
        if (1 + rate <= 0) throw FormulaException("The discount factor must be positive")
        FormulaResult(future / (1 + rate).pow(time))
    },
    formula(
        id = "future_value",
        name = "Future value of a lump sum",
        category = FormulaCategory.FINANCE,
        expression = "FV = PV × (1 + r)^t",
        description = "Value in the future of an amount invested today.",
        inputs = listOf(
            input("pv", "Present value"),
            input("r", "Annual rate", "%"),
            input("t", "Time", "years"),
        ),
        output = FormulaOutput("Future value"),
    ) { values ->
        val present = values.value("pv", "Present value")
        val rate = values.value("r", "Annual rate") / 100
        val time = requireNonNegative(values.value("t", "Time"), "Time")
        if (1 + rate <= 0) throw FormulaException("The growth factor must be positive")
        FormulaResult(present * (1 + rate).pow(time))
    },
    formula(
        id = "emi",
        name = "Loan EMI",
        category = FormulaCategory.FINANCE,
        expression = "EMI = P·i·(1+i)^n ÷ ((1+i)^n − 1)",
        description = "Equal monthly instalment for a reducing-balance loan.",
        inputs = listOf(
            input("p", "Loan amount"),
            input("r", "Annual interest rate", "%"),
            input("years", "Loan period", "years"),
        ),
        output = FormulaOutput("Monthly instalment"),
        assumptions = "Monthly payments at the end of each month, fixed rate, no fees. This is a mathematical calculator, not financial advice.",
    ) { values ->
        val principal = requirePositive(values.value("p", "Loan amount"), "Loan amount")
        val annual = requireNonNegative(values.value("r", "Annual interest rate"), "Annual interest rate")
        val years = requirePositive(values.value("years", "Loan period"), "Loan period")
        val months = years * 12
        val monthlyRate = annual / 100 / 12
        val emi = if (monthlyRate == 0.0) {
            principal / months
        } else {
            val growth = (1 + monthlyRate).pow(months)
            principal * monthlyRate * growth / (growth - 1)
        }
        val total = emi * months
        FormulaResult(
            emi,
            listOf(
                "Number of payments" to fmt(months),
                "Total repayment" to fmt(total),
                "Total interest" to fmt(total - principal),
            ),
        )
    },
    formula(
        id = "total_loan_interest",
        name = "Total interest on a loan",
        category = FormulaCategory.FINANCE,
        expression = "Total interest = EMI × n − P",
        description = "Interest paid over the whole life of the loan.",
        inputs = listOf(input("emi", "Monthly instalment"), input("months", "Number of months"), input("p", "Loan amount")),
        output = FormulaOutput("Total interest"),
    ) { values ->
        val emi = requirePositive(values.value("emi", "Monthly instalment"), "Monthly instalment")
        val months = requirePositive(values.value("months", "Number of months"), "Number of months")
        val principal = requirePositive(values.value("p", "Loan amount"), "Loan amount")
        FormulaResult(emi * months - principal)
    },
    formula(
        id = "down_payment",
        name = "Down payment",
        category = FormulaCategory.FINANCE,
        expression = "Down payment = Price × Percent ÷ 100",
        description = "Up-front payment for a financed purchase.",
        inputs = listOf(input("price", "Price"), input("percent", "Down payment", "%")),
        output = FormulaOutput("Down payment"),
    ) { values ->
        val price = requireNonNegative(values.value("price", "Price"), "Price")
        val percent = values.value("percent", "Down payment")
        if (percent > 100) throw FormulaException("The down payment cannot exceed 100%")
        FormulaResult(price * percent / 100, listOf("Financed amount" to fmt(price * (1 - percent / 100))))
    },
    formula(
        id = "installment_amount",
        name = "Equal instalments without interest",
        category = FormulaCategory.FINANCE,
        expression = "Instalment = Amount ÷ Months",
        description = "Splits an amount into equal interest-free instalments.",
        inputs = listOf(input("amount", "Amount"), input("months", "Number of months", kind = InputKind.INTEGER)),
        output = FormulaOutput("Monthly instalment"),
    ) { values ->
        val amount = values.value("amount", "Amount")
        val months = requireInteger(values.value("months", "Number of months"), "Number of months")
        if (months <= 0) throw FormulaException("The number of months must be positive")
        FormulaResult(amount / months)
    },
    formula(
        id = "savings_growth",
        name = "Savings growth with monthly deposits",
        category = FormulaCategory.FINANCE,
        expression = "FV = P(1+i)^n + D·((1+i)^n − 1) ÷ i",
        description = "Future value of an initial amount plus regular monthly deposits.",
        inputs = listOf(
            input("p", "Initial amount"),
            input("d", "Monthly deposit"),
            input("r", "Annual rate", "%"),
            input("years", "Time", "years"),
        ),
        output = FormulaOutput("Future value"),
        assumptions = "Deposits are made at the end of each month and the rate stays constant.",
    ) { values ->
        val initial = requireNonNegative(values.value("p", "Initial amount"), "Initial amount")
        val deposit = requireNonNegative(values.value("d", "Monthly deposit"), "Monthly deposit")
        val rate = values.value("r", "Annual rate") / 100 / 12
        val months = requirePositive(values.value("years", "Time"), "Time") * 12
        val growth = (1 + rate).pow(months)
        val future = if (rate == 0.0) {
            initial + deposit * months
        } else {
            initial * growth + deposit * (growth - 1) / rate
        }
        FormulaResult(future, listOf("Total deposited" to fmt(initial + deposit * months)))
    },
)

internal fun timeSpeedFormulas(): List<FormulaDefinition> = listOf(
    formula(
        id = "distance",
        name = "Distance from speed and time",
        category = FormulaCategory.TIME_SPEED,
        expression = "d = s × t",
        description = "Distance covered at a constant speed.",
        inputs = listOf(input("s", "Speed", "km/h"), input("t", "Time", "hours")),
        output = FormulaOutput("Distance", "km"),
    ) { values -> FormulaResult(values.value("s", "Speed") * values.value("t", "Time")) },
    formula(
        id = "speed",
        name = "Speed from distance and time",
        category = FormulaCategory.TIME_SPEED,
        expression = "s = d ÷ t",
        description = "Average speed for a journey.",
        inputs = listOf(input("d", "Distance", "km"), input("t", "Time", "hours")),
        output = FormulaOutput("Speed", "km/h"),
    ) { values ->
        val time = requirePositive(values.value("t", "Time"), "Time")
        FormulaResult(values.value("d", "Distance") / time)
    },
    formula(
        id = "travel_time",
        name = "Time from distance and speed",
        category = FormulaCategory.TIME_SPEED,
        expression = "t = d ÷ s",
        description = "Time needed to cover a distance.",
        inputs = listOf(input("d", "Distance", "km"), input("s", "Speed", "km/h")),
        output = FormulaOutput("Time", "hours"),
    ) { values ->
        val speed = requirePositive(values.value("s", "Speed"), "Speed")
        val hours = values.value("d", "Distance") / speed
        FormulaResult(hours, listOf("In minutes" to fmt(hours * 60)))
    },
    formula(
        id = "average_speed",
        name = "Average speed for two legs",
        category = FormulaCategory.TIME_SPEED,
        expression = "Average = total distance ÷ total time",
        description = "Average speed when the distance and duration of each leg are known.",
        inputs = listOf(
            input("d1", "Distance 1", "km"),
            input("t1", "Time 1", "h"),
            input("d2", "Distance 2", "km"),
            input("t2", "Time 2", "h"),
        ),
        output = FormulaOutput("Average speed", "km/h"),
    ) { values ->
        val totalDistance = values.value("d1", "Distance 1") + values.value("d2", "Distance 2")
        val totalTime = requirePositive(
            values.value("t1", "Time 1") + values.value("t2", "Time 2"),
            "Total time",
        )
        FormulaResult(totalDistance / totalTime, listOf("Total distance" to fmt(totalDistance), "Total time" to fmt(totalTime)))
    },
    formula(
        id = "relative_speed",
        name = "Relative speed",
        category = FormulaCategory.TIME_SPEED,
        expression = "Opposite directions: s₁ + s₂ · Same direction: |s₁ − s₂|",
        description = "Closing or separating speed of two moving objects.",
        inputs = listOf(
            input("s1", "Speed 1", "km/h"),
            input("s2", "Speed 2", "km/h"),
            input("same_direction", "Same direction? (1 = yes, 0 = no)"),
        ),
        output = FormulaOutput("Relative speed", "km/h"),
    ) { values ->
        val s1 = values.value("s1", "Speed 1")
        val s2 = values.value("s2", "Speed 2")
        val same = values.value("same_direction", "Direction flag") >= 0.5
        FormulaResult(if (same) kotlin.math.abs(s1 - s2) else s1 + s2)
    },
    formula(
        id = "work_rate",
        name = "Work rate",
        category = FormulaCategory.TIME_SPEED,
        expression = "Rate = work ÷ time",
        description = "Units of work completed per unit of time.",
        inputs = listOf(input("work", "Work"), input("time", "Time", "hours")),
        output = FormulaOutput("Rate", "work/hour"),
    ) { values ->
        val time = requirePositive(values.value("time", "Time"), "Time")
        FormulaResult(values.value("work", "Work") / time)
    },
    formula(
        id = "combined_work",
        name = "Combined work time",
        category = FormulaCategory.TIME_SPEED,
        expression = "Time = 1 ÷ (1/a + 1/b)",
        description = "Time for two workers to finish a job together.",
        inputs = listOf(input("a", "Worker A time", "hours"), input("b", "Worker B time", "hours")),
        output = FormulaOutput("Combined time", "hours"),
        assumptions = "Both workers keep a constant rate while working together.",
    ) { values ->
        val a = requirePositive(values.value("a", "Worker A time"), "Worker A time")
        val b = requirePositive(values.value("b", "Worker B time"), "Worker B time")
        FormulaResult.steps(1 / (1 / a + 1 / b), listOf("Rates: 1/${fmt(a)} + 1/${fmt(b)}"))
    },
    formula(
        id = "productivity",
        name = "Productivity",
        category = FormulaCategory.TIME_SPEED,
        expression = "Productivity = output ÷ time",
        description = "Output produced per unit of time.",
        inputs = listOf(input("output", "Output"), input("time", "Time", "hours")),
        output = FormulaOutput("Productivity", "units/hour"),
    ) { values ->
        val time = requirePositive(values.value("time", "Time"), "Time")
        FormulaResult(values.value("output", "Output") / time)
    },
    formula(
        id = "duration_to_seconds",
        name = "Time duration",
        category = FormulaCategory.TIME_SPEED,
        expression = "Total seconds = h×3600 + m×60 + s",
        description = "Converts a duration into seconds, minutes and hours.",
        inputs = listOf(
            input("h", "Hours", kind = InputKind.INTEGER),
            input("m", "Minutes", kind = InputKind.INTEGER),
            input("s", "Seconds"),
        ),
        output = FormulaOutput("Total seconds", "s"),
    ) { values ->
        val hours = requireNonNegative(values.value("h", "Hours"), "Hours")
        val minutes = requireNonNegative(values.value("m", "Minutes"), "Minutes")
        val seconds = requireNonNegative(values.value("s", "Seconds"), "Seconds")
        val total = hours * 3600 + minutes * 60 + seconds
        FormulaResult(
            total,
            listOf("In minutes" to fmt(total / 60), "In hours" to fmt(total / 3600)),
            listOf("$hours h × 3600 + $minutes min × 60 + ${fmt(seconds)} s"),
        )
    },
)
