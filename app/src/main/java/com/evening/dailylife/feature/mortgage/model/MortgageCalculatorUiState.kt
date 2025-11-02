package com.evening.dailylife.feature.mortgage.model

import androidx.annotation.StringRes

data class MortgageCalculatorUiState(
    val principalInput: String = "",
    val annualRateInput: String = "",
    val termYearsInput: String = "",
    val amortizationType: MortgageAmortizationType = MortgageAmortizationType.EqualPrincipalInterest,
    @StringRes val principalError: Int? = null,
    @StringRes val annualRateError: Int? = null,
    @StringRes val termYearsError: Int? = null,
    val calculation: MortgageCalculationResult? = null,
    val isCalculateEnabled: Boolean = false,
)
