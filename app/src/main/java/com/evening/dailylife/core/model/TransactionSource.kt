package com.evening.dailylife.core.model

/**
 * Centralizes the default source metadata so it stays locale-agnostic.
 */
object TransactionSource {
    const val DEFAULT = "dailylife"

    private val legacyValues = setOf("dailylife", "DailyLife", "日子记账")

    fun isAppSource(value: String): Boolean {
        return legacyValues.any { it.equals(value, ignoreCase = true) }
    }
}
