# Architecture

APEX Calculator is a single-module Android application written in Kotlin with Jetpack Compose and
Material 3. It follows MVVM with unidirectional data flow: immutable UI state flows down, user
events flow up as method calls, and only the ViewModel mutates state.

```
┌──────────────────────────────────────────────────────────────┐
│ MainActivity (edge-to-edge, theme from settings)             │
│   └── ApexApp  NavHost ── six tabs + secondary screens       │
├──────────────────────────────────────────────────────────────┤
│ feature/*      Compose screens + ViewModels                  │
├──────────────────────────────────────────────────────────────┤
│ core/domain    models, SettingsMapping, formula + solver      │
│                catalogues (pure Kotlin, no Android types)     │
│ core/math      lexer, parser, evaluator, numeric types,       │
│                fractions, number theory, statistics,          │
│                probability, matrices, programmer, dates,      │
│                numerical calculus, formatting                 │
├──────────────────────────────────────────────────────────────┤
│ core/data      Room database, DataStore settings, repositories│
│                WorkManager cleanup                            │
└──────────────────────────────────────────────────────────────┘
```

## Source layout

| Path | Responsibility |
|---|---|
| `core/math` | Pure calculation engine. No Android dependencies, fully JVM-testable. |
| `core/domain/model` | `AppSettings`, `ApexTab`, `HistoryRecord`, `FavoriteItem`, `MemoryState`. |
| `core/domain/formula` | Formula metadata, DSL and the seven catalogue files (120 formulas). |
| `core/domain/solver` | Solver metadata, input parsing helpers and the solver catalogues. |
| `core/data` | `ApexDatabase` (Room), DAOs, repositories, `SettingsRepository` (DataStore), `HistoryCleanupWorker`. |
| `core/designsystem` | Colours, typography, shapes, icons, reusable components. |
| `core/ui` | Cross-feature helpers: catalogue rows, session logging, settings state. |
| `feature/*` | One package per tab or secondary screen. |
| `navigation` | Route table, `NavHost`, tab chrome and secondary scaffold. |

## Dependency injection

`AppContainer` is a hand-written container created once by `ApexApplication`. It owns the database,
the three repositories and the application coroutine scope. ViewModels extend `AndroidViewModel` and
read the container from the application, so no DI framework is required and startup stays cheap.
The container also purges expired history on launch and schedules the daily cleanup worker.

## State and events

Every screen with mutable UI state uses a ViewModel exposing a single `StateFlow<UiState>`:

- `StandardViewModel` — expression `TextFieldValue`, committed value, live preview, preview error,
  memory register, undo/redo stacks, last answer, previous record.
- `ScientificViewModel` — the same core plus SHIFT, complex mode and calculus dialog state.
- `SettingsViewModel` — settings plus destructive actions and transient status messages.
- `HistoryViewModel` — search query, records, selection and the active retention policy.
- `CatalogViewModel` — favourites shared by the formula catalogue, solvers and tools.
- `SessionViewModel` — writes solver/formula results into history.

Events are plain methods (`onDigit`, `onOperator`, `onEquals`, `insert`). The ViewModel never holds
a `Context` and never touches Compose; screens are stateless relative to business logic.

## Data model

```
HistoryRecord(id, expression, result, source, createdAt, details)
FavoriteItem(kind, refId, title, subtitle, savedAt)   // unique key = "KIND:refId"
AppSettings(theme, accent, angleUnit, formatting, retention, haptics, …)
```

Room tables: `history` (indexed on `createdAt`) and `favorites` (unique index on `kind` + `refId`).
DataStore stores settings as primitives with enum names, so unknown values fall back to defaults
instead of crashing after an upgrade.

## Navigation

Six tab routes (`standard`, `scientific`, `formula`, `quick`, `programmer`, `tools`) plus secondary
routes (`history`, `favorites`, `settings`, `about`, `privacy`, `tools/matrix`, `tools/complex`,
`formula/{id}`, `solver/{id}`, `tool/{id}`). Tab switches use `popUpTo(start)
{ saveState = true }`, `launchSingleTop` and `restoreState`, so each tab keeps its own scroll and
input state. A history row hands its expression back to Standard through a one-shot
`ExpressionHandoff` flow rather than reaching into another ViewModel.

## Responsive layout

`TabScaffold` measures the window: below 720 dp it renders a Material 3 bottom navigation bar; at or
above 720 dp it switches to a navigation rail. Standard, Scientific and Programmer additionally
switch to a two-pane layout in short landscape windows. All screens use `Scaffold` insets so
content is never drawn under the status or navigation bars, and text uses `sp` units so system font
scaling is respected.

## Offline and privacy guarantees

- No `INTERNET` permission is declared in the manifest, and CI verifies the merged manifest does not
  introduce one from a dependency.
- No analytics, crash reporting, advertising or telemetry SDK is present.
- No account, login, cloud sync or paid API is used.
- Clipboard access happens only in direct response to a copy action.
- History cleanup runs locally on launch and daily through WorkManager; it never touches favourites.

## Error handling

The math layer throws `MathEvalException(MathError)`; the UI layer maps it to a message and an inline
banner. Catalogues throw `FormulaException` / `SolverException` with a message intended for the user.
Unhandled arithmetic overflow never produces `NaN` on screen: it becomes a typed `Overflow` error.

## Build

AGP 8.6.1, Kotlin 2.0.21, Compose BOM 2024.10.01, JDK 17, `minSdk` 26, `targetSdk/compileSdk` 35.
Dependencies: AndroidX Core/Lifecycle/Activity, Compose UI + Material 3, Navigation Compose, Room
(KSP), DataStore Preferences, WorkManager, kotlinx-coroutines. Tests: JUnit 4. Only permissive
(Apache-2.0 / EPL-1.0 test) licences are involved.
