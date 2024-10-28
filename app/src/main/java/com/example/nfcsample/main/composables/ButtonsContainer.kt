package com.example.nfcsample.main.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.nfcsample.R

@Composable
fun ButtonContainer(
    innerPadding: PaddingValues,
    onReadDataButtonClicked: () -> Unit,
    onWriteDataButtonClicked: () -> Unit,
    onClearDataButtonClicked: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.Center)
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth(),
                onClick = onReadDataButtonClicked
            ) {
                Text(stringResource(R.string.read_data_label))
            }
            Button(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth(),
                onClick = onWriteDataButtonClicked
            ) {
                Text(stringResource(R.string.write_data_label))
            }
            Button(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth(),
                onClick = onClearDataButtonClicked
            ) {
                Text(stringResource(R.string.remove_data_label))
            }
        }
    }
}