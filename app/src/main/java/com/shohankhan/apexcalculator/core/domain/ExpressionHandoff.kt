package com.shohankhan.apexcalculator.core.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * One-shot channel that lets history and favourites reopen an expression in the Standard tab
 * without coupling the screens to each other.
 */
object ExpressionHandoff {

    private val _pending = MutableStateFlow<String?>(null)
    val pending: StateFlow<String?> = _pending

    fun offer(expression: String) {
        _pending.value = expression
    }

    fun clear() {
        _pending.value = null
    }
}
