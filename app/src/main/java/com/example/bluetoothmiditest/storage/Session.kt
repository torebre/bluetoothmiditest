package com.example.bluetoothmiditest.storage

import java.io.Serializable


data class Session(val midiMessages: MutableList<MidiMessage> = mutableListOf()) : Serializable
