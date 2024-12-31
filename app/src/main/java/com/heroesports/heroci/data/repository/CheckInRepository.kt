package com.heroesports.heroci.data.repository

import com.heroesports.heroci.data.dao.CheckInDao
import com.heroesports.heroci.data.entity.CheckIn
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CheckInRepository @Inject constructor(
    private val checkInDao: CheckInDao
) {
    fun getCheckInsByProjectId(projectId: Long): Flow<List<CheckIn>> =
        checkInDao.getCheckInsByProjectId(projectId)

    suspend fun getCheckInsByProjectIdSync(projectId: Long): List<CheckIn> =
        checkInDao.getCheckInsByProjectIdSync(projectId)

    fun getCheckInById(checkInId: Long): Flow<CheckIn?> {
        return checkInDao.getCheckInById(checkInId)
    }

    suspend fun insertCheckIn(checkIn: CheckIn) {
        // 先删除同一天的打卡记录
        checkInDao.deleteExistingCheckIn(
            projectId = checkIn.projectId,
            memberName = checkIn.memberName,
            checkInTime = checkIn.checkInTime
        )
        // 插入新的打卡记录
        checkInDao.insertCheckIn(checkIn)
    }

    suspend fun updateCheckIn(checkInId: Long, newTime: LocalDateTime, newLocation: String) {
        checkInDao.getCheckInByIdSync(checkInId)?.let { checkIn ->
            val updatedCheckIn = checkIn.copy(
                checkInTime = newTime,
                location = newLocation
            )
            checkInDao.updateCheckIn(updatedCheckIn)
        }
    }

    suspend fun deleteCheckIn(checkInId: Long) {
        checkInDao.deleteCheckIn(checkInId)
    }

    suspend fun deleteProjectCheckIns(projectId: Long) = checkInDao.deleteByProjectId(projectId)
} 