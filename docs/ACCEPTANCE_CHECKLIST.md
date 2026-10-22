# Acceptance checklist

Status legend: **PASS** verified in this repository or CI, **BLOCKED** cannot be verified in this
environment (reason given), **FAIL** verified not to meet the requirement.

| # | Requirement | Status | Evidence / reason |
|---|---|---|---|
| 1 | App label “APEX Calculator” | PASS | `res/values/strings.xml`, launcher activity label |
| 2 | applicationId `com.shohankhan.apexcalculator` | PASS | `app/build.gradle.kts`; asserted by CI release verification |
| 3 | Developer details in About | PASS | Name, email, version and build code from `PackageManager` |
| 4 | Kotlin + Jetpack Compose + Material 3 | PASS | Compose BOM 2024.10.01, custom `ApexTheme` |
| 5 | Navigation Compose, six tabs | PASS | `navigation/ApexApp.kt`, `TabScaffold` |
| 6 | MVVM with unidirectional data flow | PASS | `StateFlow<UiState>` + event methods in ViewModels |
| 7 | Room history storage | PASS | `ApexDatabase`, `HistoryDao`, repositories |
| 8 | DataStore settings | PASS | `SettingsRepository` |
| 9 | Gradle Kotlin DSL + committed wrapper | PASS | `*.gradle.kts`, `gradle/wrapper` |
| 10 | JUnit tests | PASS | 108 tests across five suites |
| 11 | GitHub Actions CI | PASS | `.github/workflows/android-ci.yml` |
| 12 | minSdk 26, targetSdk 35, JDK 17 | PASS | `app/build.gradle.kts` |
| 13 | Portrait and landscape | PASS | Responsive `BoxWithConstraints` layouts, split panes |
| 14 | Font and display scaling | PASS | `sp` typography, no fixed-height text clipping |
| 15 | Edge-to-edge with insets | PASS | `enableEdgeToEdge()` + `Scaffold` insets |
| 16 | Light and dark themes | PASS | `apexLightColors` / `apexDarkColors`, system/light/dark |
| 17 | Standard calculator: precedence, parentheses, percent, memory, backspace, copy, history | PASS | `StandardScreen`, `StandardViewModel`; percentage convention tested |
| 18 | Scientific: trig/inverse/hyperbolic, ln/log/arbitrary base, powers, roots, factorial, nCr/nPr, GCD/LCM, constants, DEG/RAD, SHIFT | PASS | Function strip, SHIFT toggle, engine functions |
| 19 | Scientific: complex numbers | PASS | `i` unit in complex mode; complex workspace |
| 20 | Scientific: numerical derivative, integral, root finding | PASS | Calculus dialog with adaptive methods |
| 21 | Formula Lab: structured catalogue with IDs, categories, validation | PASS | 120 formulas in 7 categories |
| 22 | Formula Lab: percentages, financial/EMI/compound interest, time–speed–work, geometry, algebra/sequences, physics, statistics | PASS | `FormulaCatalogArithmetic`, `…Geometry`, `…Science` |
| 23 | Formula Lab: search and favourites | PASS | Query filtering + `FavoritesRepository` |
| 24 | Quick Solve: exact fractions, ratios, number theory | PASS | 22 solvers in three catalogues |
| 25 | Quick Solve: algebra and linear systems with pivoting | PASS | Partial-pivot Gauss-Jordan for 2×2 and 3×3 |
| 26 | Quick Solve: genuine step-by-step output | PASS | Every solver returns `steps` |
| 27 | Quick Solve: inline validation | PASS | `SolverException` rendered above the result |
| 28 | Programmer: bin/oct/dec/hex | PASS | `NumberRadix`, radix panel |
| 29 | Programmer: bitwise, shifts, 8/16/32/64 signed/unsigned, BigInteger | PASS | `ProgrammerMath`, `ProgrammerValue` |
| 30 | Programmer: bit inspector | PASS | Tappable bit grid |
| 31 | Math Tools: statistics workspace | PASS | `dataset_summary` and related solvers |
| 32 | Math Tools: probability | PASS | Binomial, nCr, nPr, event algebra |
| 33 | Math Tools: matrix calculator | PASS | 8×8 workspace with det/inverse/RREF/rank/solve |
| 34 | Math Tools: complex workspace | PASS | Arithmetic + polar form |
| 35 | Math Tools: sequences and summations | PASS | Arithmetic/geometric/Fibonacci solvers |
| 36 | Math Tools: date arithmetic | PASS | `DateMath` + five date solvers |
| 37 | Math Tools: numerical tools | PASS | Derivative, integral, root, table |
| 38 | History: search and date grouping | PASS | `HistoryViewModel`, `HistoryGroups` |
| 39 | History: copy, reuse, recalculate | PASS | Per-row copy; reuse re-opens the expression in Standard |
| 40 | History: single/multi/delete-all with confirmation | PASS | Selection mode + `ConfirmDialog` |
| 41 | History: retention Never/1/7/30/90/365, default 90 | PASS | `RetentionPolicy`, `HistoryCleanupWorker` |
| 42 | History cleanup on launch and WorkManager | PASS | `AppContainer.purgeExpiredHistory`, daily worker |
| 43 | Cleanup never touches favourites | PASS | Separate tables and repository |
| 44 | Favorites screen | PASS | Formulas and tools with removal |
| 45 | Settings: theme, accent, haptics, reduced motion, angle, precision, notation, retention, reset | PASS | `SettingsScreen` |
| 46 | About screen without fabricated channels | PASS | Only name, email, version, build and package |
| 47 | Privacy and data management screen | PASS | Data locations, permissions, deletion actions |
| 48 | Own lexer/parser/AST, no dynamic eval | PASS | `core/math` engine |
| 49 | Explicit precedence, unary minus vs exponent | PASS | Grammar documented and tested |
| 50 | Exact rational + BigInteger/BigDecimal + complex | PASS | Numeric tower with promotion |
| 51 | Typed errors | PASS | `MathError` hierarchy |
| 52 | DEG/RAD consistency | PASS | Shared `AngleUnit` across engine, solvers, formatting |
| 53 | Formatting layer separate from values | PASS | `ResultFormatter`/`FormatSettings` |
| 54 | Mandated test vectors | PASS | See `docs/TESTING.md` mapping table |
| 55 | CI: compile, unit tests, lint, release build, artifact checks, uploads | PASS | `android-ci.yml`, build + quality jobs |
| 56 | Release workflow with signed APK name and GitHub Release | PASS | `release.yml`; requires repository secrets to sign |
| 57 | Signed production APK produced | BLOCKED | No production keystore or signing secrets are available in this environment; the release build falls back to the debug identity and is not published as production |
| 58 | APK installed on a physical device | BLOCKED | No Android device or emulator is reachable from the build sandbox |
| 59 | README and docs set | PASS | `README.md`, `docs/ARCHITECTURE.md`, `MATH_ENGINE.md`, `TESTING.md`, `RELEASE.md` |
| 60 | No secrets committed | PASS | Signing values are environment/secret driven |

## Known limitations

1. **Signing.** Without `APEX_KEYSTORE_*` secrets the release variant is signed with the debug key.
   This is deliberate so the artifact remains installable, and is called out in the delivery report.
2. **On-device verification.** No emulator or physical device is available here; the APK was compiled
   and its package/version/signature can be inspected, but installation was not performed.
3. **Instrumented tests.** UI tests are not included; the UI is verified by compilation and manual
   review of every screen.
4. **Lint.** Lint runs with `abortOnError = true`; warnings are reported but do not fail the build.
