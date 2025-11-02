package com.evening.dailylife.feature.mortgage

import androidx.lifecycle.ViewModel
import com.evening.dailylife.R
import com.evening.dailylife.feature.mortgage.model.MortgageAmortizationType
import com.evening.dailylife.feature.mortgage.model.MortgageCalculationResult
import com.evening.dailylife.feature.mortgage.model.MortgageCalculatorUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.pow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class MortgageCalculatorViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(MortgageCalculatorUiState())
    val uiState: StateFlow<MortgageCalculatorUiState> = _uiState

    fun onPrincipalChange(value: String) {
        _uiState.update { current ->
            current.copy(
                principalInput = value,
                principalError = null,
                calculation = null,
                isCalculateEnabled = determineCalculateEnabled(
                    principalInput = value,
                    annualRateInput = current.annualRateInput,
                    termYearsInput = current.termYearsInput,
                ),
            )
        }
    }

    fun onAnnualRateChange(value: String) {
        _uiState.update { current ->
            current.copy(
                annualRateInput = value,
                annualRateError = null,
                calculation = null,
                isCalculateEnabled = determineCalculateEnabled(
                    principalInput = current.principalInput,
                    annualRateInput = value,
                    termYearsInput = current.termYearsInput,
                ),
            )
        }
    }

    fun onTermYearsChange(value: String) {
        _uiState.update { current ->
            current.copy(
                termYearsInput = value,
                termYearsError = null,
                calculation = null,
                isCalculateEnabled = determineCalculateEnabled(
                    principalInput = current.principalInput,
                    annualRateInput = current.annualRateInput,
                    termYearsInput = value,
                ),
            )
        }
    }

    fun onAmortizationTypeChange(type: MortgageAmortizationType) {
        _uiState.update { current ->
            current.copy(amortizationType = type)
        }
        if (_uiState.value.calculation != null) {
            performCalculation()
        }
    }

    fun onCalculateClick() {
        performCalculation()
    }

    private fun performCalculation() {
        val current = _uiState.value
        val principal = current.principalInput.toDoubleOrNull()
        val annualRate = current.annualRateInput.toDoubleOrNull()
        val termYears = current.termYearsInput.toIntOrNull()

        val principalError = when {
            principal == null || principal <= 0.0 -> R.string.mortgage_calculator_error_principal
            else -> null
        }
        val annualRateError = when {
            annualRate == null || annualRate < 0.0 || annualRate > 100.0 -> R.string.mortgage_calculator_error_annual_rate
            else -> null
        }
        val termYearsError = when {
            termYears == null || termYears <= 0 -> R.string.mortgage_calculator_error_term
            else -> null
        }

        if (principalError != null || annualRateError != null || termYearsError != null) {
            _uiState.update {
                it.copy(
                    principalError = principalError,
                    annualRateError = annualRateError,
                    termYearsError = termYearsError,
                    calculation = null,
                )
            }
            return
        }

        val result = computeMortgage(
            principal = principal!!,
            annualRate = annualRate!!,
            termYears = termYears!!,
            type = current.amortizationType,
        )

        _uiState.update {
            it.copy(
                principalError = null,
                annualRateError = null,
                termYearsError = null,
                calculation = result,
            )
        }
    }

    private fun computeMortgage(
        principal: Double,
        annualRate: Double,
        termYears: Int,
        type: MortgageAmortizationType,
    ): MortgageCalculationResult {
        val totalMonths = termYears * 12
        val monthlyRate = annualRate / 1200.0
        val isZeroRate = abs(monthlyRate) < 1e-8

        return when (type) {
            MortgageAmortizationType.EqualPrincipalInterest -> {
                val monthlyPaymentValue = if (isZeroRate) {
                    principal / totalMonths
                } else {
                    val factor = (1 + monthlyRate).pow(totalMonths)
                    principal * monthlyRate * factor / (factor - 1)
                }
                val totalPaymentValue = monthlyPaymentValue * totalMonths
                val totalInterestValue = totalPaymentValue - principal

                MortgageCalculationResult(
                    amortizationType = type,
                    loanYears = termYears,
                    loanMonths = totalMonths,
                    typicalMonthlyPayment = monthlyPaymentValue.toCurrency(),
                    totalInterest = totalInterestValue.toCurrency(),
                    totalPayment = totalPaymentValue.toCurrency(),
                )
            }

            MortgageAmortizationType.EqualPrincipal -> {
                val monthlyPrincipal = principal / totalMonths
                val totalInterestValue = if (isZeroRate) {
                    0.0
                } else {
                    monthlyRate * principal * (totalMonths + 1) / 2.0
                }
                val totalPaymentValue = principal + totalInterestValue
                val firstMonthPaymentValue = if (isZeroRate) {
                    monthlyPrincipal
                } else {
                    monthlyPrincipal + principal * monthlyRate
                }
                val lastMonthPaymentValue = if (isZeroRate) {
                    monthlyPrincipal
                } else {
                    monthlyPrincipal + monthlyPrincipal * monthlyRate
                }
                val monthlyDecreaseValue = if (isZeroRate) {
                    0.0
                } else {
                    monthlyPrincipal * monthlyRate
                }
                val averageMonthlyPaymentValue = totalPaymentValue / totalMonths

                MortgageCalculationResult(
                    amortizationType = type,
                    loanYears = termYears,
                    loanMonths = totalMonths,
                    typicalMonthlyPayment = averageMonthlyPaymentValue.toCurrency(),
                    totalInterest = totalInterestValue.toCurrency(),
                    totalPayment = totalPaymentValue.toCurrency(),
                    firstMonthPayment = firstMonthPaymentValue.toCurrency(),
                    lastMonthPayment = lastMonthPaymentValue.toCurrency(),
                    monthlyPaymentDecrease = monthlyDecreaseValue.toCurrency(),
                )
            }
        }
    }

    private fun determineCalculateEnabled(
        principalInput: String,
        annualRateInput: String,
        termYearsInput: String,
    ): Boolean {
        return principalInput.isNotBlank() &&
            annualRateInput.isNotBlank() &&
            termYearsInput.isNotBlank()
    }

    private fun Double.toCurrency(): BigDecimal {
        return BigDecimal.valueOf(this).setScale(2, RoundingMode.HALF_UP)
    }
}
