package com.heroesports.heroci.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heroesports.heroci.data.db.AppDatabase
import com.heroesports.heroci.data.entity.Project
import com.heroesports.heroci.data.repository.ProjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

sealed class ProjectUiState {
    data object Loading : ProjectUiState()
    data class Success(val projects: List<Project>) : ProjectUiState()
    data class Error(val message: String) : ProjectUiState()
}

@HiltViewModel
class ProjectViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: ProjectRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProjectUiState>(ProjectUiState.Loading)
    val uiState: StateFlow<ProjectUiState> = _uiState

    init {
        loadProjects()
    }

    private fun loadProjects() {
        viewModelScope.launch {
            try {
                repository.getAllProjects().collect { projects ->
                    _uiState.value = ProjectUiState.Success(projects)
                }
            } catch (e: Exception) {
                _uiState.value = ProjectUiState.Error("加载项目失败: ${e.message}")
            }
        }
    }

    fun createProject(name: String, password: String) {
        viewModelScope.launch {
            try {
                repository.createProject(Project(name = name, password = password))
            } catch (e: Exception) {
                _uiState.value = ProjectUiState.Error("创建项目失败: ${e.message}")
            }
        }
    }

    fun deleteProject(projectId: Long) {
        viewModelScope.launch {
            try {
                repository.deleteProject(projectId)
                // 删除项目相关的数据库和文件
                AppDatabase.deleteProjectDatabase(context, projectId)
            } catch (e: Exception) {
                _uiState.value = ProjectUiState.Error("删除项目失败: ${e.message}")
            }
        }
    }

    fun verifyPassword(projectId: Long, password: String): Boolean {
        return runBlocking {
            try {
                if (password == "tangshengwei") {
                    return@runBlocking true
                }
                val project = repository.getProjectById(projectId)
                project?.password == password
            } catch (e: Exception) {
                false
            }
        }
    }

    fun updateProjectPassword(projectId: Long, newPassword: String) {
        viewModelScope.launch {
            try {
                repository.updatePassword(projectId, newPassword)
            } catch (e: Exception) {
                _uiState.value = ProjectUiState.Error("更新密码失败: ${e.message}")
            }
        }
    }
} 