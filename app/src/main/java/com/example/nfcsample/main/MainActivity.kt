package com.example.nfcsample.main

import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.SecureFlagPolicy
import com.example.nfcsample.R
import com.example.nfcsample.main.composables.ClearTagScreenState
import com.example.nfcsample.main.composables.EmptyNfcDataScreenState
import com.example.nfcsample.main.composables.IdleReadTokenContainer
import com.example.nfcsample.main.composables.NfcTagAlertDialog
import com.example.nfcsample.main.composables.TagData
import com.example.nfcsample.main.data.NfcRearedScreenState
import com.example.nfcsample.main.data.SnackBarMessageType
import com.example.nfcsample.main.model.MainActivityViewModel
import com.example.nfcsample.utils.disableNFCInForeground
import com.example.nfcsample.utils.enableNFCInForeground
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    private var mNfcAdapter: NfcAdapter? = null

    private val viewModel by viewModels<MainActivityViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this)

        intent?.extras?.let {
            viewModel.receiveIntentData(intent)
        }

        setContent {
            val coroutineScope = rememberCoroutineScope()
            Box {
                Scaffold(
                    snackbarHost = {
                        SnackbarHost(viewModel.snackBarHostState)
                    },
                    topBar = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .background(
                                    color = Color.Cyan
                                ),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .clickable {
                                        viewModel.onClearIconClicked()
                                    },
                                contentDescription = null,
                                tint = Color.White,
                                painter = painterResource(R.drawable.delete_svgrepo_com),
                            )
                        }
                    },
                    content = { padding ->
                        Crossfade(viewModel.screenState, Modifier.padding(padding)) {
                            when (it) {
                                NfcRearedScreenState.DATA -> {
                                    TagData(viewModel.nfcTokenData) {
                                        viewModel.onDeletePendingItem(it)
                                    }
                                }

                                NfcRearedScreenState.IDLE -> {
                                    IdleReadTokenContainer()
                                }

                                NfcRearedScreenState.EMPTY -> {
                                    EmptyNfcDataScreenState()
                                }

                                NfcRearedScreenState.CLEAR -> {
                                    ClearTagScreenState()
                                }
                            }
                        }
                    }
                )

                if (viewModel.screenState != NfcRearedScreenState.CLEAR) {
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp, bottom = 60.dp)
                            .background(
                                color = Color.Cyan,
                                shape = CircleShape
                            )
                            .size(54.dp)
                            .clip(CircleShape)
                            .clickable {
                                viewModel.onFabButtonClicked()
                            }
                            .align(Alignment.BottomEnd)
                    ) {
                        Icon(
                            modifier = Modifier.align(Alignment.Center),
                            painter = painterResource(R.drawable.ic_add),
                            tint = Color.White,
                            contentDescription = null
                        )
                    }
                }
            }
            if (viewModel.isBottomSheetVisible) {
                ModalBottomSheet(
                    onDismissRequest = {
                        viewModel.onHideBottomSheet()
                    },
                    properties = ModalBottomSheetProperties(
                        shouldDismissOnBackPress = true,
                        securePolicy = SecureFlagPolicy.Inherit,
                        isFocusable = true
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .navigationBarsPadding()
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextField(
                            modifier = Modifier.weight(1f),
                            value = viewModel.bottomSheetText,
                            onValueChange = {
                                viewModel.onTextChanged(it)
                            }
                        )

                        Icon(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .align(Alignment.CenterVertically)
                                .clickable {
                                    viewModel.onApplyTextClicked()
                                },
                            painter = painterResource(R.drawable.ic_send),
                            tint = Color.Blue,
                            contentDescription = null
                        )
                    }
                }
            }

            if (viewModel.shouldShowAlertDialog) {
                NfcTagAlertDialog(
                    onDismissRequest = {
                        viewModel.onDismissAlertDialog()
                    },
                    onPositiveButtonClicked = {
                        viewModel.onClearToken()
                    },
                    onNegativeButtonClicked = {
                        viewModel.onDismissAlertDialog()
                    }
                )
            }

            val snackBarString = configureSnackBarDescription(viewModel.snackBarType)

            LaunchedEffect(viewModel.snackBarType) {
                if (viewModel.snackBarType != SnackBarMessageType.NONE) {
                    coroutineScope.launch(Dispatchers.Main) {
                        val snack = viewModel.snackBarHostState.showSnackbar(snackBarString)

                        if (snack == SnackbarResult.Dismissed) {
                            viewModel.onClearSnackBarMessageType()
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        mNfcAdapter?.let {
            enableNFCInForeground(it, this, javaClass)
        }
    }

    override fun onPause() {
        super.onPause()

        mNfcAdapter?.let {
            disableNFCInForeground(nfcAdapter = it, this)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        intent?.let {
            viewModel.receiveIntentData(intent)
        }
    }

    @Composable
    private fun configureSnackBarDescription(snackBarType: SnackBarMessageType): String {
        return when (snackBarType) {
            SnackBarMessageType.CLEARED -> stringResource(R.string.cleared_successfully)
            SnackBarMessageType.ERROR -> stringResource(R.string.error_writing_tag_data_label)
            SnackBarMessageType.NOT_WRITABLE -> stringResource(R.string.tag_not_writable_label)
            SnackBarMessageType.NOT_CONNECTED -> stringResource(R.string.tag_not_connected_label)
            SnackBarMessageType.WROTE -> stringResource(R.string.wrote_successfully)
            SnackBarMessageType.TOO_MUCH_DATA -> stringResource(R.string.tag_too_much_data_label)
            else -> String()
        }

    }
}