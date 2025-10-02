package com.evening.dailylife.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.evening.dailylife.data.local.converter.Converters

@Entity(tableName = "transactions")
@TypeConverters(Converters::class)
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val category: String,
    val description: String,
    val amount: Double,
    val icon: String, // Storing icon name as String
    val date: Long // Storing date as timestamp for easier querying
)