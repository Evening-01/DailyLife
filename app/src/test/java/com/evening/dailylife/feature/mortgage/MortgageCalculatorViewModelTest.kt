package com.evening.dailylife.feature.mortgage

import com.evening.dailylife.R
import com.evening.dailylife.feature.mortgage.model.MortgageAmortizationType
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class MortgageCalculatorViewModelTest {

    @Test
    fun calculateEqualPrincipalInterestLoan() {
        val viewModel = MortgageCalculatorViewModel()

        viewModel.onPrincipalChange("500000")
        viewModel.onAnnualRateChange("4.1")
        viewModel.onTermYearsChange("30")
        viewModel.onAmortizationTypeChange(MortgageAmortizationType.EqualPrincipalInterest)

        viewModel.onCalculateClick()

        val result = viewModel.uiState.value.calculation
        assertNotNull(result)
        result!!
        assertEquals(BigDecimal("2415.99"), result.typicalMonthlyPayment)
        assertEquals(BigDecimal("369757.07"), result.totalInterest)
        assertEquals(BigDecimal("869757.07"), result.totalPayment)
        assertNull(result.firstMonthPayment)
        assertNull(result.lastMonthPayment)
        assertNull(result.monthlyPaymentDecrease)
    }

    @Test
    fun calculateEqualPrincipalLoan() {
        val viewModel = MortgageCalculatorViewModel()

        viewModel.onPrincipalChange("500000")
        viewModel.onAnnualRateChange("4.1")
        viewModel.onTermYearsChange("30")
        viewModel.onAmortizationTypeChange(MortgageAmortizationType.EqualPrincipal)

        viewModel.onCalculateClick()

        val result = viewModel.uiState.value.calculation
        assertNotNull(result)
        result!!
        assertEquals(BigDecimal("2245.43"), result.typicalMonthlyPayment)
        assertEquals(BigDecimal("308354.17"), result.totalInterest)
        assertEquals(BigDecimal("808354.17"), result.totalPayment)
        assertEquals(BigDecimal("3097.22"), result.firstMonthPayment)
        assertEquals(BigDecimal("1393.63"), result.lastMonthPayment)
        assertEquals(BigDecimal("4.75"), result.monthlyPaymentDecrease)
    }

    @Test
    fun invalidInputsShowErrors() {
        val viewModel = MortgageCalculatorViewModel()

        viewModel.onPrincipalChange("-10")
        viewModel.onAnnualRateChange("120")
        viewModel.onTermYearsChange("0")

        viewModel.onCalculateClick()

        val uiState = viewModel.uiState.value
        assertEquals(R.string.mortgage_calculator_error_principal, uiState.principalError)
        assertEquals(R.string.mortgage_calculator_error_annual_rate, uiState.annualRateError)
        assertEquals(R.string.mortgage_calculator_error_term, uiState.termYearsError)
        assertNull(uiState.calculation)
    }
}
