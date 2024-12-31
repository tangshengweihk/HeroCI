package com.heroesports.heroci.data.dao

import androidx.room.*
import com.heroesports.heroci.data.entity.CheckInRecord
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface CheckInRecordDao {
    @Query("SELECT * FROM check_in_records WHERE projectId = :projectId ORDER BY checkInTime DESC")
    fun getProjectCheckIns(projectId: Long): Flow<List<CheckInRecord>>

    @Query("SELECT * FROM check_in_records WHERE projectId = :projectId AND checkInTime BETWEEN :startTime AND :endTime ORDER BY checkInTime DESC")
    fun getProjectCheckInsByDateRange(
        projectId: Long,
        startTime: LocalDateTime,
        endTime: LocalDateTime
    ): Flow<List<CheckInRecord>>

    @Insert
    suspend fun insertCheckIn(checkInRecord: CheckInRecord): Long

    @Update
    suspend fun updateCheckIn(checkInRecord: CheckInRecord)

    @Delete
    suspend fun deleteCheckIn(checkInRecord: CheckInRecord)
} 