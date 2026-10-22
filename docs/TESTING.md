# Testing

## How to run

```bash
./gradlew :app:testDebugUnitTest      # JVM unit tests
./gradlew :app:lintDebug              # Android lint
./gradlew :app:assembleRelease        # release build
```

HTML report: `app/build/reports/tests/testDebugUnitTest/index.html`
JUnit XML: `app/build/test-results/testDebugUnitTest/*.xml`

CI runs the unit tests on every push and pull request. Test reports and the debug APK are uploaded as
artifacts; the build log is published to `ci-logs/ci-build.log` so failures can be inspected without
the Actions log API.

## Test suites (108 JUnit tests)

| File | Tests | Area |
|---|---|---|
| `core/math/ExpressionEngineTest.kt` | 19 | lexer/parser/evaluator, precedence, functions, formatting, typed errors |
| `core/math/FractionsAndStatisticsTest.kt` | 32 | exact fractions, mixed numbers, number theory, descriptive statistics, probability |
| `core/math/ProgrammerAndMatrixTest.kt` | 19 | radix parsing/formatting, two's complement, bitwise ops, matrices |
| `core/math/NumericalAndDateTest.kt` | 16 | derivatives, integrals, roots, tables, calendar arithmetic |
| `core/domain/CatalogTest.kt` | 22 | formula catalogue integrity and solver vectors |

## Mandated acceptance vectors

| # | Vector | Expected | Covered by |
|---|---|---|---|
| 1 | `2 + 3 × 4` | 14 | `ExpressionEngineTest.precedence…` |
| 2 | `-5 + 8` | 3 | `ExpressionEngineTest.…unary…` |
| 3 | `2^10` | 1024 | `ExpressionEngineTest.powers…` |
| 4 | `sin(30°)` | 0.5 | `ExpressionEngineTest.trigonometry…` |
| 5 | `5!` | 120 | `ExpressionEngineTest.factorials…` |
| 6 | `C(5,2)` | 10 | `ExpressionEngineTest.combinatorics…` |
| 7 | `GCD(48,18)` | 6 | `NumberTheoryTest`, `SolverCatalogTest` |
| 8 | `LCM(12,18)` | 36 | `NumberTheoryTest` |
| 9 | `1/3 + 1/6` | ½ exact | `FractionsTest`, `SolverCatalogTest` |
| 10 | `15% of 200` | 30 | `ExpressionEngineTest`, `FormulaCatalogTest` |
| 11 | `200 + 10%` | 220 | `ExpressionEngineTest` |
| 12 | `200 − 10%` | 180 | `ExpressionEngineTest` |
| 13 | SI 1000 at 5% for 2 years | 100 | `FormulaCatalogTest` |
| 14 | Square with side 5 | 25 | `FormulaCatalogTest` |
| 15 | Circle with r = 2 | 4π | `FormulaCatalogTest` |
| 16 | `0b1010` | 10 | `ProgrammerTest` |
| 17 | `0xFF` | 255 | `ProgrammerTest` |
| 18 | `12 AND 10` | 8 | `ProgrammerTest` |
| 19 | `det[[1,2],[3,4]]` | −2 | `MatrixTest` |
| 20 | mean of `[2,4,6,8]` | 5 | `StatisticsTest`, `SolverCatalogTest` |
| 21 | median of `[1,3,7]` | 3 | `StatisticsTest`, `SolverCatalogTest` |

## Broader coverage

- **Precedence and associativity** — right-associative powers, unary minus versus exponent, implicit
  multiplication, postfix factorial/percent, parentheses and absolute value.
- **Numeric tower** — exact integers beyond 64 bits (`2^64`), exact decimals (`0.1 + 0.2`), exact
  fractions, complex arithmetic (`√−4 = 2i`), overflow and domain errors.
- **Functions** — trigonometry in both angle units, logarithms, roots, combinatorics, number theory
  helpers, rounding family, constants, random functions with fixed seeds.
- **Fractions** — parsing of `3/4`, mixed `1 1/2`, negatives, decimals; addition, subtraction,
  multiplication, division; terminating-decimal detection; mixed-number display; malformed input.
- **Number theory** — GCD/LCM edge cases, primality boundaries, canonical factorisation, divisor
  enumeration, perfect squares and cubes, divisibility rules.
- **Statistics** — mean/median/quartiles/IQR, population versus sample variance, modes, percentiles
  (R-7), rejection of empty or non-finite data.
- **Probability** — factorial, permutations, combinations, binomial distribution, event algebra,
  conditional probability, invalid probability rejection.
- **Programmer** — radix prefixes, grouped formatting, two's complement boundaries, wrapping
  arithmetic, division/modulo by zero, shift validation, reversible bit toggling, invalid input.
- **Matrices** — determinant, inverse round trip, systems, rank, transpose, dimension mismatch,
  singular matrices, RREF of dependent rows.
- **Numerical methods** — derivative accuracy, integral orientation, root finding and its failure
  mode, table generation, degree-mode consistency.
- **Dates** — ISO parsing, rejection of other formats, day/week/month components, half-open weekday
  counting, clamping when adding months, durations across midnight, leap years.
- **Catalogues** — unique IDs, non-empty metadata, every category populated, representative
  computations, typed validation errors, search.

## What is not covered

- Instrumented (Espresso/Compose UI) tests are not part of this repository; UI behaviour is verified
  by compiling every screen and by manual installation. See `docs/ACCEPTANCE_CHECKLIST.md`.
- Rendering screenshots are not regenerated in CI.
- The sandbox used to build this repository has no Android device, so on-device installation was
  not performed here.
