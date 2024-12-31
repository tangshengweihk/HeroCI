package com.heroesports.heroci.data.dao

import androidx.room.*
import com.heroesports.heroci.data.entity.CheckIn
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface CheckInDao {
    @Query("SELECT * FROM check_ins WHERE projectId = :projectId ORDER BY checkInTime DESC")
    fun getCheckInsByProjectId(projectId: Long): Flow<List<CheckIn>>

    @Query("SELECT * FROM check_ins WHERE projectId = :projectId ORDER BY checkInTime DESC")
    suspend fun getCheckInsByProjectIdSync(projectId: Long): List<CheckIn>

    @Query("SELECT * FROM check_ins WHERE id = :checkInId")
    fun getCheckInById(checkInId: Long): Flow<CheckIn?>

    @Query("SELECT * FROM check_ins WHERE id = :checkInId")
    suspend fun getCheckInByIdSync(checkInId: Long): CheckIn?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckIn(checkIn: CheckIn)

    @Update
    suspend fun updateCheckIn(checkIn: CheckIn)

    @Query("DELETE FROM check_ins WHERE id = :checkInId")
    suspend fun deleteCheckIn(checkInId: Long)

    @Query("DELETE FROM check_ins WHERE projectId = :projectId")
    suspend fun deleteByProjectId(projectId: Long)

    @Query("DELETE FROM check_ins WHERE projectId = :projectId AND memberName = :memberName AND date(checkInTime) = date(:checkInTime)")
    suspend fun deleteExistingCheckIn(projectId: Long, memberName: String, checkInTime: LocalDateTime)
} 