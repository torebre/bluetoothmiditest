package com.example.bluetoothmiditest

@ExperimentalUnsignedTypes
internal class DummyMidiReceiver : MidiMessageHandler {
    val receivedMessages = mutableListOf<MidiMessage>()

    override fun store(store: Boolean) {
        // Do nothing
    }

    override fun isStoring(): Boolean {
        return false
    }

    override fun send(msg: UByteArray, offset: Int, count: Int, timestamp: Long) {
        receivedMessages.add(MidiMessage(msg, offset, count, timestamp))
    }

    override fun close() {
        // Do nothing
    }

}