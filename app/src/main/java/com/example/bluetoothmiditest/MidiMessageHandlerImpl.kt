package com.example.bluetoothmiditest

import android.util.Log


class MidiMessageHandlerImpl: MidiMessageHandler {

    override fun onSend(msg: ByteArray?, offset: Int, count: Int, timestamp: Long) {
        // TODO
        Log.i("MessageHandler", "Got message ${msg}")
    }


}