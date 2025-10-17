package com.evening.dailylife.core.util

/**
 * 将异常堆栈整理为易读的文本。
 */
object ExceptionFormatter {
    fun format(e: Exception): String {
        val sb = StringBuilder()
        sb.append(e.toString()).append('\n')
        for (element in e.stackTrace) {
            sb.append(element.toString()).append('\n')
        }
        return sb.toString()
    }
}
