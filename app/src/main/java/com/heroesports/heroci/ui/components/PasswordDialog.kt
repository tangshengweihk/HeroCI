package com.heroesports.heroci.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (password: String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("请输入管理密码") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
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
                if (isError) {
                    Text(
                        text = "请输入密码",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (password.isBlank()) {
                        isError = true
                        return@Button
                    }
                    onConfirm(password)
                }
            ) {
                Text("确认")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
} 