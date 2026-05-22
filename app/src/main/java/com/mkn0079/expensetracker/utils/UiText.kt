package com.mkn0079.expensetracker.utils

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

/**
 * A wrapper for UI text that can be either a hardcoded string or a string resource.
 * Useful for ViewModels that need to expose localized text without direct context access.
 */
sealed class UiText {
    data class DynamicString(val value: String) : UiText()
    data class StringResource(
        @StringRes val resId: Int,
        val args: List<Any> = emptyList()
    ) : UiText()

    @Composable
    fun asString(): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> {
                val resolvedArgs = args.map { arg ->
                    if (arg is UiText) arg.asString() else arg
                }
                stringResource(resId, *resolvedArgs.toTypedArray())
            }
        }
    }

    companion object {
        fun dynamic(value: String): UiText = DynamicString(value)
        fun res(@StringRes resId: Int, vararg args: Any): UiText = 
            StringResource(resId, args.toList())
    }
}
