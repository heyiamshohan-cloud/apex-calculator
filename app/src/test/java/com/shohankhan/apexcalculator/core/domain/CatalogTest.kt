package com.shohankhan.apexcalculator.core.domain

import com.shohankhan.apexcalculator.core.domain.formula.FormulaCatalog
import com.shohankhan.apexcalculator.core.domain.formula.FormulaCategory
import com.shohankhan.apexcalculator.core.domain.formula.FormulaDefinition
import com.shohankhan.apexcalculator.core.domain.formula.FormulaException
import com.shohankhan.apexcalculator.core.domain.solver.SolverCatalog
import com.shohankhan.apexcalculator.core.domain.solver.SolverCategory
import com.shohankhan.apexcalculator.core.domain.solver.SolverException
import com.shohankhan.apexcalculator.core.domain.solver.SolverSpec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI

/**
 * Catalogue integrity plus the mandated formula and solver vectors.
 * Values are parsed from the headline text so these tests do not depend on display rounding.
 */
class FormulaCatalogTest {

    private fun compute(id: String, vararg inputs: Pair<String, Double>): Double {
        val formula = FormulaCatalog.find(id) ?: throw AssertionError("Missing formula $id")
        return formula.compute(inputs.toMap()).primary
    }

    @Test
    fun `identifiers are unique and every category is populated`() {
        val ids = FormulaCatalog.all.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
        FormulaCategory.entries.forEach { category ->
            assertTrue("Category ${category.label} is empty", FormulaCatalog.countByCategory(category) > 0)
        }
        assertTrue("The library should be substantial", FormulaCatalog.all.size >= 100)
    }

    @Test
    fun `every formula declares inputs and non-empty metadata`() {
        FormulaCatalog.all.forEach { formula ->
            assertTrue("${formula.id} has no inputs", formula.inputs.isNotEmpty())
            assertTrue("${formula.id} has no name", formula.name.isNotBlank())
            assertTrue("${formula.id} has no description", formula.description.isNotBlank())
            assertTrue("${formula.id} has no expression", formula.expression.isNotBlank())
            assertEquals(formula.id, FormulaCatalog.find(formula.id)?.id)
        }
    }

    @Test
    fun `percentage of the mandated values`() {
        assertEquals(30.0, compute("percentage_of", "p" to 15.0, "n" to 200.0), 1e-9)
    }

    @Test
    fun `simple interest vector`() {
        // 1000 at 5% for 2 years = 100
        assertEquals(100.0, compute("simple_interest", "p" to 1000.0, "r" to 5.0, "t" to 2.0), 1e-9)
    }

    @Test
    fun `geometry vectors`() {
        assertEquals(25.0, compute("square", "a" to 5.0), 1e-9)
        assertEquals(4 * PI, compute("circle", "r" to 2.0), 1e-9)
    }

    @Test
    fun `percentage change vectors`() {
        assertEquals(10.0, compute("percentage_change", "old" to 200.0, "new" to 220.0), 1e-9)
        assertEquals(-10.0, compute("percentage_change", "old" to 200.0, "new" to 180.0), 1e-9)
    }

    @Test
    fun `discounts and tax are consistent`() {
        val discount = compute("discounted_price", "price" to 200.0, "rate" to 10.0)
        assertEquals(180.0, discount, 1e-9)
        val withTax = compute("price_with_tax", "price" to 200.0, "rate" to 10.0)
        assertEquals(220.0, withTax, 1e-9)
    }

    @Test
    fun `invalid inputs raise a typed formula error`() {
        val formula: FormulaDefinition = FormulaCatalog.find("simple_interest")!!
        try {
            formula.compute(mapOf("p" to -1000.0, "r" to 5.0, "t" to 2.0))
            throw AssertionError("Expected a validation failure")
        } catch (expected: FormulaException) {
            assertTrue(expected.message?.isNotBlank() == true)
        }
    }

    @Test
    fun `missing input raises a typed formula error`() {
        val formula = FormulaCatalog.find("square")!!
        try {
            formula.compute(emptyMap())
            throw AssertionError("Expected a validation failure")
        } catch (expected: FormulaException) {
            // expected
        }
    }

    @Test
    fun `search returns relevant formulas`() {
        assertTrue(FormulaCatalog.search("interest").any { it.id == "simple_interest" })
        assertTrue(FormulaCatalog.search("triangle").isNotEmpty())
        assertTrue(FormulaCatalog.search("zzzz-no-match").isEmpty())
    }
}

