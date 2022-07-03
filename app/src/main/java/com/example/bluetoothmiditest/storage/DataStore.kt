package com.example.bluetoothmiditest.storage

import java.io.Closeable

interface DataStore: Closeable {


    fun store(midiMessage: MidiMessage)

    fun getData(): Session

}