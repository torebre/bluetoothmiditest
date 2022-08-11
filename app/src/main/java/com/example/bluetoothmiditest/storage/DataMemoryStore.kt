package com.example.bluetoothmiditest.storage


class DataMemoryStore(session: Session? = null) : MidiMessageListener {

    private val session: Session = session ?: Session()

    override fun store(midiMessage: MidiMessage) {
        session.midiMessages.add(midiMessage)
    }


    fun getData() = session


    override fun close() {
        // Nothing to close
    }


}