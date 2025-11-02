package com.evening.dailylife.app.widget

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.evening.dailylife.R
import com.evening.dailylife.app.main.MainActivity
import com.evening.dailylife.app.navigation.Route
import com.evening.dailylife.core.data.repository.TransactionRepository
import com.evening.dailylife.core.model.TransactionCategoryRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import kotlin.math.abs

@OptIn(androidx.glance.ExperimentalGlanceApi::class)
class TransactionSummaryWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(
            DpSize(110.dp, 110.dp),
            DpSize(220.dp, 160.dp),
            DpSize(250.dp, 250.dp)
        )
    )

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context,
            TransactionWidgetEntryPoint::class.java
        )
        val repository = entryPoint.transactionRepository()
        val state = buildTransactionWidgetState(
            transactions = repository.getTransactionsSnapshot(),
            zoneId = ZoneId.systemDefault(),
            now = Instant.now()
        )

        provideContent {
            WidgetSurface {
                WidgetContent(state = state)
            }
        }
    }

    companion object {
        suspend fun refresh(context: Context) {
            TransactionSummaryWidget().updateAll(context)
        }
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface TransactionWidgetEntryPoint {
    fun transactionRepository(): TransactionRepository
}

private val WidgetBackground = ColorProvider(
    day = Color(0xFFF5F3FF),
    night = Color(0xFF1A1D2F)
)
private val CardBackground = ColorProvider(
    day = Color(0x33FFFFFF),
    night = Color(0x33202639)
)
private val PrimaryTextColor = ColorProvider(
    day = Color(0xFF1F2140),
    night = Color(0xFFE7E8FF)
)
private val SecondaryTextColor = ColorProvider(
    day = Color(0xFF5B5E77),
    night = Color(0xFFB6BAD4)
)
private val AccentExpense = ColorProvider(
    day = Color(0xFF4F46E5),
    night = Color(0xFF8B8CFF)
)
private val AccentIncome = ColorProvider(
    day = Color(0xFF2F9F70),
    night = Color(0xFF63D098)
)
private val AccentNeutral = ColorProvider(
    day = Color(0x33202338),
    night = Color(0x33283245)
)
private val ExpenseChipColor = ColorProvider(
    day = Color(0x33FF6B6B),
    night = Color(0x4D8C3030)
)
private val IncomeChipColor = ColorProvider(
    day = Color(0x3326D482),
    night = Color(0x4D24583B)
)

@OptIn(androidx.glance.ExperimentalGlanceApi::class)
@Composable
private fun WidgetSurface(content: @Composable () -> Unit) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .cornerRadius(24.dp)
            .background(WidgetBackground)
            .padding(18.dp),
        verticalAlignment = Alignment.Vertical.Top,
        horizontalAlignment = Alignment.Horizontal.Start
    ) {
        content()
    }
}

@OptIn(androidx.glance.ExperimentalGlanceApi::class)
@Composable
private fun WidgetContent(state: TransactionWidgetState) {
    val size = LocalSize.current
    val isCompact = size.width <= 150.dp && size.height <= 150.dp
    val isMedium = !isCompact && size.height <= 220.dp

    when {
        isCompact -> CompactWidget(state)
        isMedium -> MediumWidget(state)
        else -> ExpandedWidget(state)
    }
}

@Composable
private fun CompactWidget(state: TransactionWidgetState) {
    val context = LocalContext.current
    Header(text = context.getString(R.string.widget_title_today))
    Spacer(modifier = GlanceModifier.height(10.dp))
    MetricText(
        label = context.getString(R.string.widget_total_expense),
        amount = state.totalExpenseToday,
        emphasize = true
    )
    Spacer(modifier = GlanceModifier.height(6.dp))
    MetricText(
        label = context.getString(R.string.widget_total_income),
        amount = state.totalIncomeToday,
        emphasize = false
    )
    Spacer(modifier = GlanceModifier.height(12.dp))
    LastTransactionCard(state)
    Spacer(modifier = GlanceModifier.height(14.dp))
    WidgetActionButton(
        label = context.getString(R.string.widget_quick_add),
        background = AccentExpense,
        textColor = ColorProvider(
            day = Color.White,
            night = Color(0xFF090B13)
        ),
        onClick = quickAddAction(
            context = context,
            isExpense = true,
            categoryId = null
        )
    )
}

