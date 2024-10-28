package com.example.nfcsample.main.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

data class NfcModelPresentation(
    val text: String,
    var isPending: MutableState<Boolean> = mutableStateOf(false)
)
