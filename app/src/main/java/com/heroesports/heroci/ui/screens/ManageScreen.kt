package com.heroesports.heroci.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import com.heroesports.heroci.ui.viewmodel.CheckInViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageScreen(
    projectId: Long,
    viewModel: CheckInViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToCheckInDetail: (Long, Boolean) -> Unit,
    isAdmin: Boolean = false
) {
    val members by viewModel.getMembers(projectId).collectAsState(initial = emptyList())
    val checkIns by viewModel.getCheckIns(projectId).collectAsState(initial = emptyList())
    val project by viewModel.currentProject.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDateRangeDialog by remember { mutableStateOf(false) }
    var memberToDelete by remember { mutableStateOf<String?>(null) }
    var newMemberName by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf(LocalDate.now().minusDays(30)) }
    var endDate by remember { mutableStateOf(LocalDate.now()) }
    var startDateSliderValue by remember { mutableStateOf(30f / 90f) }
    var endDateSliderValue by remember { mutableStateOf(1f) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 添加折叠状态管理
    var expandedMembers by remember { mutableStateOf(mutableSetOf<String>()) }

    // 创建文件选择器
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri ->
        uri?.let { documentUri ->
            scope.launch {
                try {
                    val outputFile = File(
                        context.getExternalFilesDir(null),
                        "打卡记录_${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))}.xlsx"
                    )
                    
                    val success = viewModel.exportToExcel(
                        context = context,
                        projectId = projectId,
                        startDate = startDate,
                        endDate = endDate,
                        outputFile = outputFile
                    )
                    if (success) {
                        // 复制到用户选择的位置
                        context.contentResolver.openOutputStream(documentUri)?.use { outputStream ->
                            outputFile.inputStream().use { inputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }
                        Toast.makeText(context, "导出成功", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "导出失败", Toast.LENGTH_SHORT).show()
                    }
                    outputFile.delete()
                } catch (e: Exception) {
                    Toast.makeText(context, "导出失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(projectId) {
        viewModel.loadProject(projectId)
    }

    if (showDateRangeDialog) {
        AlertDialog(
            onDismissRequest = { showDateRangeDialog = false },
            title = { Text("选择导出日期范围") },
            text = {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("开始日期: ${startDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}")
                    Slider(
                        value = startDateSliderValue,
                        onValueChange = { value ->
                            startDateSliderValue = value
                            val days = (value * 90).toInt() // 最多往前90天
                            startDate = LocalDate.now().minusDays(days.toLong())
                        },
                        valueRange = 0f..1f
                    )
                    Text("结束日期: ${endDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}")
                    Slider(
                        value = endDateSliderValue,
                        onValueChange = { value ->
                            endDateSliderValue = value
                            val days = ((1 - value) * 90).toInt() // 最多往前90天
                            endDate = LocalDate.now().minusDays(days.toLong())
                        },
                        valueRange = 0f..1f
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDateRangeDialog = false
                        val timestamp = LocalDateTime.now().format(
                            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
                        )
                        val fileName = "打卡记录_${project?.name}_$timestamp.xlsx"
                        createDocumentLauncher.launch(fileName)
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDateRangeDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = project?.name ?: "",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "返回",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    // 导出按钮
                    FilledTonalButton(
                        onClick = { showDateRangeDialog = true },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = "选择日期",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "导出",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    FilledTonalButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "添加",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "添加人员",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            items(members) { member ->
                val memberCheckIns = checkIns
                    .filter { it.memberName == member.name }
                    .sortedByDescending { it.checkInTime }
                val isExpanded = expandedMembers.contains(member.name)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // 成员信息头部（可点击展开/折叠）
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    expandedMembers = if (isExpanded) {
                                        expandedMembers.minus(member.name).toMutableSet()
                                    } else {
                                        expandedMembers.plus(member.name).toMutableSet()
                                    }
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = member.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                // 显示打卡记录数量
                                Text(
                                    text = "(${memberCheckIns.size})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (isExpanded) "折叠" else "展开",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = {
                                    memberToDelete = member.name
                                    showDeleteDialog = true
                                }
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "删除",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        
                        // 只在展开状态显示打卡记录
                        if (isExpanded) {
                            if (memberCheckIns.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                memberCheckIns.forEach { checkIn ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable {
                                                onNavigateToCheckInDetail(checkIn.id, isAdmin)
                                            },
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                        ),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp)
                                        ) {
                                            Text(
                                                text = checkIn.checkInTime.format(
                                                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                                                ),
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "位置：${checkIn.location}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "暂无打卡记录",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("添加人员") },
                text = {
                    OutlinedTextField(
                        value = newMemberName,
                        onValueChange = { newMemberName = it },
                        label = { Text("姓名") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newMemberName.isNotBlank()) {
                                viewModel.addMember(projectId, newMemberName)
                                newMemberName = ""
                                showAddDialog = false
                            }
                        }
                    ) {
                        Text("确定")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("取消")
                    }
                }
            )
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("删除确认") },
                text = { Text("确定要删除该人员吗？") },
                confirmButton = {
                    Button(
                        onClick = {
                            memberToDelete?.let { name ->
                                viewModel.deleteMember(projectId, name)
                            }
                            showDeleteDialog = false
                        }
                    ) {
                        Text("确定")
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

private fun createTempFile(context: Context): File {
    return File(context.cacheDir, "temp_export.xlsx").apply {
        if (exists()) {
            delete()
        }
    }
} 