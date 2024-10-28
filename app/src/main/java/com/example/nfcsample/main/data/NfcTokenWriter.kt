package com.example.nfcsample.main.data

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import com.example.nfcsample.utils.NfcUtilsConst
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class NfcTokenWriter(
    private val tokenReader: TokenReader,
    private val onClearData: () -> Unit,
    private val onDismissClearedState: (Boolean) -> Unit,
    private val onSetSnackBarState: (SnackBarMessageType) -> Unit,
    private val onSetScreenBarState: (NfcRearedScreenState) -> Unit,
    private val onAddOldData: (List<NfcModelPresentation>) -> Unit
) : TokenWriter {

    private val oldTagData: MutableList<NfcModelPresentation> = mutableListOf()

    override suspend fun writeToken(
        intent: Intent,
        shouldClearData: Boolean,
        nfcTokenData: List<NfcModelPresentation>
    ) {
        tokenReader.read(intent).map {
            oldTagData.add(NfcModelPresentation(it))
        }
        val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
        val formatableTag = NdefFormatable.get(tag)
        val newNdef = if (shouldClearData) listOf(
            NdefRecord(
                NdefRecord.TNF_EMPTY,
                null,
                null,
                null
            )
        ).toTypedArray() else createNdefRecords(nfcTokenData.filter { it.isPending.value }
            .map { it.text })

        formatableTag?.use {
            {
                it.connect()

                if (it.isConnected) {
                    tokenReader.getTokenMessageList(intent)

                    it.format(NdefMessage(newNdef))
                }
            }
        }
        writeMessage(newNdef, tag, shouldClearData, nfcTokenData)
    }

    private suspend fun writeMessage(
        message: Array<NdefRecord>,
        tag: Tag? = null,
        shouldClearData: Boolean,
        nfcTokenData: List<NfcModelPresentation>,
    ) {
        val ndefTag = Ndef.get(tag)

        ndefTag?.use {

            try {
                if (!it.isConnected) {
                    it.connect()
                }

                if (!it.isWritable) {
                    return
                }

                val ndefRecordList = ndefTag.ndefMessage?.records
                val messages = NdefMessage(
                    when {

                        shouldClearData -> {
                            message
                        }

                        ndefTag.ndefMessage == null -> {
                            message
                        }

                        else -> {
                            ndefRecordList?.filter { it.tnf != NdefRecord.TNF_EMPTY }
                                ?.toMutableList()
                                ?.apply { addAll(message) }?.toTypedArray()
                        }
                    }
                )

                if (ndefTag.maxSize < messages.byteArrayLength) {
                    onSetSnackBarState(SnackBarMessageType.TOO_MUCH_DATA)
                    return
                }

                if (it.isWritable) {
                    try {
                        it.writeNdefMessage(messages)
                    } catch (e: IOException) {
                        onSetSnackBarState(SnackBarMessageType.ERROR)
                        onSetScreenBarState(NfcRearedScreenState.DATA)
                        onDismissClearedState(false)
                        return
                    }

                    withContext(Dispatchers.Main) {

                        when {
                            shouldClearData -> {
                                onClearData()
                                onSetScreenBarState(NfcRearedScreenState.EMPTY)
                                onSetSnackBarState(SnackBarMessageType.CLEARED)
                                onDismissClearedState(false)
                            }

                            else -> {
                                onSetSnackBarState(SnackBarMessageType.WROTE)
                                onAddOldData(oldTagData)
                                oldTagData.clear()
                            }
                        }
                    }
                } else {
                    onSetSnackBarState(SnackBarMessageType.NOT_WRITABLE)
                }
            } catch (e: IOException) {
                onSetSnackBarState(SnackBarMessageType.NOT_CONNECTED)
            }
        }
    }

    private fun createNdefRecords(messages: List<String>): Array<NdefRecord> {
        return messages.map { NdefRecord.createMime(NfcUtilsConst.NfcDataType, it.toByteArray()) }
            .toTypedArray()
    }
}
