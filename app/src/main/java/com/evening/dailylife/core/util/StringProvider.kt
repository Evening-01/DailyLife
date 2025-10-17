package com.evening.dailylife.core.util

import android.content.Context
import androidx.annotation.StringRes
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 提供在非 @Composable 环境下获取字符串资源的能力。
 */
@Singleton
class StringProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getString(@StringRes resId: Int): String = context.getString(resId)

    fun getString(@StringRes resId: Int, vararg args: Any): String =
        context.getString(resId, *args)
}
