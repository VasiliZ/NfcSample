package com.example.nfcsample.main.data

import android.content.Intent

interface TokenWriter {

   suspend fun writeToken(
      intent: Intent,
      shouldClearData: Boolean,
      nfcTokenData: List<NfcModelPresentation>
   )
}