@Composable
private fun MediumWidget(state: TransactionWidgetState) {
    val context = LocalContext.current
    Header(text = context.getString(R.string.widget_title_today))
    Spacer(modifier = GlanceModifier.height(10.dp))
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.Start
    ) {
        MetricColumn(
            label = context.getString(R.string.widget_total_expense),
            amount = state.totalExpenseToday,
            emphasize = true,
            modifier = GlanceModifier.defaultWeight()
        )
        Spacer(modifier = GlanceModifier.width(12.dp))
        MetricColumn(
            label = context.getString(R.string.widget_total_income),
            amount = state.totalIncomeToday,
            emphasize = false,
            modifier = GlanceModifier.defaultWeight()
        )
        Spacer(modifier = GlanceModifier.width(12.dp))
        MetricColumn(
            label = context.getString(R.string.widget_total_balance),
            amount = state.netToday,
            emphasize = false,
            modifier = GlanceModifier.defaultWeight()
        )
    }
    Spacer(modifier = GlanceModifier.height(12.dp))
    LastTransactionCard(state)
    Spacer(modifier = GlanceModifier.height(14.dp))
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.Start
    ) {
        WidgetActionButton(
            label = context.getString(R.string.widget_action_add_expense),
            background = AccentExpense,
            textColor = ColorProvider(
                day = Color.White,
                night = Color(0xFF090B13)
            ),
            modifier = GlanceModifier.defaultWeight(),
            onClick = quickAddAction(context, true, null)
        )
        Spacer(modifier = GlanceModifier.width(10.dp))
        WidgetActionButton(
            label = context.getString(R.string.widget_action_add_income),
            background = AccentIncome,
            textColor = ColorProvider(
                day = Color.White,
                night = Color(0xFF0A130E)
            ),
            modifier = GlanceModifier.defaultWeight(),
            onClick = quickAddAction(context, false, null)
        )
    }
}

@Composable
private fun ExpandedWidget(state: TransactionWidgetState) {
    val context = LocalContext.current
    Header(text = context.getString(R.string.widget_title_today))
    Spacer(modifier = GlanceModifier.height(10.dp))
    MetricSummaryPill(
        title = context.getString(R.string.widget_total_expense),
        amount = state.totalExpenseToday,
        background = AccentNeutral
    )
    Spacer(modifier = GlanceModifier.height(8.dp))
    MetricSummaryPill(
        title = context.getString(R.string.widget_total_income),
        amount = state.totalIncomeToday,
        background = AccentNeutral
    )
    Spacer(modifier = GlanceModifier.height(8.dp))
    MetricSummaryPill(
        title = context.getString(R.string.widget_total_balance),
        amount = state.netToday,
        background = AccentNeutral
    )
    Spacer(modifier = GlanceModifier.height(16.dp))
    LastTransactionCard(state)
    Spacer(modifier = GlanceModifier.height(16.dp))
    FrequentCategoriesSection(state.frequentCategories)
    Spacer(modifier = GlanceModifier.height(16.dp))
    WidgetActionButton(
        label = context.getString(R.string.widget_action_add_expense),
        background = AccentExpense,
        textColor = ColorProvider(
            day = Color.White,
            night = Color(0xFF090B13)
        ),
        onClick = quickAddAction(context, true, null)
    )
    Spacer(modifier = GlanceModifier.height(10.dp))
    WidgetActionButton(
        label = context.getString(R.string.widget_action_add_income),
        background = AccentIncome,
        textColor = ColorProvider(
            day = Color.White,
            night = Color(0xFF0A130E)
        ),
        onClick = quickAddAction(context, false, null)
    )
    Spacer(modifier = GlanceModifier.height(10.dp))
    WidgetActionButton(
        label = context.getString(R.string.widget_action_open_app),
        background = AccentNeutral,
        textColor = PrimaryTextColor,
        onClick = openAppAction(context)
    )
}

@Composable
private fun Header(text: String) {
    Text(
        text = text,
        style = TextStyle(
            color = PrimaryTextColor,
            fontWeight = FontWeight.Bold
        ),
        maxLines = 1
    )
}

@Composable
private fun MetricText(
    label: String,
    amount: Double,
    emphasize: Boolean
) {
    Text(
        text = "$label  ${formatAmount(amount)}",
        style = TextStyle(
            color = if (emphasize) PrimaryTextColor else SecondaryTextColor,
            fontWeight = if (emphasize) FontWeight.Bold else FontWeight.Medium
        ),
        maxLines = 1
    )
}

@Composable
private fun MetricColumn(
    label: String,
    amount: Double,
    emphasize: Boolean,
    modifier: GlanceModifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Horizontal.Start,
        verticalAlignment = Alignment.Vertical.Top
    ) {
        Text(
            text = label,
            style = TextStyle(
                color = SecondaryTextColor,
                fontWeight = FontWeight.Medium
            ),
            maxLines = 1
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
            text = formatAmount(amount),
            style = TextStyle(
                color = if (emphasize) PrimaryTextColor else SecondaryTextColor,
                fontWeight = if (emphasize) FontWeight.Bold else FontWeight.Medium
            ),
            maxLines = 1
        )
    }
}

