package com.example.data.repository

import android.content.Context
import com.example.data.local.TasbeehDao
import com.example.data.local.TasbeehDatabase
import com.example.data.model.DailyStat
import com.example.data.model.DhikrItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TasbeehRepository(private val dao: TasbeehDao) {

    val allAzkar: Flow<List<DhikrItem>> = dao.getAllAzkar()
    val lifetimeTotal: Flow<Int?> = dao.getTotalLifetimeTasbeeh()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    private val dayNameFormat = SimpleDateFormat("EEEE", Locale("ar"))

    fun getCurrentDate(): String = dateFormat.format(Date())

    fun getCurrentDayName(): String = dayNameFormat.format(Date())

    suspend fun ensureAzkarPopulated() = withContext(Dispatchers.IO) {
        dao.insertAllAzkar(TasbeehDatabase.INITIAL_AZKAR)
        dao.resetOutdatedTodayCounts(getCurrentDate())
    }

    fun getTodayStat(): Flow<DailyStat?> {
        return dao.getDailyStat(getCurrentDate())
    }

    fun getRecentStats(limit: Int = 7): Flow<List<DailyStat>> {
        return dao.getRecentDailyStats(limit)
    }

    suspend fun incrementTasbeeh(dhikrId: Int) = withContext(Dispatchers.IO) {
        val today = getCurrentDate()
        val dayName = getCurrentDayName()

        // 1. Update Dhikr count
        dao.incrementDhikrCount(dhikrId, today)

        // 2. Update DailyStat
        val existingStat = dao.getDailyStat(today).firstOrNull()
        val newCount = (existingStat?.totalCount ?: 0) + 1
        dao.insertOrUpdateDailyStat(
            DailyStat(
                date = today,
                totalCount = newCount,
                dayName = dayName
            )
        )
    }

    suspend fun resetDhikr(dhikrId: Int) = withContext(Dispatchers.IO) {
        dao.resetDhikrTodayCount(dhikrId)
    }

    suspend fun updateDhikr(dhikr: DhikrItem) = withContext(Dispatchers.IO) {
        dao.updateDhikr(dhikr)
    }
}
