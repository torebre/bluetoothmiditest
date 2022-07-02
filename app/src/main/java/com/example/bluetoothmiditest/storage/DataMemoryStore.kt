package com.example.bluetoothmiditest.storage


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