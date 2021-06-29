package com.example.bluetoothmiditest.storage

import java.io.Serializable

data class MidiMessage(
    val messageType: String,
    val midiData: String,
    val channel: Int?,
    val timestamp: Long
): Serializable
