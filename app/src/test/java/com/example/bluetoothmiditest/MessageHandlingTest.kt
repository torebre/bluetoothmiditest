package com.example.bluetoothmiditest

import org.junit.Test
import timber.log.Timber

class MessageHandlingTest {

    @Test
    fun messageTranslationTest() {
        Timber.plant(TimberSystemOutTree())

        val dataMemoryStore = DataMemoryStore()
        val midiMessageHandler = MidiMessageHandlerImpl(dataMemoryStore, true)
        val midiMessageTranslator = MidiMessageTranslator(midiMessageHandler)

        midiMessageTranslator.onSend(MidiMessageTestData.testMessage.data, MidiMessageTestData.testMessage.offset, MidiMessageTestData.testMessage.count, MidiMessageTestData.testMessage.timestamp)

        val session = dataMemoryStore.getData()

        println(session.toString())
    }

}