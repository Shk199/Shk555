package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DailyStat
import com.example.data.model.DhikrItem
import kotlinx.coroutines.flow.Flow

@Dao
interface TasbeehDao {

    @Query("SELECT * FROM azkar ORDER BY orderIndex ASC")
    fun getAllAzkar(): Flow<List<DhikrItem>>

    @Query("SELECT * FROM azkar WHERE id = :id LIMIT 1")
    fun getDhikrById(id: Int): Flow<DhikrItem?>

    @Query("SELECT COUNT(*) FROM azkar")
    suspend fun getAzkarCount(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllAzkar(azkar: List<DhikrItem>)

    @Update
    suspend fun updateDhikr(dhikr: DhikrItem)

    @Query("UPDATE azkar SET todayCount = todayCount + 1, totalCount = totalCount + 1, lastUpdatedDate = :date WHERE id = :id")
    suspend fun incrementDhikrCount(id: Int, date: String)

    @Query("UPDATE azkar SET todayCount = 0 WHERE id = :id")
    suspend fun resetDhikrTodayCount(id: Int)

    @Query("UPDATE azkar SET todayCount = 0 WHERE lastUpdatedDate != :currentDate")
    suspend fun resetOutdatedTodayCounts(currentDate: String)

    @Query("SELECT * FROM daily_stats WHERE date = :date LIMIT 1")
    fun getDailyStat(date: String): Flow<DailyStat?>

    @Query("SELECT * FROM daily_stats ORDER BY date DESC LIMIT :limit")
    fun getRecentDailyStats(limit: Int): Flow<List<DailyStat>>

    @Query("SELECT * FROM daily_stats ORDER BY date DESC")
    fun getAllDailyStats(): Flow<List<DailyStat>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDailyStat(stat: DailyStat)

    @Query("SELECT SUM(totalCount) FROM daily_stats")
    fun getTotalLifetimeTasbeeh(): Flow<Int?>
}
