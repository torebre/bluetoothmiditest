package com.example.bluetoothmiditest

import android.media.midi.MidiReceiver
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Matchers
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.runners.MockitoJUnitRunner


@RunWith(
    MockitoJUnitRunner::class
)
class MidiMessageTranslatorTest {

    @Mock
    private lateinit var midiReceiver: MidiReceiver


    @Test
    fun testNoteOn() {
        val receivedMessages = mutableListOf<ByteArray>()
        doAnswer { invocation ->
            val msg = invocation.getArgumentAt(0, ByteArray::class.java)
            receivedMessages.add(msg)
        }.`when` (midiReceiver).onSend(
            Matchers.any(),
            Matchers.anyInt(),
            Matchers.anyInt(),
            Matchers.anyLong()
        )

        val messageTranslator = MidiMessageTranslator(midiReceiver)

        val data = byteArrayOf(0x90.toByte(), 0x45, 0x32)

        messageTranslator.processMessage(data, 0, data.size, 0)

        println("Received messages: ${receivedMessages}")

    }


}