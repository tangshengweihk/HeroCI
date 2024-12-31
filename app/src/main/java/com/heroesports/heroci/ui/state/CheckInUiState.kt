package com.heroesports.heroci.ui.state

sealed class CheckInUiState {
    data object Loading : CheckInUiState()
    data class Error(val message: String) : CheckInUiState()
} 