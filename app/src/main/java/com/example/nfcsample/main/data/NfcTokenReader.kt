package com.example.nfcsample.main.data

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.os.Build

class NfcTokenReader : TokenReader {

    override fun read(intent: Intent): List<String> {

        val tokenMessageList = getTokenMessageList(intent)
        return readNfcTokenMessages(tokenMessageList)
    }

    override fun getTokenMessageList(intent: Intent): Array<NdefMessage>? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES, NdefMessage::class.java)
        } else {
            intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES) as? Array<NdefMessage>
        }
    }

    override fun readNfcTokenMessages(tokenMessageList: Array<NdefMessage>?): MutableList<String> {
        val savedMessages = mutableListOf<String>()
        tokenMessageList?.forEach {
            (it as? NdefMessage)?.records?.filter {
                it.tnf != NdefRecord.TNF_EMPTY
            }?.forEachIndexed { index, record ->
                savedMessages.add(String(record.payload))
            }
        }

        return savedMessages
    }

}