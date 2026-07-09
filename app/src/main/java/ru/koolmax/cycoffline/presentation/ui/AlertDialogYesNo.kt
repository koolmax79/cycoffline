package ru.koolmax.cycoffline.presentation.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun AlertDialogYesNo(
    dialogText: String,
    onYes: () -> Unit = { },
    onNo: () -> Unit = { },
    onClose: () -> Unit = { },
) {
    AlertDialog(
        text = {
            Text(text = dialogText)
        },
        onDismissRequest = {
            onNo()
            onClose()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onYes()
                    onClose()
                }
            ) {
                Text("Yes")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onNo()
                    onClose()
                }
            ) {
                Text("No")
            }
        }
    )
}