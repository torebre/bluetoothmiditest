package com.example.bluetoothmiditest.storage

import java.io.Closeable

interface MidiMessageListener: Closeable {

    fun store(midiMessage: MidiMessage)

}