package com.evening.dailylife.feature.currency.model

import androidx.annotation.StringRes
import com.evening.dailylife.R

enum class CurrencyOption(
    val code: String,
    @StringRes val labelResId: Int,
) {
    CNY(code = "CNY", labelResId = R.string.currency_option_cny),
    USD(code = "USD", labelResId = R.string.currency_option_usd),
    EUR(code = "EUR", labelResId = R.string.currency_option_eur),
    JPY(code = "JPY", labelResId = R.string.currency_option_jpy),
    GBP(code = "GBP", labelResId = R.string.currency_option_gbp),
    HKD(code = "HKD", labelResId = R.string.currency_option_hkd),
    AUD(code = "AUD", labelResId = R.string.currency_option_aud),
    CAD(code = "CAD", labelResId = R.string.currency_option_cad),
}
