package com.evening.dailylife.app.widget

import android.content.Context
import com.evening.dailylife.core.data.repository.TransactionRepository
import com.evening.dailylife.core.di.ApplicationScope
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest

@Singleton
class TransactionWidgetUpdater @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val repository: TransactionRepository,
    @ApplicationScope private val applicationScope: CoroutineScope
) {

    init {
        applicationScope.launch {
            repository.observeAllTransactions().collectLatest {
                runCatching {
                    TransactionSummaryWidget.refresh(appContext)
                }
            }
        }
    }

    fun requestImmediateUpdate() {
        applicationScope.launch {
            runCatching {
                TransactionSummaryWidget.refresh(appContext)
            }
        }
    }
}
