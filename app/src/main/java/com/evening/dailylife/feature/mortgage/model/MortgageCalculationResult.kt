package com.evening.dailylife.feature.mortgage.model

import java.math.BigDecimal

data class MortgageCalculationResult(
    val amortizationType: MortgageAmortizationType,
    val loanYears: Int,
    val loanMonths: Int,
    val typicalMonthlyPayment: BigDecimal,
    val totalInterest: BigDecimal,
    val totalPayment: BigDecimal,
    val firstMonthPayment: BigDecimal? = null,
    val lastMonthPayment: BigDecimal? = null,
    val monthlyPaymentDecrease: BigDecimal? = null,
)
