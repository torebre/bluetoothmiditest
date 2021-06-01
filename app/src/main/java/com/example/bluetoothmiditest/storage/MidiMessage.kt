package com.example.bluetoothmiditest.storage

data class MidiMessage(
    val messageType: String,
    val midiData: String,
    val channel: Int?,
    val timestamp: Long
)
