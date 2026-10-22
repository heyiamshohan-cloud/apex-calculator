# APEX Calculator

A premium, fully offline Android calculator built with Kotlin and Jetpack Compose. APEX combines a
standard calculator, a scientific workspace with calculus, a formula library, step-by-step solvers,
a programmer workspace and a set of mathematical tools in one Material 3 interface.

- **Offline by construction** — no INTERNET permission, no backend, no accounts, no ads, no analytics.
- **Exact where it matters** — rational fractions, arbitrary-precision integers and complex numbers
  instead of floating point everywhere.
- **Explainable results** — solvers show the formula, the substitution and the arithmetic that
  produced the answer.
- **Local data only** — history, favourites and settings live in Room and DataStore on the device.

| | |
|---|---|
| Application ID | `com.shohankhan.apexcalculator` |
| Version | 1.0.0 (versionCode 1) |
| Minimum SDK | 26 (Android 8.0) |
| Target / compile SDK | 35 |
| Language | English only |
| Developer | Shohan Khan — helloiamshohan@gmail.com |

---

## Features

### Standard
Expression entry with visible precedence, parentheses, percent, sign toggle and backspace.
Memory register (MC, MR, M+, M−, MS), undo/redo, live preview of the pending result, copy, and
automatic history. Landscape uses a two-pane layout.

### Scientific
Trigonometric, inverse and hyperbolic functions, natural and arbitrary-base logarithms,
exponential, roots, factorial, nCr/nPr, GCD/LCM, constants, DEG/RAD switching, SHIFT for inverse
functions, complex mode (`i`), and a calculus dialog that evaluates:
numerical derivatives, definite integrals and root finding on any expression in `x`.

### Formula Lab
A structured catalogue of 120 formulas with stable IDs, categories, descriptions, assumptions and
validated inputs. Categories: percentages, finance (EMI, compound interest, and more), time–speed–
work, geometry and mensuration, algebra and sequences, physics and statistics. Search across names,
expressions and tags; mark favourites.

### Quick Solve
Exact fraction arithmetic, ratios and proportions, number theory (GCD/LCM, primes, factorisation,
divisors), linear and quadratic equations, 2×2 and 3×3 systems with pivoting, polynomial roots,
sequences and series, powers, roots and logarithms, statistics and probability. Every solver returns
a headline result, supporting values and the steps used to obtain them.

### Programmer
Binary, octal, decimal and hexadecimal views of the same value, two's complement interpretation for
8/16/32/64-bit signed and unsigned registers, bitwise AND/OR/XOR/NOT, logical and arithmetic shifts,
add/subtract/multiply/divide/modulo with wrapping, and a tappable bit inspector.

### Math Tools
Matrix calculator (determinant, inverse, transpose, RREF, rank and linear systems up to 8×8),
complex-number workspace (arithmetic, modulus, argument, conjugate, polar form), statistics
workspace, probability tools, sequences and summations, offline date and time arithmetic, and
numerical calculus.

### History, Favorites, Settings, About
History is grouped by day, searchable, and supports single or multi-selection deletion plus delete
all with confirmation. Retention is configurable (Never, 1, 7, 30, 90 or 365 days; default 90) and
enforced on launch and by a daily WorkManager job. Favorites are never affected by cleanup. Settings
cover theme, accent, haptics, reduced motion, angle mode, number format, decimal places, significant
figures, thousands separators, exact-fraction display, scientific-notation threshold, retention and
memory persistence.

---

## Percentage semantics

The rule is identical on every screen and is covered by tests:

| Expression | Meaning | Result |
|---|---|---|
| `200 + 10%` | 10% **of the value on the left** | 220 |
| `200 − 10%` | 10% of the value on the left | 180 |
| `200 × 15%` | 15% as a fraction of 200 | 30 |
| `15% of 200` | `of` is multiplication | 30 |
| `50%` | standalone fraction | 0.5 |

---

## Build

Requirements: JDK 17 and an Android SDK with platform 35 and build-tools 35.0.0.

```bash
git clone https://github.com/heyiamshohan-cloud/apex-calculator.git
cd apex-calculator
./gradlew :app:assembleDebug        # debug APK
./gradlew :app:testDebugUnitTest    # unit tests
./gradlew :app:lintDebug            # Android lint
./gradlew :app:assembleRelease      # release APK (signed when secrets are present)
```

Outputs:

- Debug: `app/build/outputs/apk/debug/app-debug.apk` (applicationId suffix `.debug`)
- Release: `app/build/outputs/apk/release/app-release.apk`

Release signing is driven by `APEX_KEYSTORE_FILE`, `APEX_KEYSTORE_PASSWORD`, `APEX_KEY_ALIAS` and
`APEX_KEY_PASSWORD`. Without them the release variant is signed with the debug identity so the
artifact remains installable — see [docs/RELEASE.md](docs/RELEASE.md) for the production procedure.

## Testing

Unit tests run on the JVM with JUnit 4 and cover the engine, fractions, number theory, statistics,
probability, matrices, programmer arithmetic, date math, numerical calculus, both catalogues and the
mandated acceptance vectors. See [docs/TESTING.md](docs/TESTING.md).

## Continuous integration

`.github/workflows/android-ci.yml` compiles the app, runs unit tests, lint and a release build, then
uploads the test reports and APKs. `.github/workflows/release.yml` builds and publishes a signed
release when the four signing secrets are configured.

## Privacy

The app declares no permissions at all. History, favourites and settings are stored locally and can
be deleted from Settings or the Privacy screen. See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## Documentation

- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) — module layout, state flow and data model
- [docs/MATH_ENGINE.md](docs/MATH_ENGINE.md) — grammar, precedence, numeric tower and semantics
- [docs/TESTING.md](docs/TESTING.md) — test strategy, coverage of mandated vectors, how to run
- [docs/RELEASE.md](docs/RELEASE.md) — signing, versioning and publishing
- [docs/ACCEPTANCE_CHECKLIST.md](docs/ACCEPTANCE_CHECKLIST.md) — requirement-by-requirement status

## Licence and contact

Copyright © 2026 Shohan Khan. All rights reserved.
Contact: helloiamshohan@gmail.com
