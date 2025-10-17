package com.evening.dailylife.core.util

/**
 * Map 排序与取值的辅助方法。
 */
object CollectionOrderUtils {

    fun sortByValue(map: Map<String, Double>, order: Char): List<Map.Entry<String, Double>> {
        val entryList = ArrayList(map.entries)
        entryList.sortWith { first, second ->
            if (order == 'A') {
                first.value.compareTo(second.value)
            } else {
                second.value.compareTo(first.value)
            }
        }
        return entryList
    }

    fun sortByKey(map: Map<Int, Int>, order: Char): List<Map.Entry<Int, Int>> {
        val entryList = ArrayList(map.entries)
        entryList.sortWith { first, second ->
            if (order == 'A') {
                first.key.compareTo(second.key)
            } else {
                second.key.compareTo(first.key)
            }
        }
        return entryList
    }

    fun getMaxValueStringDouble(map: Map<String, Double>): Map.Entry<String, Double>? {
        var maxEntry: Map.Entry<String, Double>? = null
        for (entry in map.entries) {
            if (maxEntry == null || entry.value > maxEntry!!.value) {
                maxEntry = entry
            }
        }
        return maxEntry
    }

    fun getMaxValueIntDouble(map: Map<Int, Double>): Map.Entry<Int, Double>? {
        var maxEntry: Map.Entry<Int, Double>? = null
        for (entry in map.entries) {
            if (maxEntry == null || entry.value > maxEntry!!.value) {
                maxEntry = entry
            }
        }
        return maxEntry
    }

    fun getMinValue(map: Map<String, Double>): Map.Entry<String, Double>? {
        var minEntry: Map.Entry<String, Double>? = null
        for (entry in map.entries) {
            if (minEntry == null || entry.value < minEntry!!.value) {
                minEntry = entry
            }
        }
        return minEntry
    }
}
