package com.heroesports.heroci.data.dao

import androidx.room.*
import com.heroesports.heroci.data.entity.Member
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberDao {
    @Query("SELECT * FROM members WHERE projectId = :projectId ORDER BY name")
    fun getMembersByProjectId(projectId: Long): Flow<List<Member>>

    @Query("SELECT * FROM members WHERE projectId = :projectId ORDER BY name")
    suspend fun getMembersByProjectIdSync(projectId: Long): List<Member>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(member: Member)

    @Query("DELETE FROM members WHERE projectId = :projectId AND name = :name")
    suspend fun deleteByProjectIdAndName(projectId: Long, name: String)

    @Query("DELETE FROM members WHERE projectId = :projectId")
    suspend fun deleteByProjectId(projectId: Long)
} 