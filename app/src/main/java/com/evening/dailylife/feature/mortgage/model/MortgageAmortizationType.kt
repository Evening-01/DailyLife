package com.evening.dailylife.feature.mortgage.model

import androidx.annotation.StringRes
import com.evening.dailylife.R

enum class MortgageAmortizationType(
    @StringRes val labelResId: Int,
) {
    EqualPrincipalInterest(R.string.mortgage_calculator_type_equal_payment),
    EqualPrincipal(R.string.mortgage_calculator_type_equal_principal),
}