@Composable
private fun MetricSummaryPill(
    title: String,
    amount: Double,
    background: ColorProvider
) {
    Column(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(background)
            .cornerRadius(18.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.Horizontal.Start,
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        Text(
            text = title,
            style = TextStyle(
                color = SecondaryTextColor,
                fontWeight = FontWeight.Medium
            ),
            maxLines = 1
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
            text = formatAmount(amount),
            style = TextStyle(
                color = PrimaryTextColor,
                fontWeight = FontWeight.Bold
            ),
            maxLines = 1
        )
    }
}

@Composable
private fun LastTransactionCard(state: TransactionWidgetState) {
    val context = LocalContext.current
    val transaction = state.lastTransaction
    val headline = if (transaction != null) {
        val categoryLabel = TransactionCategoryRepository.getDisplayName(context, transaction.categoryId)
        val formattedAmount = formatAmount(transaction.amount)
        context.getString(R.string.widget_last_transaction, categoryLabel, formattedAmount)
    } else {
        context.getString(R.string.widget_last_transaction_empty)
    }

    Column(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(CardBackground)
            .cornerRadius(18.dp)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.Horizontal.Start,
        verticalAlignment = Alignment.Vertical.Top
    ) {
        Text(
            text = headline,
            style = TextStyle(
                color = PrimaryTextColor,
                fontWeight = FontWeight.Medium
            ),
            maxLines = 2
        )
        if (transaction != null && transaction.description.isNotBlank()) {
            Spacer(modifier = GlanceModifier.height(6.dp))
            Text(
                text = transaction.description,
                style = TextStyle(
                    color = SecondaryTextColor,
                    fontWeight = FontWeight.Normal
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun FrequentCategoriesSection(categories: List<FrequentCategory>) {
    if (categories.isEmpty()) return
    val context = LocalContext.current
    Column(
        horizontalAlignment = Alignment.Horizontal.Start,
        verticalAlignment = Alignment.Vertical.Top
    ) {
        Text(
            text = context.getString(R.string.widget_top_categories),
            style = TextStyle(
                color = PrimaryTextColor,
                fontWeight = FontWeight.Bold
            ),
            maxLines = 1
        )
        Spacer(modifier = GlanceModifier.height(8.dp))
        Row(
            horizontalAlignment = Alignment.Horizontal.Start,
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            val items = categories.take(3)
            items.forEachIndexed { index, category ->
                val label = TransactionCategoryRepository.getDisplayName(context, category.categoryId)
                CategoryChip(
                    label = label,
                    background = if (category.isExpense) ExpenseChipColor else IncomeChipColor,
                    onClick = quickAddAction(
                        context = context,
                        isExpense = category.isExpense,
                        categoryId = category.categoryId
                    )
                )
                if (index < items.lastIndex) {
                    Spacer(modifier = GlanceModifier.width(8.dp))
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(
    label: String,
    background: ColorProvider,
    onClick: Action
) {
    Text(
        text = label,
        modifier = GlanceModifier
            .background(background)
            .cornerRadius(16.dp)
            .clickable(onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        style = TextStyle(
            color = PrimaryTextColor,
            fontWeight = FontWeight.Medium
        ),
        maxLines = 1
    )
}

@Composable
private fun WidgetActionButton(
    label: String,
    background: ColorProvider,
    textColor: ColorProvider,
    modifier: GlanceModifier = GlanceModifier.fillMaxWidth(),
    onClick: Action
) {
    Box(
        modifier = modifier
            .background(background)
            .cornerRadius(18.dp)
            .clickable(onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = TextStyle(
                color = textColor,
                fontWeight = FontWeight.Medium
            ),
            maxLines = 1
        )
    }
}

private fun formatAmount(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance().apply {
        maximumFractionDigits = 2
        minimumFractionDigits = 0
        isGroupingUsed = true
    }
    val absolute = abs(amount)
    return if (amount < 0) "-${formatter.format(absolute)}" else formatter.format(absolute)
}

private fun quickAddAction(
    context: Context,
    isExpense: Boolean,
    categoryId: String?
): Action {
    val route = Route.addNewTransactionShortcut(
        categoryId = categoryId,
        isExpense = isExpense
    )
    return actionStartActivity(
        Intent(context, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_NAVIGATE_ROUTE, route)
            putExtra(MainActivity.EXTRA_WIDGET_IS_EXPENSE, isExpense)
            putExtra(MainActivity.EXTRA_WIDGET_CATEGORY_ID, categoryId)
        }
    )
}

private fun openAppAction(context: Context): Action =
    actionStartActivity(Intent(context, MainActivity::class.java))

class TransactionSummaryWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TransactionSummaryWidget()
}
