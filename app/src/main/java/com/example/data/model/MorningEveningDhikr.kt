package com.example.data.model

enum class MorningEveningCategory {
    MORNING,
    EVENING
}

data class MorningEveningDhikr(
    val id: Int,
    val category: MorningEveningCategory,
    val title: String,
    val arabicText: String,
    val targetCount: Int,
    val virtue: String,
    val takhrij: String,
    val orderIndex: Int
)

data class MorningEveningItemState(
    val dhikr: MorningEveningDhikr,
    val currentCount: Int = 0
) {
    val isCompleted: Boolean
        get() = currentCount >= dhikr.targetCount
}
