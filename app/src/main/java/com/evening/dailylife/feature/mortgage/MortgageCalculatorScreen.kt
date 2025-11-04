package com.evening.dailylife.feature.mortgage

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.evening.dailylife.R
import com.evening.dailylife.feature.mortgage.model.MortgageAmortizationType
import com.evening.dailylife.feature.mortgage.model.MortgageCalculationResult
import com.evening.dailylife.feature.mortgage.model.MortgageCalculatorUiState
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.TitleBar
import com.moriafly.salt.ui.UnstableSaltApi
import com.moriafly.salt.ui.ext.safeMainPadding
import java.text.NumberFormat
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, UnstableSaltApi::class)
@Composable
fun MortgageCalculatorScreen(
    onBackClick: () -> Unit,
    viewModel: MortgageCalculatorViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val amountFormatter = rememberAmountFormatter()
    val currencySymbol = stringResource(id = R.string.mortgage_calculator_currency_symbol)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SaltTheme.colors.background)
            .safeMainPadding(),
    ) {
        TitleBar(
            onBack = onBackClick,
            text = stringResource(id = R.string.discover_common_tool_mortgage_title),
        )

        MortgageCalculatorContent(
            uiState = uiState,
            currencySymbol = currencySymbol,
            amountFormatter = amountFormatter,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            onPrincipalChange = viewModel::onPrincipalChange,
            onAnnualRateChange = viewModel::onAnnualRateChange,
            onTermYearsChange = viewModel::onTermYearsChange,
            onAmortizationTypeChange = viewModel::onAmortizationTypeChange,
            onCalculateClick = {
                focusManager.clearFocus()
                viewModel.onCalculateClick()
            },
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun MortgageCalculatorContent(
    uiState: MortgageCalculatorUiState,
    currencySymbol: String,
    amountFormatter: NumberFormat,
    modifier: Modifier = Modifier,
    onPrincipalChange: (String) -> Unit,
    onAnnualRateChange: (String) -> Unit,
    onTermYearsChange: (String) -> Unit,
    onAmortizationTypeChange: (MortgageAmortizationType) -> Unit,
    onCalculateClick: () -> Unit,
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
            currencySymbol = currencySymbol,
            onPrincipalChange = onPrincipalChange,
            onAnnualRateChange = onAnnualRateChange,
            onTermYearsChange = onTermYearsChange,
            onAmortizationTypeChange = onAmortizationTypeChange,
            onCalculateClick = onCalculateClick,
        )
        uiState.calculation?.let { result ->
            ResultCard(
                result = result,
                currencySymbol = currencySymbol,
                amountFormatter = amountFormatter,
            )
        }
    }
}

