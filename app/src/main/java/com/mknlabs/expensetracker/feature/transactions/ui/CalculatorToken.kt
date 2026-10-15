package com.mknlabs.expensetracker.feature.transactions.ui

/**
 * Atomic token unit for the MIUI-style expression engine.
 *
 * Each button press inserts / removes exactly ONE [CalculatorToken] object,
 * giving the cursor true token-boundary semantics (no mid-number navigation
 * issues).
 *
 * Display symbols vs. eval symbols are kept separate so the UI always renders
 * user-friendly glyphs (×, ÷, −) while the evaluator works with ASCII math.
 */
sealed class CalculatorToken {

    /** A numeric literal, possibly containing a decimal point. */
    data class Number(val value: String) : CalculatorToken()

    /**
     * An arithmetic operator.
     *
     * @param symbol      Human-readable glyph shown on screen (×, ÷, −, +).
     * @param evalSymbol  ASCII character used by the evaluator (*, /, -, +).
     */
    data class Operator(val symbol: String, val evalSymbol: String) : CalculatorToken()

    /** An opening parenthesis "(" */
    data object ParenthesisOpen : CalculatorToken()

    /** A closing parenthesis ")" */
    data object ParenthesisClose : CalculatorToken()

    // ── Rendering helpers ─────────────────────────────────────────────────

    /**
     * The string displayed to the user for this token.
     *
     * Rendered tight — operators and brackets carry no padding spaces, so an
     * expression reads `1+2×3` rather than `1 + 2 × 3`. This length is also what
     * the display↔token cursor mapping walks, so changing it moves every caret
     * position with it.
     */
    val displaySymbol: String
        get() = when (this) {
            is Number           -> value
            is Operator         -> symbol
            is ParenthesisOpen  -> "("
            is ParenthesisClose -> ")"
        }
}

// ── Extension helpers ─────────────────────────────────────────────────────

/**
 * Renders the full token list as a human-readable expression string.
 * Nothing is padded: digits, operators and brackets all join directly, so the
 * expression box shows `1+2×3`.
 */
fun List<CalculatorToken>.toDisplayString(): String =
    joinToString(separator = "") { it.displaySymbol }

/**
 * Renders the token list into an ASCII string suitable for the shunting-yard
 * evaluator. Automatically appends the missing closing parentheses for any
 * unclosed opening ones so the evaluator always receives a balanced expression.
 */
fun List<CalculatorToken>.toEvalString(): String {
    val openCount  = count { it is CalculatorToken.ParenthesisOpen }
    val closeCount = count { it is CalculatorToken.ParenthesisClose }
    val missing    = (openCount - closeCount).coerceAtLeast(0)
    return buildString {
        for (token in this@toEvalString) {
            when (token) {
                is CalculatorToken.Number           -> append(token.value)
                is CalculatorToken.Operator         -> append(token.evalSymbol)
                is CalculatorToken.ParenthesisOpen  -> append("(")
                is CalculatorToken.ParenthesisClose -> append(")")
            }
        }
        repeat(missing) { append(")") }
    }
}

/** Net count of unclosed parentheses across the entire token list. */
val List<CalculatorToken>.unclosedParenCount: Int
    get() {
        val open  = count { it is CalculatorToken.ParenthesisOpen }
        val close = count { it is CalculatorToken.ParenthesisClose }
        return (open - close).coerceAtLeast(0)
    }
