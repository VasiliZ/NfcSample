package com.example.nfcsample.main.composables

import androidx.compose.foundation.clickable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import com.example.nfcsample.R

@Composable
fun NfcTagAlertDialog(
    onDismissRequest: () -> Unit,
    onPositiveButtonClicked: () -> Unit,
    onNegativeButtonClicked: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(stringResource(R.string.dialog_clear_data_title))
        },
        confirmButton = {
            Text(
                stringResource(R.string.dialog_positive_button_label),
                modifier = Modifier.clickable { onPositiveButtonClicked() })
        },
        dismissButton = {
            Text(
                stringResource(R.string.dialog_negative_button_label),
                modifier = Modifier.clickable { onNegativeButtonClicked() })
        },
        properties = DialogProperties(
            dismissOnClickOutside = false,
            dismissOnBackPress = false
        ),
        containerColor = Color.White
    )
}