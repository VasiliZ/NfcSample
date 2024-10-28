package com.example.nfcsample.utils

import android.app.Activity
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable

fun <T> enableNFCInForeground(nfcAdapter: NfcAdapter, activity: Activity, classType: Class<T>) {
    val pendingIntent = PendingIntent.getActivity(
        activity, 0,
        Intent(activity, classType).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), FLAG_MUTABLE
    )

    val techLists =
        arrayOf(arrayOf(Ndef::class.java.name), arrayOf(NdefFormatable::class.java.name))

    nfcAdapter.enableForegroundDispatch(
        activity, pendingIntent, arrayOf(
            IntentFilter(
                NfcAdapter.ACTION_NDEF_DISCOVERED
            ).apply {
                addDataType(NfcUtilsConst.NfcDataType)
            },
            IntentFilter(
                NfcAdapter.ACTION_TECH_DISCOVERED
            )
        ), techLists
    )
}

fun disableNFCInForeground(nfcAdapter: NfcAdapter, activity: Activity) {
    nfcAdapter.disableForegroundDispatch(activity)
}