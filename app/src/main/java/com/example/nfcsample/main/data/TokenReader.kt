package com.example.nfcsample.main.data

import android.content.Intent
import android.nfc.NdefMessage

interface TokenReader {

    fun read(intent: Intent): List<String>

    fun getTokenMessageList(intent: Intent): Array<NdefMessage>?

    fun readNfcTokenMessages(tokenMessageList: Array<NdefMessage>?): MutableList<String>

}