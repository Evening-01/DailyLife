package com.evening.dailylife.core.util

/**
 * 语义化版本比较工具。
 */
object VersionComparator {

    fun isNewer(current: String, candidate: String): Boolean {
        return versionStringToDouble(current) < versionStringToDouble(candidate)
    }

    private fun versionStringToDouble(version: String): Double {
        val versionArray = version.split(".")
        val integerPart = versionArray[0]
        val decimalPart = versionArray.drop(1).joinToString("")
        return "$integerPart.$decimalPart".toDouble()
    }
}
