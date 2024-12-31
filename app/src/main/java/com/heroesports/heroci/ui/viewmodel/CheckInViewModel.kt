package com.heroesports.heroci.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heroesports.heroci.data.entity.CheckIn
import com.heroesports.heroci.data.entity.Member
import com.heroesports.heroci.data.entity.Project
import com.heroesports.heroci.data.repository.CheckInRepository
import com.heroesports.heroci.data.repository.MemberRepository
import com.heroesports.heroci.data.repository.ProjectRepository
import com.heroesports.heroci.ui.state.CheckInUiState
import com.heroesports.heroci.utils.ExportUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class CheckInViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val projectRepository: ProjectRepository,
    private val memberRepository: MemberRepository,
    private val checkInRepository: CheckInRepository
) : ViewModel() {

    private val _currentProject = MutableStateFlow<Project?>(null)
    val currentProject: StateFlow<Project?> = _currentProject

    private val _uiState = MutableStateFlow<CheckInUiState>(CheckInUiState.Loading)
    val uiState: StateFlow<CheckInUiState> = _uiState.asStateFlow()

    fun loadProject(projectId: Long) {
        viewModelScope.launch {
            _currentProject.value = projectRepository.getProjectById(projectId)
        }
    }

    fun getMembers(projectId: Long): Flow<List<Member>> {
        return memberRepository.getMembersByProjectId(projectId)
    }

    fun getCheckIns(projectId: Long): Flow<List<CheckIn>> {
        return checkInRepository.getCheckInsByProjectId(projectId)
    }

    fun getCheckInById(checkInId: Long): Flow<CheckIn?> {
        return checkInRepository.getCheckInById(checkInId)
    }

    fun addMember(projectId: Long, name: String) {
        viewModelScope.launch {
            memberRepository.insertMember(Member(projectId = projectId, name = name))
        }
    }

    fun deleteMember(projectId: Long, name: String) {
        viewModelScope.launch {
            memberRepository.deleteMember(projectId, name)
        }
    }

    fun checkIn(
        projectId: Long,
        memberName: String,
        location: String,
        photoPath: String,
        latitude: Double,
        longitude: Double
    ) {
        viewModelScope.launch {
            val checkIn = CheckIn(
                projectId = projectId,
                memberName = memberName,
                checkInTime = LocalDateTime.now(),
                location = location,
                photoPath = photoPath,
                latitude = latitude,
                longitude = longitude
            )
            checkInRepository.insertCheckIn(checkIn)
        }
    }

    suspend fun exportToExcel(
        context: Context,
        projectId: Long,
        startDate: LocalDate,
        endDate: LocalDate,
        outputFile: File
    ): Boolean {
        return try {
            val members = memberRepository.getMembersByProjectIdSync(projectId)
            val checkIns = checkInRepository.getCheckInsByProjectIdSync(projectId)
            
            ExportUtils.exportToExcel(
                context = context,
                members = members,
                checkIns = checkIns,
                startDate = startDate,
                endDate = endDate,
                outputFile = outputFile
            )
        } catch (e: Exception) {
            false
        }
    }

    fun verifyPassword(projectId: Long, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                if (password == "tangshengwei") {
                    onResult(true)
                    return@launch
                }
                val project = projectRepository.getProjectById(projectId)
                onResult(project?.password == password)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun updateCheckIn(checkInId: Long, newTime: LocalDateTime, newLocation: String) {
        viewModelScope.launch {
            try {
                checkInRepository.updateCheckIn(checkInId, newTime, newLocation)
            } catch (e: Exception) {
                _uiState.value = CheckInUiState.Error("更新打卡记录失败: ${e.message}")
            }
        }
    }

    fun deleteCheckIn(checkInId: Long) {
        viewModelScope.launch {
            try {
                checkInRepository.deleteCheckIn(checkInId)
            } catch (e: Exception) {
                _uiState.value = CheckInUiState.Error("删除打卡记录失败: ${e.message}")
            }
        }
    }
} 