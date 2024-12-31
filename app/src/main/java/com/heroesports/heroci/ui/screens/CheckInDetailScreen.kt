package com.heroesports.heroci.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.heroesports.heroci.ui.viewmodel.CheckInViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInDetailScreen(
    projectId: Long,
    checkInId: Long,
    isAdmin: Boolean,
    viewModel: CheckInViewModel,
    onNavigateBack: () -> Unit
) {
    val checkIn by viewModel.getCheckInById(checkInId).collectAsState(initial = null)
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("打卡详情") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (isAdmin) {
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "编辑")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "删除")
                        }
                    }
                }
            )
        }
    ) { padding ->
        checkIn?.let { checkIn ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text(
                    text = checkIn.checkInTime.format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                    ),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "位置：${checkIn.location}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = Uri.parse(checkIn.photoPath),
                    contentDescription = "打卡照片",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }

        if (showEditDialog) {
            checkIn?.let { currentCheckIn ->
                var editedTime by remember { mutableStateOf(currentCheckIn.checkInTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))) }
                var editedLocation by remember { mutableStateOf(currentCheckIn.location) }
                var timeError by remember { mutableStateOf<String?>(null) }

                AlertDialog(
                    onDismissRequest = { showEditDialog = false },
                    title = { Text("编辑打卡记录") },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            OutlinedTextField(
                                value = editedTime,
                                onValueChange = { 
                                    editedTime = it
                                    timeError = null
                                },
                                label = { Text("打卡时间 (yyyy-MM-dd HH:mm)") },
                                isError = timeError != null,
                                modifier = Modifier.fillMaxWidth()
                            )
                            timeError?.let {
                                Text(
                                    text = it,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = editedLocation,
                                onValueChange = { editedLocation = it },
                                label = { Text("位置") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                try {
                                    val newDateTime = LocalDateTime.parse(editedTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                                    viewModel.updateCheckIn(
                                        checkInId = checkInId,
                                        newTime = newDateTime,
                                        newLocation = editedLocation
                                    )
                                    showEditDialog = false
                                } catch (e: Exception) {
                                    timeError = "时间格式错误"
                                }
                            }
                        ) {
                            Text("保存")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showEditDialog = false }) {
                            Text("取消")
                        }
                    }
                )
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("确认删除") },
                text = { Text("确定要删除这条打卡记录吗？") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteCheckIn(checkInId)
                            showDeleteDialog = false
                            onNavigateBack()
                        }
                    ) {
                        Text("删除")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("取消")
                    }
                }
            )
        }
    }
} 