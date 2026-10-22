package com.shohankhan.apexcalculator.core.math

import kotlin.math.abs

/** Immutable row-major matrix of double precision values. */
data class Matrix(val rows: Int, val cols: Int, val values: List<Double>) {

    init {
        require(rows > 0 && cols > 0) { "A matrix needs at least one row and column" }
        require(values.size == rows * cols) { "Matrix data does not match its dimensions" }
    }

    val isSquare: Boolean get() = rows == cols

    operator fun get(row: Int, col: Int): Double = values[row * cols + col]

    fun toRows(): List<List<Double>> = (0 until rows).map { row -> (0 until cols).map { col -> get(row, col) } }

    fun withValue(row: Int, col: Int, value: Double): Matrix {
        val copy = values.toMutableList()
        copy[row * cols + col] = value
        return Matrix(rows, cols, copy)
    }

    companion object {
        fun of(data: List<List<Double>>): Matrix {
            require(data.isNotEmpty()) { "A matrix needs at least one row" }
            val cols = data.first().size
            require(cols > 0) { "A matrix needs at least one column" }
            require(data.all { it.size == cols }) { "All rows must have the same length" }
            return Matrix(data.size, cols, data.flatten())
        }

        fun zero(rows: Int, cols: Int): Matrix = Matrix(rows, cols, List(rows * cols) { 0.0 })

        fun identity(size: Int): Matrix =
            Matrix(size, size, List(size * size) { index -> if (index / size == index % size) 1.0 else 0.0 })
    }
}

sealed interface MatrixResult {
    data class MatrixValue(val matrix: Matrix) : MatrixResult

    data class Scalar(val value: Double) : MatrixResult

    data class Failure(val message: String) : MatrixResult
}

/**
 * Matrix arithmetic, elimination and linear-system solving.
 *
 * Numerical tolerance [EPSILON] treats values below 1e-10 as zero during elimination.
 * Singular matrices are reported, never silently inverted.
 */
object MatrixMath {

    const val EPSILON = 1e-10
    const val MAX_DIMENSION = 8

    fun add(a: Matrix, b: Matrix): MatrixResult {
        if (a.rows != b.rows || a.cols != b.cols) {
            return MatrixResult.Failure("Addition needs matrices of the same size (${a.rows}×${a.cols} vs ${b.rows}×${b.cols})")
        }
        return MatrixResult.MatrixValue(
            Matrix(a.rows, a.cols, List(a.values.size) { a.values[it] + b.values[it] }),
        )
    }

    fun subtract(a: Matrix, b: Matrix): MatrixResult {
        if (a.rows != b.rows || a.cols != b.cols) {
            return MatrixResult.Failure("Subtraction needs matrices of the same size (${a.rows}×${a.cols} vs ${b.rows}×${b.cols})")
        }
        return MatrixResult.MatrixValue(
            Matrix(a.rows, a.cols, List(a.values.size) { a.values[it] - b.values[it] }),
        )
    }

    fun multiply(a: Matrix, b: Matrix): MatrixResult {
        if (a.cols != b.rows) {
            return MatrixResult.Failure(
                "Multiplication needs the first matrix columns (${a.cols}) to match the second matrix rows (${b.rows})",
            )
        }
        val result = DoubleArray(a.rows * b.cols)
        for (row in 0 until a.rows) {
            for (col in 0 until b.cols) {
                var total = 0.0
                for (k in 0 until a.cols) {
                    total += a[row, k] * b[k, col]
                }
                result[row * b.cols + col] = total
            }
        }
        return MatrixResult.MatrixValue(Matrix(a.rows, b.cols, result.toList()))
    }

    fun scalarMultiply(a: Matrix, scalar: Double): MatrixResult =
        MatrixResult.MatrixValue(Matrix(a.rows, a.cols, a.values.map { it * scalar }))

    fun transpose(a: Matrix): MatrixResult =
        MatrixResult.MatrixValue(Matrix(a.cols, a.rows, List(a.rows * a.cols) { index ->
            val row = index % a.rows
            val col = index / a.rows
            a[row, col]
        }))

    fun trace(a: Matrix): MatrixResult {
        if (!a.isSquare) return MatrixResult.Failure("The trace is defined for square matrices")
        return MatrixResult.Scalar((0 until a.rows).sumOf { a[it, it] })
    }

    fun determinant(a: Matrix): MatrixResult {
        if (!a.isSquare) return MatrixResult.Failure("The determinant is defined for square matrices")
        val n = a.rows
        val data = a.values.toMutableList()
        var determinant = 1.0
        var sign = 1.0
        for (col in 0 until n) {
            var pivot = col
            for (row in col + 1 until n) {
                if (abs(data[row * n + col]) > abs(data[pivot * n + col])) pivot = row
            }
            if (abs(data[pivot * n + col]) < EPSILON) return MatrixResult.Scalar(0.0)
            if (pivot != col) {
                for (k in 0 until n) {
                    val temporary = data[col * n + k]
                    data[col * n + k] = data[pivot * n + k]
                    data[pivot * n + k] = temporary
                }
                sign = -sign
            }
            determinant *= data[col * n + col]
            for (row in col + 1 until n) {
                val factor = data[row * n + col] / data[col * n + col]
                for (k in col until n) {
                    data[row * n + k] -= factor * data[col * n + k]
                }
            }
        }
        return MatrixResult.Scalar(determinant * sign)
    }

