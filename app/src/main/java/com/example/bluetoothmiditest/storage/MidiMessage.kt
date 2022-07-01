package com.example.bluetoothmiditest.storage

import com.example.bluetoothmiditest.MidiCommand
import java.io.Serializable

data class MidiMessage(
    val midiCommand: MidiCommand,
    val midiData: String,
    val channel: Int?,
    val timestamp: Long
): Serializable
