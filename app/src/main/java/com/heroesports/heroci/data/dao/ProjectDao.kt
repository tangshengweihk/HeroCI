package com.heroesports.heroci.data.dao

import androidx.room.*
import com.heroesports.heroci.data.entity.Project
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY createdAt DESC")
    fun getAllProjects(): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE id = :projectId")
    suspend fun getProjectById(projectId: Long): Project?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(project: Project): Long

    @Delete
    suspend fun delete(project: Project)

    @Query("DELETE FROM projects WHERE id = :projectId")
    suspend fun deleteById(projectId: Long)

    @Query("UPDATE projects SET password = :newPassword WHERE id = :projectId")
    suspend fun updatePassword(projectId: Long, newPassword: String)
} 