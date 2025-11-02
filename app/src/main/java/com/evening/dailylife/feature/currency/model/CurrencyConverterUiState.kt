package com.evening.dailylife.feature.currency.model

import androidx.annotation.StringRes

data class CurrencyConverterUiState(
    val baseCurrency: CurrencyOption = CurrencyOption.CNY,
    val targetCurrency: CurrencyOption = CurrencyOption.USD,
    val amountInput: String = "",
    val rateInput: String = "",
    @StringRes val amountError: Int? = null,
    @StringRes val rateError: Int? = null,
    val conversionResult: CurrencyConversionResult? = null,
    val isConvertEnabled: Boolean = false,
)
