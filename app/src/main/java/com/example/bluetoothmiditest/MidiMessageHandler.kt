package com.example.bluetoothmiditest

interface MidiMessageHandler {

    fun onSend(msg: ByteArray?, offset: Int, count: Int, timestamp: Long)


}