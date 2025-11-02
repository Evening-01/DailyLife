package com.evening.dailylife.feature.currency

import androidx.lifecycle.ViewModel
import com.evening.dailylife.R
import com.evening.dailylife.feature.currency.model.CurrencyConversionResult
import com.evening.dailylife.feature.currency.model.CurrencyConverterUiState
import com.evening.dailylife.feature.currency.model.CurrencyOption
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class CurrencyConverterViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(CurrencyConverterUiState())
    val uiState: StateFlow<CurrencyConverterUiState> = _uiState

    fun onBaseCurrencyChange(option: CurrencyOption) {
        val hadResult = _uiState.value.conversionResult != null
        _uiState.update { current ->
            current.copy(
                baseCurrency = option,
                conversionResult = null,
            )
        }
        if (hadResult) {
            performConversion()
        }
    }

    fun onTargetCurrencyChange(option: CurrencyOption) {
        val hadResult = _uiState.value.conversionResult != null
        _uiState.update { current ->
            current.copy(
                targetCurrency = option,
                conversionResult = null,
            )
        }
        if (hadResult) {
            performConversion()
        }
    }

    fun onSwapCurrencies() {
        val current = _uiState.value
        val hadResult = current.conversionResult != null
        val invertedRate = current.rateInput.toDoubleOrNull()?.takeIf { it > 0.0 }?.let { rate ->
            BigDecimal.ONE.divide(BigDecimal.valueOf(rate), MathContext(12, RoundingMode.HALF_UP))
        }
        val updatedAmountInput = if (hadResult) {
            current.conversionResult!!.targetAmount.trimmedString()
        } else {
            current.amountInput
        }
        val updatedRateInput = invertedRate?.trimmedString() ?: current.rateInput
        val newState = current.copy(
            baseCurrency = current.targetCurrency,
            targetCurrency = current.baseCurrency,
            amountInput = updatedAmountInput,
            rateInput = updatedRateInput,
            amountError = null,
            rateError = null,
            conversionResult = null,
            isConvertEnabled = determineConvertEnabled(
                amountInput = updatedAmountInput,
                rateInput = updatedRateInput,
            ),
        )
        _uiState.value = newState
        if (hadResult) {
            performConversion()
        }
    }

    fun onAmountChange(value: String) {
        _uiState.update { current ->
            current.copy(
                amountInput = value,
                amountError = null,
                conversionResult = null,
                isConvertEnabled = determineConvertEnabled(
                    amountInput = value,
                    rateInput = current.rateInput,
                ),
            )
        }
    }

    fun onRateChange(value: String) {
        _uiState.update { current ->
            current.copy(
                rateInput = value,
                rateError = null,
                conversionResult = null,
                isConvertEnabled = determineConvertEnabled(
                    amountInput = current.amountInput,
                    rateInput = value,
                ),
            )
        }
    }

    fun onConvertClick() {
        performConversion()
    }

    private fun performConversion() {
        val current = _uiState.value
        val amount = current.amountInput.toDoubleOrNull()
        val rate = current.rateInput.toDoubleOrNull()

        val amountError = when {
            amount == null || amount <= 0.0 -> R.string.currency_converter_error_amount
            else -> null
        }
        val rateError = when {
            rate == null || rate <= 0.0 -> R.string.currency_converter_error_rate
            else -> null
        }

        if (amountError != null || rateError != null) {
            _uiState.update {
                it.copy(
                    amountError = amountError,
                    rateError = rateError,
                    conversionResult = null,
                )
            }
            return
        }

        val baseAmount = BigDecimal.valueOf(amount!!)
        val rateValue = BigDecimal.valueOf(rate!!)
        val targetAmount = baseAmount.multiply(rateValue).setScale(2, RoundingMode.HALF_UP)
        val inverseRate = BigDecimal.ONE.divide(rateValue, 8, RoundingMode.HALF_UP)

        val result = CurrencyConversionResult(
            baseCurrency = current.baseCurrency,
            targetCurrency = current.targetCurrency,
            baseAmount = baseAmount.setScale(2, RoundingMode.HALF_UP),
            targetAmount = targetAmount,
            rate = rateValue.setScale(6, RoundingMode.HALF_UP),
            inverseRate = inverseRate,
        )

        _uiState.update {
            it.copy(
                amountError = null,
                rateError = null,
                conversionResult = result,
            )
        }
    }

    private fun determineConvertEnabled(
        amountInput: String,
        rateInput: String,
    ): Boolean {
        return amountInput.isNotBlank() && rateInput.isNotBlank()
    }

    private fun BigDecimal.trimmedString(): String {
        return this.stripTrailingZeros().toPlainString()
    }
}