@Composable
private fun InputCard(
    uiState: MortgageCalculatorUiState,
    currencySymbol: String,
    onPrincipalChange: (String) -> Unit,
    onAnnualRateChange: (String) -> Unit,
    onTermYearsChange: (String) -> Unit,
    onAmortizationTypeChange: (MortgageAmortizationType) -> Unit,
    onCalculateClick: () -> Unit,
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
                text = stringResource(id = R.string.mortgage_calculator_inputs_title),
                style = MaterialTheme.typography.titleMedium,
            )
            OutlinedTextField(
                value = uiState.principalInput,
                onValueChange = { value -> onPrincipalChange(sanitizeDecimalInput(value)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = stringResource(id = R.string.mortgage_calculator_principal_label)) },
                placeholder = { Text(text = stringResource(id = R.string.mortgage_calculator_principal_placeholder)) },
                isError = uiState.principalError != null,
                supportingText = uiState.principalError?.let { errorRes ->
                    {
                        Text(
                            text = stringResource(id = errorRes),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                },
                prefix = { Text(text = currencySymbol) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next,
                ),
            )
            OutlinedTextField(
                value = uiState.annualRateInput,
                onValueChange = { value -> onAnnualRateChange(sanitizeDecimalInput(value)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = stringResource(id = R.string.mortgage_calculator_annual_rate_label)) },
                placeholder = { Text(text = stringResource(id = R.string.mortgage_calculator_annual_rate_placeholder)) },
                isError = uiState.annualRateError != null,
                supportingText = uiState.annualRateError?.let { errorRes ->
                    {
                        Text(
                            text = stringResource(id = errorRes),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                },
                suffix = { Text(text = "%") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next,
                ),
            )
            OutlinedTextField(
                value = uiState.termYearsInput,
                onValueChange = { value -> onTermYearsChange(sanitizeIntegerInput(value)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = stringResource(id = R.string.mortgage_calculator_term_label)) },
                placeholder = { Text(text = stringResource(id = R.string.mortgage_calculator_term_placeholder)) },
                isError = uiState.termYearsError != null,
                supportingText = uiState.termYearsError?.let { errorRes ->
                    {
                        Text(
                            text = stringResource(id = errorRes),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                },
                suffix = { Text(text = stringResource(id = R.string.mortgage_calculator_years_suffix)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        if (uiState.isCalculateEnabled) {
                            onCalculateClick()
                        }
                    },
                ),
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(id = R.string.mortgage_calculator_type_section_title),
                    style = MaterialTheme.typography.titleSmall,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    MortgageAmortizationType.entries.forEach { type ->
                        val selected = uiState.amortizationType == type
                        FilterChip(
                            selected = selected,
                            onClick = { onAmortizationTypeChange(type) },
                            label = {
                                Text(
                                    text = stringResource(id = type.labelResId),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                            leadingIcon = if (selected) {
                                {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                    )
                                }
                            } else {
                                null
                            },
                        )
                    }
                }
            }
            Button(
                onClick = onCalculateClick,
                enabled = uiState.isCalculateEnabled,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(id = R.string.mortgage_calculator_calculate_action))
            }
        }
    }
}

@Composable
private fun ResultCard(
    result: MortgageCalculationResult,
    currencySymbol: String,
    amountFormatter: NumberFormat,
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
                text = stringResource(id = R.string.mortgage_calculator_result_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(
                    id = R.string.mortgage_calculator_result_term,
                    result.loanMonths,
                    result.loanYears,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            HorizontalDivider()
            ResultRow(
                label = when (result.amortizationType) {
                    MortgageAmortizationType.EqualPrincipalInterest ->
                        stringResource(id = R.string.mortgage_calculator_result_monthly_payment)

                    MortgageAmortizationType.EqualPrincipal ->
                        stringResource(id = R.string.mortgage_calculator_result_average_monthly_payment)
                },
                value = formatCurrency(result.typicalMonthlyPayment, currencySymbol, amountFormatter),
            )
            ResultRow(
                label = stringResource(id = R.string.mortgage_calculator_result_total_payment),
                value = formatCurrency(result.totalPayment, currencySymbol, amountFormatter),
            )
            ResultRow(
                label = stringResource(id = R.string.mortgage_calculator_result_total_interest),
                value = formatCurrency(result.totalInterest, currencySymbol, amountFormatter),
            )
            if (result.firstMonthPayment != null && result.lastMonthPayment != null && result.monthlyPaymentDecrease != null) {
                HorizontalDivider()
                ResultRow(
                    label = stringResource(id = R.string.mortgage_calculator_result_first_month_payment),
                    value = formatCurrency(result.firstMonthPayment, currencySymbol, amountFormatter),
                )
                ResultRow(
                    label = stringResource(id = R.string.mortgage_calculator_result_last_month_payment),
                    value = formatCurrency(result.lastMonthPayment, currencySymbol, amountFormatter),
                )
                ResultRow(
                    label = stringResource(id = R.string.mortgage_calculator_result_monthly_decrease),
                    value = formatCurrency(result.monthlyPaymentDecrease, currencySymbol, amountFormatter),
                )
            }
        }
    }
}

@Composable
private fun ResultRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun formatCurrency(
    value: java.math.BigDecimal,
    currencySymbol: String,
    formatter: NumberFormat,
): String {
    return "$currencySymbol ${formatter.format(value)}"
}

@Composable
private fun rememberAmountFormatter(): NumberFormat {
    return remember {
        NumberFormat.getNumberInstance(Locale.getDefault()).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 2
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

private fun sanitizeIntegerInput(input: String): String {
    return input.filter { it.isDigit() }
}