    fun inverse(a: Matrix): MatrixResult {
        if (!a.isSquare) return MatrixResult.Failure("Only square matrices can be inverted")
        val n = a.rows
        val augmented = Array(n) { row ->
            DoubleArray(2 * n) { col ->
                when {
                    col < n -> a[row, col]
                    col - n == row -> 1.0
                    else -> 0.0
                }
            }
        }
        for (col in 0 until n) {
            var pivot = col
            for (row in col + 1 until n) {
                if (abs(augmented[row][col]) > abs(augmented[pivot][col])) pivot = row
            }
            if (abs(augmented[pivot][col]) < EPSILON) {
                return MatrixResult.Failure("The matrix is singular and has no inverse")
            }
            if (pivot != col) {
                val temporary = augmented[col]
                augmented[col] = augmented[pivot]
                augmented[pivot] = temporary
            }
            val pivotValue = augmented[col][col]
            for (k in 0 until 2 * n) augmented[col][k] /= pivotValue
            for (row in 0 until n) {
                if (row == col) continue
                val factor = augmented[row][col]
                if (abs(factor) < EPSILON) continue
                for (k in 0 until 2 * n) augmented[row][k] -= factor * augmented[col][k]
            }
        }
        val inverseValues = ArrayList<Double>(n * n)
        for (row in 0 until n) {
            for (col in 0 until n) {
                inverseValues += augmented[row][n + col]
            }
        }
        return MatrixResult.MatrixValue(Matrix(n, n, inverseValues))
    }

    fun rref(a: Matrix): Matrix {
        val data = a.values.toMutableList()
        val n = a.rows
        val m = a.cols
        var pivotRow = 0
        for (col in 0 until m) {
            if (pivotRow >= n) break
            var pivot = pivotRow
            for (row in pivotRow + 1 until n) {
                if (abs(data[row * m + col]) > abs(data[pivot * m + col])) pivot = row
            }
            if (abs(data[pivot * m + col]) < EPSILON) continue
            if (pivot != pivotRow) {
                for (k in 0 until m) {
                    val temporary = data[pivotRow * m + k]
                    data[pivotRow * m + k] = data[pivot * m + k]
                    data[pivot * m + k] = temporary
                }
            }
            val pivotValue = data[pivotRow * m + col]
            for (k in 0 until m) data[pivotRow * m + k] /= pivotValue
            for (row in 0 until n) {
                if (row == pivotRow) continue
                val factor = data[row * m + col]
                if (abs(factor) < EPSILON) continue
                for (k in 0 until m) data[row * m + k] -= factor * data[pivotRow * m + k]
            }
            pivotRow++
        }
        return Matrix(data.size / m, m, data.map { if (abs(it) < EPSILON) 0.0 else it })
    }

    fun rank(a: Matrix): Int {
        val reduced = rref(a)
        var rank = 0
        for (row in 0 until reduced.rows) {
            if ((0 until reduced.cols).any { abs(reduced[row, it]) > EPSILON }) rank++
        }
        return rank
    }

    fun rowOperationSwap(a: Matrix, first: Int, second: Int): MatrixResult {
        if (first !in 0 until a.rows || second !in 0 until a.rows) {
            return MatrixResult.Failure("Row index is outside the matrix")
        }
        val data = a.values.toMutableList()
        for (col in 0 until a.cols) {
            val temporary = data[first * a.cols + col]
            data[first * a.cols + col] = data[second * a.cols + col]
            data[second * a.cols + col] = temporary
        }
        return MatrixResult.MatrixValue(Matrix(a.rows, a.cols, data))
    }

    fun rowOperationScale(a: Matrix, row: Int, factor: Double): MatrixResult {
        if (row !in 0 until a.rows) return MatrixResult.Failure("Row index is outside the matrix")
        if (factor == 0.0) return MatrixResult.Failure("Scaling by zero would destroy the row")
        val data = a.values.toMutableList()
        for (col in 0 until a.cols) data[row * a.cols + col] *= factor
        return MatrixResult.MatrixValue(Matrix(a.rows, a.cols, data))
    }

    fun rowOperationAddMultiple(a: Matrix, target: Int, source: Int, factor: Double): MatrixResult {
        if (target !in 0 until a.rows || source !in 0 until a.rows) {
            return MatrixResult.Failure("Row index is outside the matrix")
        }
        if (target == source) return MatrixResult.Failure("Choose two different rows")
        val data = a.values.toMutableList()
        for (col in 0 until a.cols) data[target * a.cols + col] += factor * data[source * a.cols + col]
        return MatrixResult.MatrixValue(Matrix(a.rows, a.cols, data))
    }

    /** Solves A·x = b. Reports singular, inconsistent and underdetermined systems explicitly. */
    fun solve(a: Matrix, b: Matrix): MatrixResult {
        if (!a.isSquare) return MatrixResult.Failure("The coefficient matrix must be square")
        if (b.rows != a.rows || b.cols != 1) {
            return MatrixResult.Failure("The right-hand side must be a column vector with ${a.rows} rows")
        }
        val n = a.rows
        val augmented = Matrix(n, n + 1, buildList {
            for (row in 0 until n) {
                for (col in 0 until n) add(a[row, col])
                add(b[row, 0])
            }
        })
        val reduced = rref(augmented)
        for (row in 0 until n) {
            val allZero = (0 until n).all { abs(reduced[row, it]) < EPSILON }
            if (allZero && abs(reduced[row, n]) > EPSILON) {
                return MatrixResult.Failure("The system is inconsistent and has no solution")
            }
        }
        if (rank(a) < n) {
            return MatrixResult.Failure("The system does not have a unique solution (the matrix is singular)")
        }
        return MatrixResult.MatrixValue(Matrix(n, 1, List(n) { reduced[it, n] }))
    }
}
