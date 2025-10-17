package com.evening.dailylife.core.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.evening.dailylife.core.data.local.converter.Converters

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["isDeleted", "date"]),
        Index(value = ["category"]),
        Index(value = ["mood"])
    ]
)
@TypeConverters(Converters::class)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val category: String,
    val description: String,
    val amount: Double,
    val mood: Int?,
    val source: String = "",
    val date: Long,
    val isDeleted: Boolean = false
)
