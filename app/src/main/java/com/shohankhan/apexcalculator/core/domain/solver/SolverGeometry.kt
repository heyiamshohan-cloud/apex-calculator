package com.shohankhan.apexcalculator.core.domain.solver

import kotlin.math.PI
import kotlin.math.hypot
import kotlin.math.sqrt

internal fun geometrySolvers(): List<SolverSpec> = listOf(
    SolverSpec(
        id = "pythagoras",
        title = "Pythagoras theorem",
        description = "Finds the hypotenuse of a right triangle.",
        category = SolverCategory.GEOMETRY,
        fields = listOf(SolverField("a", "Leg a", "3"), SolverField("b", "Leg b", "4")),
    ) { input ->
        val a = requirePositiveValue(input.number("a", "Leg a"), "Leg a")
        val b = requirePositiveValue(input.number("b", "Leg b"), "Leg b")
        val c = hypot(a, b)
        SolverOutput(
            headline = fmt(c),
            headlineLabel = "Hypotenuse",
            rows = listOf(
                "Area" to fmt(a * b / 2),
                "Perimeter" to fmt(a + b + c),
                "Angle opposite a" to "${fmt(Math.toDegrees(kotlin.math.atan2(a, b)))}°",
            ),
            steps = listOf("c = √(a² + b²)"),
        )
    },
    SolverSpec(
        id = "circle_quick",
        title = "Circle area and circumference",
        description = "Area, circumference and diameter for a radius.",
        category = SolverCategory.GEOMETRY,
        fields = listOf(SolverField("r", "Radius", "2")),
    ) { input ->
        val r = requirePositiveValue(input.number("r", "Radius"), "Radius")
        SolverOutput(
            headline = fmt(PI * r * r),
            headlineLabel = "Area",
            rows = listOf(
                "Circumference" to fmt(2 * PI * r),
                "Diameter" to fmt(2 * r),
                "Surface of sphere" to fmt(4 * PI * r * r),
            ),
            steps = listOf("A = πr²", "C = 2πr"),
        )
    },
    SolverSpec(
        id = "rectangle_quick",
        title = "Rectangle",
        description = "Area, perimeter and diagonal of a rectangle.",
        category = SolverCategory.GEOMETRY,
        fields = listOf(SolverField("w", "Width", "5"), SolverField("h", "Height", "3")),
    ) { input ->
        val w = requirePositiveValue(input.number("w", "Width"), "Width")
        val h = requirePositiveValue(input.number("h", "Height"), "Height")
        SolverOutput(
            headline = fmt(w * h),
            headlineLabel = "Area",
            rows = listOf(
                "Perimeter" to fmt(2 * (w + h)),
                "Diagonal" to fmt(hypot(w, h)),
            ),
        )
    },
    SolverSpec(
        id = "triangle_quick",
        title = "Triangle area",
        description = "Area from a base and height, or three sides.",
        category = SolverCategory.GEOMETRY,
        fields = listOf(
            SolverField("b", "Base", "8"),
            SolverField("h", "Height", "5"),
            SolverField("c", "Third side (optional)", "6"),
        ),
    ) { input ->
        val base = requirePositiveValue(input.number("b", "Base"), "Base")
        val height = requirePositiveValue(input.number("h", "Height"), "Height")
        val third = input["c"]?.trim()?.toDoubleOrNull()
        val area = base * height / 2
        SolverOutput(
            headline = fmt(area),
            headlineLabel = "Area",
            rows = buildList {
                add("Base" to fmt(base))
                add("Height" to fmt(height))
                if (third != null && third > 0) {
                    add("Perimeter" to fmt(base + height + third))
                }
            },
            steps = listOf("A = ½ × base × height"),
        )
    },
    SolverSpec(
        id = "sphere_volume",
        title = "Sphere volume",
        description = "Volume and surface area of a sphere.",
        category = SolverCategory.GEOMETRY,
        fields = listOf(SolverField("r", "Radius", "3")),
    ) { input ->
        val r = requirePositiveValue(input.number("r", "Radius"), "Radius")
        SolverOutput(
            headline = fmt(4.0 / 3.0 * PI * r * r * r),
            headlineLabel = "Volume",
            rows = listOf("Surface area" to fmt(4 * PI * r * r), "Diameter" to fmt(2 * r)),
        )
    },
    SolverSpec(
        id = "equilateral_quick",
        title = "Equilateral triangle",
        description = "Area, height and perimeter of an equilateral triangle.",
        category = SolverCategory.GEOMETRY,
        fields = listOf(SolverField("a", "Side", "6")),
    ) { input ->
        val side = requirePositiveValue(input.number("a", "Side"), "Side")
        SolverOutput(
            headline = fmt(sqrt(3.0) / 4 * side * side),
            headlineLabel = "Area",
            rows = listOf(
                "Height" to fmt(sqrt(3.0) / 2 * side),
                "Perimeter" to fmt(3 * side),
            ),
        )
    },
)
