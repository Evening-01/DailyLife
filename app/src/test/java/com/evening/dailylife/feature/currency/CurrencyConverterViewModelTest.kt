package com.evening.dailylife.feature.currency

import com.evening.dailylife.R
import com.evening.dailylife.feature.currency.model.CurrencyOption
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class CurrencyConverterViewModelTest {

    @Test
    fun calculateConversion() {
        val viewModel = CurrencyConverterViewModel()

        viewModel.onBaseCurrencyChange(CurrencyOption.USD)
        viewModel.onTargetCurrencyChange(CurrencyOption.CNY)
        viewModel.onAmountChange("100")
        viewModel.onRateChange("7.3")

        viewModel.onConvertClick()

        val result = viewModel.uiState.value.conversionResult
        assertNotNull(result)
        result!!
        assertEquals(CurrencyOption.USD, result.baseCurrency)
        assertEquals(CurrencyOption.CNY, result.targetCurrency)
        assertEquals(BigDecimal("100.00"), result.baseAmount)
        assertEquals(BigDecimal("730.00"), result.targetAmount)
        assertEquals(BigDecimal("7.300000"), result.rate)
        assertEquals(BigDecimal("0.13698630"), result.inverseRate)
    }

    @Test
    fun swapCurrenciesInvertsRateAndRecomputes() {
        val viewModel = CurrencyConverterViewModel()

        viewModel.onBaseCurrencyChange(CurrencyOption.USD)
        viewModel.onTargetCurrencyChange(CurrencyOption.CNY)
        viewModel.onAmountChange("100")
        viewModel.onRateChange("7.3")
        viewModel.onConvertClick()

        viewModel.onSwapCurrencies()

        val uiState = viewModel.uiState.value
        assertEquals(CurrencyOption.CNY, uiState.baseCurrency)
        assertEquals(CurrencyOption.USD, uiState.targetCurrency)
        assertEquals("730", uiState.amountInput)
        assertEquals("0.13698630137", uiState.rateInput)

        val result = uiState.conversionResult
        assertNotNull(result)
        result!!
        assertEquals(BigDecimal("730.00"), result.baseAmount)
        assertEquals(BigDecimal("100.00"), result.targetAmount)
    }

    @Test
    fun invalidInputsShowErrors() {
        val viewModel = CurrencyConverterViewModel()

        viewModel.onAmountChange("0")
        viewModel.onRateChange("-1")

        viewModel.onConvertClick()

        val uiState = viewModel.uiState.value
        assertEquals(R.string.currency_converter_error_amount, uiState.amountError)
        assertEquals(R.string.currency_converter_error_rate, uiState.rateError)
        assertNull(uiState.conversionResult)
    }
}
