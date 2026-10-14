package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "azkar")
data class DhikrItem(
    @PrimaryKey val id: Int,
    val arabicText: String,
    val virtue: String,
    val targetCount: Int, // 33, 100, or 0 (open/مفتوح)
    val todayCount: Int = 0,
    val totalCount: Int = 0,
    val lastUpdatedDate: String = "",
    val orderIndex: Int = 0
) {
    val isTargetOpen: Boolean
        get() = targetCount <= 0
}
