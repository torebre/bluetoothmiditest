package com.example.bluetoothmiditest

import com.example.bluetoothmiditest.storage.MidiMessage
import com.example.bluetoothmiditest.storage.Session
import java.io.Closeable

interface DataStore: Closeable {


    fun store(midiMessage: MidiMessage)

    fun getData(): Session

}