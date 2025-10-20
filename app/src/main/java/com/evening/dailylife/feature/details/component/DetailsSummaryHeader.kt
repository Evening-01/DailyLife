package com.evening.dailylife.feature.details.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evening.dailylife.R

@Composable
fun DetailsSummaryHeader(
    year: String,
    month: String,
    income: String,
    expense: String,
    onDateClick: () -> Unit,
    containerColor: Color,
    contentColor: Color,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(containerColor)
            .padding(top = 4.dp, bottom = 16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DatePickerModule(
                year = year,
                month = month,
                onClick = onDateClick,
                contentColor = contentColor,
                modifier = Modifier.weight(1f),
            )
            VerticalDivider(contentColor)
            IncomeExpenseGroup(
                income = income,
                expense = expense,
                contentColor = contentColor,
                modifier = Modifier.weight(3f),
            )
        }
    }
}

@Composable
private fun DatePickerModule(
    year: String,
    month: String,
    onClick: () -> Unit,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 16.dp),
    ) {
        Text(
            text = year,
            color = contentColor.copy(alpha = 0.8f),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = month,
                color = contentColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = stringResource(R.string.details_select_month),
                tint = contentColor,
            )
        }
    }
}

@Composable
private fun IncomeExpenseGroup(
    income: String,
    expense: String,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SummaryItem(
            title = stringResource(R.string.chart_type_income),
            amount = income,
            contentColor = contentColor,
        )
        SummaryItem(
            title = stringResource(R.string.chart_type_expense),
            amount = expense,
            contentColor = contentColor,
        )
    }
}

@Composable
private fun VerticalDivider(color: Color) {
    Box(
        modifier = Modifier
            .fillMaxHeight(0.6f)
            .width(1.dp)
            .background(color.copy(alpha = 0.3f)),
    )
}

@Composable
private fun SummaryItem(title: String, amount: String, contentColor: Color) {
    Column(
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = title,
            color = contentColor.copy(alpha = 0.8f),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = amount,
            color = contentColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
