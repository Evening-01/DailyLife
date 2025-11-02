package com.evening.dailylife.feature.currency

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.evening.dailylife.R
import com.evening.dailylife.feature.currency.model.CurrencyConversionResult
import com.evening.dailylife.feature.currency.model.CurrencyConverterUiState
import com.evening.dailylife.feature.currency.model.CurrencyOption
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.TitleBar
import com.moriafly.salt.ui.UnstableSaltApi
import com.moriafly.salt.ui.ext.safeMainPadding
import java.text.NumberFormat
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, UnstableSaltApi::class)
@Composable
fun CurrencyConverterScreen(
    onBackClick: () -> Unit,
    viewModel: CurrencyConverterViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val amountFormatter = rememberAmountFormatter()
    val rateFormatter = rememberRateFormatter()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SaltTheme.colors.background)
            .safeMainPadding(),
    ) {
        TitleBar(
            onBack = onBackClick,
            text = stringResource(id = R.string.discover_common_tool_fx_title),
        )

        CurrencyConverterContent(
            uiState = uiState,
            amountFormatter = amountFormatter,
            rateFormatter = rateFormatter,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            onBaseCurrencyChange = viewModel::onBaseCurrencyChange,
            onTargetCurrencyChange = viewModel::onTargetCurrencyChange,
            onSwapCurrencies = viewModel::onSwapCurrencies,
            onAmountChange = { value ->
                viewModel.onAmountChange(sanitizeDecimalInput(value))
            },
            onRateChange = { value ->
                viewModel.onRateChange(sanitizeDecimalInput(value))
            },
            onConvertClick = {
                focusManager.clearFocus()
                viewModel.onConvertClick()
            },
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun CurrencyConverterContent(
    uiState: CurrencyConverterUiState,
    amountFormatter: NumberFormat,
    rateFormatter: NumberFormat,
    modifier: Modifier = Modifier,
    onBaseCurrencyChange: (CurrencyOption) -> Unit,
    onTargetCurrencyChange: (CurrencyOption) -> Unit,
    onSwapCurrencies: () -> Unit,
    onAmountChange: (String) -> Unit,
    onRateChange: (String) -> Unit,
    onConvertClick: () -> Unit,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        InputCard(
            uiState = uiState,
            onBaseCurrencyChange = onBaseCurrencyChange,
            onTargetCurrencyChange = onTargetCurrencyChange,
            onSwapCurrencies = onSwapCurrencies,
            onAmountChange = onAmountChange,
            onRateChange = onRateChange,
            onConvertClick = onConvertClick,
        )
        uiState.conversionResult?.let { result ->
            ResultCard(
                result = result,
                amountFormatter = amountFormatter,
                rateFormatter = rateFormatter,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InputCard(
    uiState: CurrencyConverterUiState,
    onBaseCurrencyChange: (CurrencyOption) -> Unit,
    onTargetCurrencyChange: (CurrencyOption) -> Unit,
    onSwapCurrencies: () -> Unit,
    onAmountChange: (String) -> Unit,
    onRateChange: (String) -> Unit,
    onConvertClick: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(
                text = stringResource(id = R.string.currency_converter_inputs_title),
                style = MaterialTheme.typography.titleMedium,
            )
            CurrencySelectionSection(
                baseCurrency = uiState.baseCurrency,
                targetCurrency = uiState.targetCurrency,
                onBaseCurrencyChange = onBaseCurrencyChange,
                onTargetCurrencyChange = onTargetCurrencyChange,
                onSwapCurrencies = onSwapCurrencies,
            )
            OutlinedTextField(
                value = uiState.amountInput,
                onValueChange = onAmountChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = stringResource(id = R.string.currency_converter_amount_label)) },
                placeholder = { Text(text = "0.00") },
                prefix = { Text(text = uiState.baseCurrency.code) },
                isError = uiState.amountError != null,
                supportingText = uiState.amountError?.let { errorRes ->
                    {
                        Text(
                            text = stringResource(id = errorRes),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next,
                ),
            )
            OutlinedTextField(
                value = uiState.rateInput,
                onValueChange = onRateChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = stringResource(id = R.string.currency_converter_rate_label)) },
                placeholder = {
                    Text(
                        text = stringResource(
                            id = R.string.currency_converter_rate_placeholder,
                            uiState.baseCurrency.code,
                            uiState.targetCurrency.code,
                        ),
                    )
                },
                supportingText = {
                    if (uiState.rateError != null) {
                        Text(
                            text = stringResource(id = uiState.rateError),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    } else {
                        Text(
                            text = stringResource(id = R.string.currency_converter_rate_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                suffix = { Text(text = uiState.targetCurrency.code) },
                isError = uiState.rateError != null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        if (uiState.isConvertEnabled) {
                            onConvertClick()
                        }
                    },
                ),
            )
            Button(
                onClick = onConvertClick,
                enabled = uiState.isConvertEnabled,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(id = R.string.currency_converter_convert_action))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencySelectionSection(
    baseCurrency: CurrencyOption,
    targetCurrency: CurrencyOption,
    onBaseCurrencyChange: (CurrencyOption) -> Unit,
    onTargetCurrencyChange: (CurrencyOption) -> Unit,
    onSwapCurrencies: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CurrencyDropdown(
            label = stringResource(id = R.string.currency_converter_base_currency_label),
            selected = baseCurrency,
            onSelected = onBaseCurrencyChange,
        )
        TextButton(
            onClick = onSwapCurrencies,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Filled.SwapVert,
                    contentDescription = null,
                )
                Text(
                    text = stringResource(id = R.string.currency_converter_swap_action),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
        CurrencyDropdown(
            label = stringResource(id = R.string.currency_converter_target_currency_label),
            selected = targetCurrency,
            onSelected = onTargetCurrencyChange,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyDropdown(
    label: String,
    selected: CurrencyOption,
    onSelected: (CurrencyOption) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            value = stringResource(id = selected.labelResId),
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            label = { Text(text = label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            singleLine = true,
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            CurrencyOption.values().forEach { option ->
                androidx.compose.material3.DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(id = option.labelResId),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    onClick = {
                        expanded = false
                        onSelected(option)
                    },
                )
            }
        }
    }
}

@Composable
private fun ResultCard(
    result: CurrencyConversionResult,
    amountFormatter: NumberFormat,
    rateFormatter: NumberFormat,
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(id = R.string.currency_converter_result_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(
                    id = R.string.currency_converter_result_amount,
                    amountFormatter.format(result.baseAmount),
                    result.baseCurrency.code,
                    amountFormatter.format(result.targetAmount),
                    result.targetCurrency.code,
                ),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Divider()
            Text(
                text = stringResource(
                    id = R.string.currency_converter_result_rate,
                    result.baseCurrency.code,
                    rateFormatter.format(result.rate),
                    result.targetCurrency.code,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(
                    id = R.string.currency_converter_result_inverse_rate,
                    result.targetCurrency.code,
                    rateFormatter.format(result.inverseRate),
                    result.baseCurrency.code,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun rememberAmountFormatter(): NumberFormat {
    return remember {
        NumberFormat.getNumberInstance(Locale.getDefault()).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
    }
}

@Composable
private fun rememberRateFormatter(): NumberFormat {
    return remember {
        NumberFormat.getNumberInstance(Locale.getDefault()).apply {
            minimumFractionDigits = 4
            maximumFractionDigits = 6
        }
    }
}

private fun sanitizeDecimalInput(input: String): String {
    val filtered = input.filter { it.isDigit() || it == '.' }
    val firstDotIndex = filtered.indexOf('.')
    return if (firstDotIndex == -1) {
        filtered
    } else {
        val beforeDot = filtered.substring(0, firstDotIndex + 1)
        val afterDot = filtered.substring(firstDotIndex + 1).replace(".", "")
        beforeDot + afterDot
    }
}
