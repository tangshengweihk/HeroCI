package com.heroesports.heroci.data.repository

import com.heroesports.heroci.data.dao.ProjectDao
import com.heroesports.heroci.data.entity.Project
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ProjectRepository @Inject constructor(
    private val projectDao: ProjectDao
) {
    fun getAllProjects(): Flow<List<Project>> = projectDao.getAllProjects()

    suspend fun getProjectById(projectId: Long): Project? = projectDao.getProjectById(projectId)

    suspend fun createProject(project: Project): Long = projectDao.insert(project)

    suspend fun deleteProject(projectId: Long) = projectDao.deleteById(projectId)

    suspend fun updateProject(project: Project) = projectDao.insert(project)

    suspend fun updatePassword(projectId: Long, newPassword: String) = 
        projectDao.updatePassword(projectId, newPassword)
} 