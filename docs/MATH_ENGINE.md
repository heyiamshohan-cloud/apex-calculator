# The APEX math engine

The engine is a hand-written lexer, recursive-descent parser and tree-walking evaluator. There is no
JavaScript bridge, no `eval`, no WebView and no remote service. It is a pure Kotlin component in
`core/math` and is used by every screen.

```
input text ──► Lexer ──► tokens ──► ExpressionParser ──► Expr tree ──► Evaluator ──► Num ──► ResultFormatter
```

## 1. Lexing

`Lexer` normalises user input before parsing:

| Input | Token |
|---|---|
| `×`, `·`, `*` | `STAR` |
| `÷`, `/` | `SLASH` |
| `−`, `–`, `-` | `MINUS` |
| `√` | identifier `sqrt` |
| `π` | identifier `pi` |
| `²`, `³` | `^2`, `^3` |
| word `of` | `STAR` |

Numbers may use a leading or trailing decimal point and exponent notation (`1.5e-3`). Integers are
scanned into `BigInteger` when they fit, so `2^64` is exact. Any character that cannot start a token
raises `MathError.Syntax` with a 1-based position.

## 2. Grammar and precedence

```
expression     := additive
additive       := multiplicative (("+" | "-") multiplicative)*
multiplicative := unary (("*" | "/") unary | implicit unary)*
unary          := ("+" | "-") unary | power
power          := postfix ("^" unary)?          // right associative
postfix        := primary ("!" | "%")*
primary        := NUMBER | IDENT | IDENT "(" args ")" | "(" expression ")" | "|" expression "|"
```

Consequences that are covered by tests:

- `2 + 3 × 4 = 14` — multiplication binds tighter than addition.
- `2^3^2 = 512` — exponentiation is right associative.
- `-2^2 = -4` — unary minus binds looser than exponentiation. Write `(-2)^2` for 4.
- `2(3+4) = 14`, `(1+2)(3) = 9`, `2π` — implicit multiplication between adjacent primaries.
- `5! = 120` and postfix `%` bind tighter than any binary operator.
- Nesting is limited to 96 levels and the tree budget to 4096 nodes; exceeding them reports
  `MathError.Limit` instead of exhausting the stack.

## 3. Percentage semantics

The postfix `%` node is resolved by the evaluator, not by textual replacement:

| Form | Evaluation | Example |
|---|---|---|
| `a + p%` | `a + a·p/100` | `200 + 10% = 220` |
| `a − p%` | `a − a·p/100` | `200 − 10% = 180` |
| `p%` elsewhere | `p/100` | `200 × 15% = 30`, `15% of 200 = 30` |

This is the documented convention across the app and is asserted in `ExpressionEngineTest`.

## 4. Numeric tower

`Num` is a sealed interface with five representations, promoted as needed:

| Type | Representation | Used for |
|---|---|---|
| `IntVal` | `BigInteger` | exact integers of any size |
| `Rat` | exact `Rational` (BigInteger pair) | fractions such as `1/3` |
| `Dec` | `BigDecimal` | exact decimal literals |
| `Real` | `Double` | transcendental results |
| `Cpx` | `ComplexNumber` | complex mode |

Promotion order is `IntVal → Rat → Dec → Real → Cpx`. Arithmetic keeps the lowest exact type
possible: `1/3 + 1/6` stays `1/2`, `0.1 + 0.2` stays exactly `0.3`, and only operations that are
inherently irrational (trigonometry, logarithms) fall back to `Real`.

Guardrails: factorials are limited to 1000! (cached), exact powers to 2,000,000 result bits, and
non-finite doubles are reported as `MathError.Overflow`.

## 5. Complex numbers

`i` resolves to the imaginary unit only when complex mode is enabled; otherwise it is
`MathError.Domain`. `sqrt(-4)` returns `2i` in complex mode and a domain error otherwise. Powers use
an integer fast path and the principal branch `exp(b·ln a)` for general exponents.

## 6. Functions

Registered in `MathFunctions`, each with arity checking:

- Trigonometric: `sin`, `cos`, `tan`, `asin`, `acos`, `atan`, `atan2`
- Hyperbolic: `sinh`, `cosh`, `tanh`, `asinh`, `acosh`, `atanh`
- Exponential/logarithmic: `ln`, `log(x [, base])`, `log10`, `log2`, `exp`
- Powers/roots: `sqrt`, `cbrt`, `root(x, n)`, `hypot`
- Rounding: `abs`, `sign`, `floor`, `ceil`, `round`, `trunc`, `frac`
- Combinatorics: `factorial`, `ncr`, `npr`
- Number theory: `gcd`, `lcm`, `mod`, `rem`
- Utilities: `min`, `max`, `deg`, `rad`, `rand`, `randint`
- Complex: `re`, `im`, `arg`, `conj`

Angles follow the DEG/RAD setting for trigonometric functions and their inverses; `arg` reports in
the same unit so complex results stay consistent.

## 7. Errors

`MathError` is a sealed hierarchy: `Syntax`, `Incomplete`, `DivisionByZero`, `Domain`, `Overflow`,
`Undefined`, `Limit`, `UnknownIdentifier`, `Arity`, `Unsupported`. The UI never shows a magic value
such as `NaN`; it shows the message of the typed error, and the Standard screen treats an
incomplete expression as "keep typing" rather than an error.

## 8. Formatting

`ResultFormatter` renders values according to `FormatSettings`: automatic, scientific, engineering,
fixed or significant-figure mode; decimal places; thousands separators; optional exact-fraction
display; and a scientific-notation threshold. Formatting never feeds back into the value — the
engine keeps full precision and only the display is rounded. The secondary line can expose the exact
fraction behind a decimal (`0.3333333333` → `exact: 1/3`).

## 9. Numerical methods

`Numerical` provides the calculus used by the Scientific sheet and Math Tools:

- Derivative: five-point central difference with `h = 1e-5·max(1, |x|)`.
- Integral: adaptive Simpson quadrature, tolerance `1e-9`, depth 24.
- Root finding: bisection on a sign-changing bracket, tolerance `1e-12`, at most 200 iterations.
- Table generation: at most 500 rows.

Numerical results are labelled as approximations wherever they appear.
