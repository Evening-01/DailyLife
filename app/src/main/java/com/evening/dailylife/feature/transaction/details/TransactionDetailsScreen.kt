package com.evening.dailylife.feature.transaction.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.evening.dailylife.app.navigation.Route
import com.evening.dailylife.core.data.local.entity.TransactionEntity
import com.evening.dailylife.core.model.MoodRepository
import com.evening.dailylife.core.model.TransactionCategoryRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailsScreen(
    navController: NavController,
    viewModel: TransactionDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("明细") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }
                uiState.error != null -> {
                    Text(text = uiState.error ?: "加载失败")
                }
                uiState.transaction != null -> {
                    TransactionDetailsContent(
                        transaction = uiState.transaction!!,
                        onDelete = {
                            viewModel.deleteTransaction(uiState.transaction!!) {
                                navController.navigateUp()
                            }
                        },
                        onEdit = {
                            // 点击编辑按钮，导航到编辑页面并传递 transactionId
                            navController.navigate(
                                Route.addEditTransactionWithId(uiState.transaction!!.id)
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionDetailsContent(
    transaction: TransactionEntity,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // 顶部信息卡片
        TransactionSummaryCard(
            transaction = transaction,
            iconVector = TransactionCategoryRepository.getIcon(transaction.category)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 详细信息列表
        DetailsList(transaction)

        // 占位，将按钮推到底部
        Spacer(modifier = Modifier.weight(1f))

        // 底部操作按钮
        ActionButtons(
            onDelete = onDelete,
            onEdit = onEdit
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}
@Composable
fun TransactionSummaryCard(
    transaction: TransactionEntity,
    iconVector: ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = transaction.category,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = String.format(Locale.CHINA, "%+.2f", transaction.amount),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .offset(x = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                val glassReflectionBrush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                        Color.White.copy(alpha = 0.5f)
                    ),
                    start = Offset.Zero,
                    end = Offset.Infinite
                )

                Icon(
                    imageVector = iconVector,
                    contentDescription = transaction.category,
                    modifier = Modifier
                        .size(96.dp)
                        .graphicsLayer(alpha = 0.99f)
                        .drawWithCache {
                            onDrawWithContent {
                                drawContent()
                                drawRect(
                                    brush = glassReflectionBrush,
                                    blendMode = BlendMode.SrcIn
                                )
                            }
                        }
                )
            }
        }
    }
}

@Composable
fun DetailsList(transaction: TransactionEntity) {
    val sdf = SimpleDateFormat("yyyy年MM月dd日 EEEE", Locale.CHINESE)
    val dateString = sdf.format(Date(transaction.date))

    Column(modifier = Modifier.fillMaxWidth()) {
        DetailItem(label = "分类", value = transaction.category)
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        DetailItem(label = "时间", value = dateString)
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        DetailItem(label = "来源", value = transaction.source)
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        // 仅在心情不为空时显示
        if (transaction.mood != null) {
            // 根据分数获取心情对象
            val mood = MoodRepository.getMoodByScore(transaction.mood)
            if (mood != null) {
                MoodDetailItem(
                    label = "心情",
                    mood = mood.name,
                    icon = {
                        Icon(
                            imageVector = mood.icon,
                            contentDescription = mood.name,
                            modifier = Modifier.size(24.dp),
                            tint = mood.color
                        )
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            }
        }

        DetailItem(label = "备注", value = transaction.description.ifBlank { "暂无" })
    }
}

@Composable
fun MoodDetailItem(label: String, mood: String, icon: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = mood,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.width(8.dp))
        icon()
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ActionButtons(
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedButton(
            onClick = onDelete,
            modifier = Modifier.weight(1f)
        ) {
            Text("删除")
        }
        OutlinedButton(
            onClick = onEdit,
            modifier = Modifier.weight(1f)
        ) {
            Text("编辑")
        }
    }
}