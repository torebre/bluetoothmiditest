package com.example.bluetoothmiditest

import com.example.bluetoothmiditest.storage.MidiMessage
import com.example.bluetoothmiditest.storage.Session


class DataMemoryStore(session: Session? = null) : DataStore {

    private val session: Session = session ?: Session()

    override fun store(midiMessage: MidiMessage) {
        session.midiMessages.add(midiMessage)
    }


    override fun getData() = session


    override fun close() {
        // Nothing to close
    }


}