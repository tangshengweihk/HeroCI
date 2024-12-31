package com.heroesports.heroci.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

@Composable
fun CommonDialog(
    title: String,
    content: @Composable () -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmText: String = "确定",
    dismissText: String = "取消"
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = content,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = dismissText)
            }
        }
    )
} 