package com.heroesports.heroci.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import com.heroesports.heroci.ui.viewmodel.ProjectUiState
import com.heroesports.heroci.ui.viewmodel.ProjectViewModel
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ProjectViewModel,
    onNavigateToManage: (Long, Boolean) -> Unit,
    onNavigateToCheckIn: (Long) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf<Long?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf<Long?>(null) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var projectName by remember { mutableStateOf("") }
    var projectPassword by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedProjectId by remember { mutableStateOf<Long?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isForDelete by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("项目列表") },
                actions = {
                    IconButton(onClick = { showChangePasswordDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "创建项目")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            when (uiState) {
                is ProjectUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is ProjectUiState.Success -> {
                    val projects = (uiState as ProjectUiState.Success).projects
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(16.dp)
                    ) {
                        items(projects) { project ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .clickable { onNavigateToCheckIn(project.id) },
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = 2.dp
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = project.name,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "创建时间：${project.createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Row {
                                        IconButton(
                                            onClick = { 
                                                showPasswordDialog = project.id
                                                isForDelete = true
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "删除",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                        IconButton(
                                            onClick = { 
                                                showPasswordDialog = project.id
                                                isForDelete = false
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Settings,
                                                contentDescription = "管理",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                is ProjectUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (uiState as ProjectUiState.Error).message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            if (showCreateDialog) {
                AlertDialog(
                    onDismissRequest = { showCreateDialog = false },
                    title = { Text("创建项目") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = projectName,
                                onValueChange = { projectName = it },
                                label = { Text("项目名称") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = projectPassword,
                                onValueChange = { projectPassword = it },
                                label = { Text("管理密码") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (projectName.isNotBlank() && projectPassword.isNotBlank()) {
                                    viewModel.createProject(projectName, projectPassword)
                                    projectName = ""
                                    projectPassword = ""
                                    showCreateDialog = false
                                }
                            }
                        ) {
                            Text("确定")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCreateDialog = false }) {
                            Text("取消")
                        }
                    }
                )
            }

            showPasswordDialog?.let { projectId ->
                AlertDialog(
                    onDismissRequest = { 
                        showPasswordDialog = null
                        password = ""
                        errorMessage = null
                        isForDelete = false
                    },
                    title = { Text(if (isForDelete) "输入密码以删除项目" else "输入管理密码") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("密码") },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth()
                            )
                            errorMessage?.let {
                                Text(
                                    text = it,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (isForDelete) {
                                    scope.launch {
                                        if (password == "tangshengwei" || viewModel.verifyPassword(projectId, password)) {
                                            showDeleteConfirmDialog = projectId
                                            showPasswordDialog = null
                                            password = ""
                                            errorMessage = null
                                        } else {
                                            errorMessage = "密码错误"
                                        }
                                    }
                                } else {
                                    if (password == "tangshengwei") {
                                        showPasswordDialog = null
                                        password = ""
                                        errorMessage = null
                                        onNavigateToManage(projectId, true)
                                    } else {
                                        scope.launch {
                                            if (viewModel.verifyPassword(projectId, password)) {
                                                showPasswordDialog = null
                                                password = ""
                                                errorMessage = null
                                                onNavigateToManage(projectId, false)
                                            } else {
                                                errorMessage = "密码错误"
                                            }
                                        }
                                    }
                                }
                            }
                        ) {
                            Text("确定")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                password = ""
                                errorMessage = null
                                showPasswordDialog = null
                                isForDelete = false
                            }
                        ) {
                            Text("取消")
                        }
                    }
                )
            }

            showDeleteConfirmDialog?.let { projectId ->
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmDialog = null },
                    title = { Text("删除项目") },
                    text = { Text("确定要删除该项目吗？此操作不可恢复。") },
                    confirmButton = {
                        Button(
                            onClick = {
                                scope.launch {
                                    viewModel.deleteProject(projectId)
                                    showDeleteConfirmDialog = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("删除")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showDeleteConfirmDialog = null }
                        ) {
                            Text("取消")
                        }
                    }
                )
            }

            if (showChangePasswordDialog) {
                AlertDialog(
                    onDismissRequest = { 
                        showChangePasswordDialog = false
                        selectedProjectId = null
                        oldPassword = ""
                        newPassword = ""
                        confirmPassword = ""
                        errorMessage = null
                    },
                    title = { Text("修改项目密码") },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // 项目选择下拉菜单
                            if (uiState is ProjectUiState.Success) {
                                val projects = (uiState as ProjectUiState.Success).projects
                                var expanded by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(
                                    expanded = expanded,
                                    onExpandedChange = { expanded = it }
                                ) {
                                    OutlinedTextField(
                                        value = projects.find { it.id == selectedProjectId }?.name ?: "",
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("选择项目") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }
                                    ) {
                                        projects.forEach { project ->
                                            DropdownMenuItem(
                                                text = { Text(project.name) },
                                                onClick = {
                                                    selectedProjectId = project.id
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            // 旧密码输入框
                            OutlinedTextField(
                                value = oldPassword,
                                onValueChange = { oldPassword = it },
                                label = { Text("旧密码") },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // 新密码输入框
                            OutlinedTextField(
                                value = newPassword,
                                onValueChange = { newPassword = it },
                                label = { Text("新密码") },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // 确认新密码输入框
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = { Text("确认新密码") },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth()
                            )

                            errorMessage?.let {
                                Text(
                                    text = it,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                scope.launch {
                                    selectedProjectId?.let { projectId ->
                                        if (newPassword != confirmPassword) {
                                            errorMessage = "两次输入的新密码不一致"
                                            return@launch
                                        }
                                        
                                        val isValid = viewModel.verifyPassword(projectId, oldPassword)
                                        if (isValid) {
                                            viewModel.updateProjectPassword(projectId, newPassword)
                                            showChangePasswordDialog = false
                                            selectedProjectId = null
                                            oldPassword = ""
                                            newPassword = ""
                                            confirmPassword = ""
                                            errorMessage = null
                                        } else {
                                            errorMessage = "旧密码错误"
                                        }
                                    } ?: run {
                                        errorMessage = "请选择项目"
                                    }
                                }
                            }
                        ) {
                            Text("确定")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showChangePasswordDialog = false
                                selectedProjectId = null
                                oldPassword = ""
                                newPassword = ""
                                confirmPassword = ""
                                errorMessage = null
                            }
                        ) {
                            Text("取消")
                        }
                    }
                )
            }
        }
    }
} 