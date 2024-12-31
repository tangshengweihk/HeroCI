package com.heroesports.heroci.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.heroesports.heroci.data.entity.Member
import com.heroesports.heroci.data.entity.Project

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectManageScreen(
    project: Project,
    members: List<Member>,
    onAddMember: (String) -> Unit,
    onDeleteMember: (Member) -> Unit,
    onDeleteProject: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${project.name} - 管理") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "添加人员")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text(
                    text = "人员列表",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(members) { member ->
                        ListItem(
                            headlineContent = { Text(member.name) },
                            trailingContent = {
                                IconButton(onClick = { onDeleteMember(member) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "删除")
                                }
                            }
                        )
                        Divider()
                    }
                }

                // 删除项目按钮
                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("删除项目")
                }
            }
        }

        if (showAddDialog) {
            var name by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("添加人员") },
                text = {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("姓名") },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onAddMember(name)
                                showAddDialog = false
                            }
                        }
                    ) {
                        Text("添加")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("取消")
                    }
                }
            )
        }

        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("删除项目") },
                text = { Text("确定要删除该项目吗？此操作不可恢复。") },
                confirmButton = {
                    Button(
                        onClick = {
                            onDeleteProject()
                            showDeleteConfirm = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("删除")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) {
                        Text("取消")
                    }
                }
            )
        }
    }
} 