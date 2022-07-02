package com.example.bluetoothmiditest

import com.example.bluetoothmiditest.MidiMessageTestData.testNoteOnMessage
import com.example.bluetoothmiditest.MidiMessageTestData.testNoteOffMessage
import com.example.bluetoothmiditest.midi.MidiMessageTranslator
import com.example.bluetoothmiditest.storage.DataMemoryStore
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
        session.midiMessages[0].let { midiMessage ->
            assertEquals(
                MidiCommand.NoteOn,
                midiMessage.midiCommand
            )
            assertEquals("72, 94", midiMessage.midiData)
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
        session.midiMessages[0].let { midiMessage ->
            assertEquals(
                MidiCommand.NoteOff,
                midiMessage.midiCommand
            )
            assertEquals("72, 93", midiMessage.midiData)
        }
    }

}