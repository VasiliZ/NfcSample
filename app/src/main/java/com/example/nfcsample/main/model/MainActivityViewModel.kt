package com.example.nfcsample.main.model

import android.content.Intent
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nfcsample.main.data.NfcModelPresentation
import com.example.nfcsample.main.data.NfcRearedScreenState
import com.example.nfcsample.main.data.NfcTokenReader
import com.example.nfcsample.main.data.NfcTokenWriter
import com.example.nfcsample.main.data.SnackBarMessageType
import com.example.nfcsample.main.data.TokenReader
import com.example.nfcsample.main.data.TokenWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivityViewModel : ViewModel() {
    private val tokenReader: TokenReader = NfcTokenReader()
    private val tokenWriter: TokenWriter = NfcTokenWriter(
        tokenReader,
        onClearData = { nfcTokenData.clear() },
        onSetScreenBarState = { state ->
            screenState = state
        },
        onDismissClearedState = { shouldClearData = it },
        onSetSnackBarState = { snackBarType = it },
        onAddOldData = {
            setNewNfcDataList(it)
        }
    )

    var nfcTokenData: SnapshotStateList<NfcModelPresentation> = SnapshotStateList()
    var screenState by mutableStateOf(NfcRearedScreenState.IDLE)
        private set
    var isBottomSheetVisible by mutableStateOf(false)
        private set
    var bottomSheetText by mutableStateOf("")
        private set

    private var shouldClearData: Boolean = false
    var shouldShowAlertDialog: Boolean by mutableStateOf(false)
        private set
    val snackBarHostState = SnackbarHostState()
    var snackBarType by mutableStateOf(SnackBarMessageType.NONE)

    private fun readIntentFromToken(intent: Intent) {
        nfcTokenData.clear()
        viewModelScope.launch(Dispatchers.IO) {

            val messages = tokenReader.read(intent)
            withContext(Dispatchers.Main) {
                if (messages.isEmpty()) {
                    screenState = NfcRearedScreenState.EMPTY
                } else {
                    prepareTagData(messages)
                    screenState = NfcRearedScreenState.DATA
                }
            }
        }
    }

    private fun prepareTagData(messages: List<String>) {
        messages.forEach {
            nfcTokenData.add(
                NfcModelPresentation(
                    text = it
                )
            )
        }
    }

    private fun onWriteToTag(intent: Intent) {
        viewModelScope.launch(Dispatchers.IO) {
            tokenWriter.writeToken(intent, shouldClearData, nfcTokenData)
        }
    }

    fun onFabButtonClicked() {
        isBottomSheetVisible = true
    }

    fun onHideBottomSheet() {
        hideBottomSheet()
        clearBottomSheet()
    }

    private fun hideBottomSheet() {
        isBottomSheetVisible = false
    }

    private fun clearBottomSheet() {
        bottomSheetText = ""
    }

    private fun setNewNfcDataList(nfcModelPresentations: List<NfcModelPresentation>) {
        val resultList = if (nfcTokenData.none {
                !it.isPending.value
            }) {
            nfcTokenData + nfcModelPresentations
        } else {
            nfcTokenData - nfcModelPresentations
        }
        nfcTokenData.clear()

        nfcTokenData.addAll(resultList.map {
            NfcModelPresentation(
                it.text,
                mutableStateOf(false)
            )
        })
    }

    fun onTextChanged(textValue: String) {
        bottomSheetText = textValue
    }

    fun onApplyTextClicked() {
        screenState = NfcRearedScreenState.DATA
        nfcTokenData.add(
            NfcModelPresentation(
                bottomSheetText,
                mutableStateOf(true)
            )
        )
        hideBottomSheet()
        clearBottomSheet()
    }

    fun receiveIntentData(intent: Intent) {

        when {
            nfcTokenData.any { it.isPending.value } -> {
                onWriteToTag(intent)
            }

            shouldClearData -> {
                viewModelScope.launch(Dispatchers.IO) {
                    onWriteToTag(intent)
                }
            }

            else -> {
                readIntentFromToken(intent)
            }
        }
    }

    fun onClearIconClicked() {
        shouldShowAlertDialog = true
    }

    fun onDismissAlertDialog() {
        hideDialog()
    }

    fun onClearToken() {
        screenState = NfcRearedScreenState.CLEAR
        hideDialog()
        shouldClearData = true
    }

    private fun hideDialog() {
        shouldShowAlertDialog = false
    }

    fun onDeletePendingItem(item: NfcModelPresentation) {
        nfcTokenData.remove(item)
    }

    fun onClearSnackBarMessageType() {
        snackBarType = SnackBarMessageType.NONE
    }
}