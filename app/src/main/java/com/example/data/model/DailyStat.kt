package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_stats")
data class DailyStat(
    @PrimaryKey val date: String, // YYYY-MM-DD
    val totalCount: Int,
    val dayName: String
)
