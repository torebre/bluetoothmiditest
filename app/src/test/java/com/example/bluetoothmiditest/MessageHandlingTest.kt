package com.example.bluetoothmiditest

import com.example.bluetoothmiditest.MidiMessageTestData.testNoteOnMessage
import com.example.bluetoothmiditest.MidiMessageTestData.testNoteOffMessage
import org.junit.Before
import org.junit.Test
import timber.log.Timber
import org.junit.Assert.assertEquals

class MessageHandlingTest {

    @Before
    fun setup() {
        Timber.plant(TimberSystemOutTree())
    }

    @Test
    fun messageTranslationNoteOnTest() {
        val dataMemoryStore = DataMemoryStore()
        val midiMessageHandler = MidiMessageHandlerImpl(dataMemoryStore, true)
        val midiMessageTranslator = MidiMessageTranslator(midiMessageHandler)

        midiMessageTranslator.onSend(
            testNoteOnMessage.data,
            testNoteOnMessage.offset,
            testNoteOnMessage.count,
            testNoteOnMessage.timestamp
        )
        val session = dataMemoryStore.getData()

        assertEquals(1, session.midiMessages.size)
        session.midiMessages[0].let {
            assertEquals(
                ChannelCommandName.NoteOn.name,
                it.messageType
            )
            assertEquals("72, 94", it.midiData)
        }
    }

    @Test
    fun messageTranslationNoteOffTest() {
        val dataMemoryStore = DataMemoryStore()
        val midiMessageHandler = MidiMessageHandlerImpl(dataMemoryStore, true)
        val midiMessageTranslator = MidiMessageTranslator(midiMessageHandler)

        midiMessageTranslator.onSend(
            testNoteOffMessage.data,
            testNoteOffMessage.offset,
            testNoteOffMessage.count,
            testNoteOffMessage.timestamp
        )

        val session = dataMemoryStore.getData()

        assertEquals(1, session.midiMessages.size)
        session.midiMessages[0].let {
            assertEquals(
                ChannelCommandName.NoteOff.name,
                it.messageType
            )
            assertEquals("72, 93", it.midiData)
        }
    }

}