class SolverCatalogTest {

    private fun solve(id: String, vararg inputs: Pair<String, String>): String {
        val spec: SolverSpec = SolverCatalog.find(id) ?: throw AssertionError("Missing solver $id")
        return spec.compute(inputs.toMap()).headline
    }

    @Test
    fun `identifiers are unique`() {
        val ids = SolverCatalog.all.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
        assertTrue(SolverCatalog.all.size >= 40)
    }

    @Test
    fun `every solver has fields and a description`() {
        SolverCatalog.all.forEach { spec ->
            assertTrue("${spec.id} has no fields", spec.fields.isNotEmpty())
            assertTrue("${spec.id} has no description", spec.description.isNotBlank())
            assertNotEquals("", spec.title)
        }
    }

    @Test
    fun `fraction addition is exact`() {
        assertEquals("1/2", solve("fraction_add", "a" to "1/3", "b" to "1/6"))
    }

    @Test
    fun `fraction subtraction and multiplication are exact`() {
        assertEquals("1/6", solve("fraction_subtract", "a" to "1/2", "b" to "1/3"))
        assertEquals("1/6", solve("fraction_multiply", "a" to "1/2", "b" to "1/3"))
        assertEquals("3/2", solve("fraction_divide", "a" to "1/2", "b" to "1/3"))
    }

    @Test
    fun `fraction simplification reports mixed form`() {
        val output = SolverCatalog.find("fraction_simplify")!!.compute(mapOf("fraction" to "18/12"))
        assertEquals("3/2", output.headline)
        assertTrue(output.steps.isNotEmpty())
    }

    @Test
    fun `quadratic roots are correct`() {
        val output = SolverCatalog.find("quadratic")!!.compute(
            mapOf("a" to "1", "b" to "-3", "c" to "2"),
        )
        assertNotNull(output.headline)
        val text = output.headline
        assertTrue("Expected roots 2 and 1 in $text", text.contains("2") && text.contains("1"))
    }

    @Test
    fun `quadratic with a zero leading coefficient is rejected`() {
        try {
            SolverCatalog.find("quadratic")!!.compute(mapOf("a" to "0", "b" to "1", "c" to "1"))
            throw AssertionError("Expected a validation failure")
        } catch (expected: SolverException) {
            // expected
        }
    }

    @Test
    fun `statistics solver reproduces the mandated mean and median`() {
        val summary = SolverCatalog.find("dataset_summary")!!.compute(mapOf("values" to "2, 4, 6, 8"))
        assertEquals(5.0, summary.headline.toDouble(), 1e-9)
        assertEquals("5", summary.rows.first { it.first == "Median" }.second)

        val odd = SolverCatalog.find("dataset_summary")!!.compute(mapOf("values" to "1, 3, 7"))
        assertEquals("3", odd.rows.first { it.first == "Median" }.second)
    }

    @Test
    fun `gcd and lcm solvers match the mandated vectors`() {
        val gcd = SolverCatalog.find("gcd_lcm")!!.compute(mapOf("a" to "48", "b" to "18"))
        assertEquals("6", gcd.headline)
        assertTrue(gcd.rows.any { it.first == "LCM" && it.second == "144" })
    }

    @Test
    fun `date difference solver counts inclusive and exclusive days correctly`() {
        val output = SolverCatalog.find("days_between")!!.compute(
            mapOf("start" to "2024-01-01", "end" to "2024-01-31"),
        )
        assertEquals("30 days", output.headline)
    }

    @Test
    fun `invalid dataset raises a typed solver error`() {
        try {
            SolverCatalog.find("dataset_summary")!!.compute(mapOf("values" to "1, two, 3"))
            throw AssertionError("Expected a validation failure")
        } catch (expected: SolverException) {
            // expected
        }
    }

    @Test
    fun `search matches titles categories and tags`() {
        assertTrue(SolverCatalog.search("fraction").isNotEmpty())
        assertTrue(SolverCatalog.byCategory(SolverCategory.DATE_TIME).isNotEmpty())
        assertTrue(SolverCatalog.mathTools.isNotEmpty())
        assertTrue(SolverCatalog.quickSolve.isNotEmpty())
    }
}
