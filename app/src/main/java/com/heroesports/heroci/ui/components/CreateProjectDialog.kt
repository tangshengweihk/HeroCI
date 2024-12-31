package com.heroesports.heroci.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CreateProjectDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, password: String) -> Unit
) {
    var projectName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("创建新项目") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = projectName,
                    onValueChange = { 
                        projectName = it
                        isError = false
                    },
                    label = { Text("项目名称") },
                    isError = isError,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { 
                        password = it
                        isError = false
                    },
                    label = { Text("管理密码") },
                    isError = isError,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (projectName.isBlank() || password.isBlank()) {
                        isError = true
                        return@Button
                    }
                    onConfirm(projectName, password)
                }
            ) {
                Text("创建")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
} 