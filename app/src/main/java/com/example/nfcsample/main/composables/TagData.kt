package com.example.nfcsample.main.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.nfcsample.R
import com.example.nfcsample.main.data.NfcModelPresentation


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TagData(
    dataList: List<NfcModelPresentation>,
    modifier: Modifier = Modifier,
    onDeletePendingItem: (NfcModelPresentation) -> Unit
) {
    Column {
        if (dataList.filter {
                !it.isPending.value
            }.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Text(
                    text = stringResource(R.string.tag_data_label)
                )

                Spacer(Modifier.padding(8.dp))

                dataList.filter {
                    !it.isPending.value
                }.forEach {
                    Text(
                        text = it.text,
                        modifier = modifier
                            .height(30.dp)
                            .fillMaxWidth(),
                        color = Color.Black
                    )
                }
            }
        }

        if (dataList.filter {
                it.isPending.value
            }.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Text(
                    text = stringResource(R.string.pending_tag_data_label)
                )

                Spacer(Modifier.padding(8.dp))

                dataList.filter {
                    it.isPending.value
                }.forEach {
                    Text(
                        text = it.text,
                        modifier = modifier
                            .height(30.dp)
                            .fillMaxWidth()
                            .combinedClickable(
                                onLongClick = {
                                    onDeletePendingItem(it)
                                },
                                onClick = {}
                            ),
                        color = Color.Gray
                    )
                }
            }
        }
    }
}