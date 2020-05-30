package com.example.bluetoothmiditest

import android.media.midi.MidiReceiver
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Matchers
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.runners.MockitoJUnitRunner
import java.io.IOException
import java.util.*


/**
 * These are the tests from TestMidiFramer in the MidiBtlePairing project in android-midisuite, licensed other under the Apache License, Version 2.0
 */
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
        }.`when`(midiReceiver).send(
            Matchers.any(),
            Matchers.anyInt(),
            Matchers.anyInt(),
            Matchers.anyLong()
        )

        val messageTranslator = MidiMessageTranslator(midiReceiver)
        val data = byteArrayOf(0x90.toByte(), 0x45, 0x32)

        messageTranslator.processMessage(data, 0, data.size, 0)

        verify(midiReceiver).send(
            any(ByteArray::class.java),
            eq(0),
            eq(3),
            eq(0L)
        )

    }

    // Store a complete MIDI message.
    internal class MidiMessage(
        buffer: ByteArray?,
        offset: Int,
        length: Int,
        timestamp: Long
    ) {
        val data: ByteArray
        val timestamp: Long
        val timeReceived: Long

        constructor(buffer: ByteArray, timestamp: Long) : this(
            buffer,
            0,
            buffer.size,
            timestamp
        )

        // Check whether these two messages are the same.
        fun check(other: MidiMessage) {
            Assert.assertEquals(
                "data.length",
                data.size.toLong(),
                other.data.size.toLong()
            )
            Assert.assertEquals("data.timestamp", timestamp, other.timestamp)
            for (i in data.indices) {
                Assert.assertEquals("data[$i]", data[i], other.data[i])
            }
        }

        init {
            timeReceived = System.nanoTime()
            data = ByteArray(length)
            System.arraycopy(buffer, offset, data, 0, length)
            this.timestamp = timestamp
        }
    }

    // Store received messages in an array.
    internal class MyLoggingReceiver : MidiReceiver() {
        var messages = ArrayList<MidiMessage>()
        override fun onSend(
            data: ByteArray, offset: Int, count: Int,
            timestamp: Long
        ) {
            messages.add(MidiMessage(data, offset, count, timestamp))
        }
    }

    @Throws(IOException::class)
    private fun checkSequence(
        original: Array<MidiMessage>,
        expected: Array<MidiMessage>
    ) {


        val receivedMessages = mutableListOf<MidiMessage>()
        doAnswer { invocation ->
            receivedMessages.add(
                MidiMessage(
                    invocation.getArgumentAt(0, ByteArray::class.java),
                    invocation.getArgumentAt(1, Int::class.java),
                    invocation.getArgumentAt(2, Int::class.java),
                    invocation.getArgumentAt(3, Long::class.java)
                )
            )
        }.`when`(midiReceiver).send(
            Matchers.any(),
            Matchers.anyInt(),
            Matchers.anyInt(),
            Matchers.anyLong()
        )

        val framer = MidiMessageTranslator(midiReceiver)
        for (message in original) {
            framer.processMessage(
                message.data, 0, message.data.size,
                message.timestamp
            )
        }
        Assert.assertEquals(
            "command count", expected.size,
            receivedMessages.size
        )
        for (i in expected.indices) {
            expected[i].check(receivedMessages[i])
        }
    }

    @Throws(IOException::class)
    private fun checkSequence(
        original: Array<ByteArray>, expected: Array<ByteArray>,
        timestamp: Long
    ) {
        val originalMessages = original.map { MidiMessage(it, timestamp) }.toTypedArray()
        val expectedMessages = expected.map { MidiMessage(it, timestamp) }.toTypedArray()
        checkSequence(originalMessages, expectedMessages)
    }

    @Test
    @Throws(IOException::class)
    fun testSimpleSequence() {
        val timestamp = 8263518L
        val data = byteArrayOf(0x90.toByte(), 0x45, 0x32)
        val original: Array<MidiMessage> =
            arrayOf(
                MidiMessage(
                    data,
                    timestamp
                )
            )
        checkSequence(original, original)
    }


    // NoteOn then NoteOff using running status
    @Test
    @Throws(IOException::class)
    fun testRunningArrays() {
        val timestamp = 837518L
        val original =
            arrayOf(byteArrayOf(0x90.toByte(), 0x45, 0x32, 0x45, 0x00))
        val expected = arrayOf(
            byteArrayOf(0x90.toByte(), 0x45, 0x32),
            byteArrayOf(0x90.toByte(), 0x45, 0x00)
        )
        checkSequence(original, expected, timestamp)
    }

    // Start with unresolved running status that should be ignored
    @Test
    @Throws(IOException::class)
    fun testStartMiddle() {
        val timestamp = 837518L
        val original = arrayOf(
            byteArrayOf(
                0x23,
                0x34,
                0x90.toByte(),
                0x45,
                0x32,
                0x45,
                0x00
            )
        )
        val expected = arrayOf(
            byteArrayOf(0x90.toByte(), 0x45, 0x32),
            byteArrayOf(0x90.toByte(), 0x45, 0x00)
        )
        checkSequence(original, expected, timestamp)
    }

    @Test
    @Throws(IOException::class)
    fun testTwoOn() {
        val timestamp = 837518L
        val original =
            arrayOf(byteArrayOf(0x90.toByte(), 0x45, 0x32, 0x47, 0x63))
        val expected = arrayOf(
            byteArrayOf(0x90.toByte(), 0x45, 0x32),
            byteArrayOf(0x90.toByte(), 0x47, 0x63)
        )
        checkSequence(original, expected, timestamp)
    }

    @Test
    @Throws(IOException::class)
    fun testThreeOn() {
        val timestamp = 837518L
        val original = arrayOf(
            byteArrayOf(
                0x90.toByte(),
                0x45,
                0x32,
                0x47,
                0x63,
                0x49,
                0x23
            )
        )
        val expected = arrayOf(
            byteArrayOf(0x90.toByte(), 0x45, 0x32),
            byteArrayOf(0x90.toByte(), 0x47, 0x63),
            byteArrayOf(0x90.toByte(), 0x49, 0x23)
        )
        checkSequence(original, expected, timestamp)
    }

    // A RealTime message before a NoteOn
    @Test
    @Throws(IOException::class)
    fun testRealTimeBefore() {
        val timestamp = 8375918L
        val original = arrayOf(
            byteArrayOf(
                MidiConstants.STATUS_TIMING_CLOCK, 0x90.toByte(),
                0x45, 0x32
            )
        )
        val expected = arrayOf(
            byteArrayOf(MidiConstants.STATUS_TIMING_CLOCK),
            byteArrayOf(0x90.toByte(), 0x45, 0x32)
        )
        checkSequence(original, expected, timestamp)
    }

    // A RealTime message in the middle of a NoteOn
    @Test
    @Throws(IOException::class)
    fun testRealTimeMiddle1() {
        val timestamp = 8375918L
        val original = arrayOf(
            byteArrayOf(
                0x90.toByte(), MidiConstants.STATUS_TIMING_CLOCK,
                0x45, 0x32
            )
        )
        val expected = arrayOf(
            byteArrayOf(MidiConstants.STATUS_TIMING_CLOCK),
            byteArrayOf(0x90.toByte(), 0x45, 0x32)
        )
        checkSequence(original, expected, timestamp)
    }

    @Test
    @Throws(IOException::class)
    fun testRealTimeMiddle2() {
        val timestamp = 8375918L
        val original = arrayOf(
            byteArrayOf(
                0x90.toByte(), 0x45,
                MidiConstants.STATUS_TIMING_CLOCK, 0x32
            )
        )
        val expected = arrayOf(
            byteArrayOf(0xF8.toByte()),
            byteArrayOf(0x90.toByte(), 0x45, 0x32)
        )
        checkSequence(original, expected, timestamp)
    }

    // A RealTime message after a NoteOn
    @Test
    @Throws(IOException::class)
    fun testRealTimeAfter() {
        val timestamp = 8375918L
        val original = arrayOf(
            byteArrayOf(
                0x90.toByte(), 0x45, 0x32,
                MidiConstants.STATUS_TIMING_CLOCK
            )
        )
        val expected = arrayOf(
            byteArrayOf(0x90.toByte(), 0x45, 0x32),
            byteArrayOf(0xF8.toByte())
        )
        checkSequence(original, expected, timestamp)
    }

    // Break up running status across multiple messages
    @Test
    @Throws(IOException::class)
    fun testPieces() {
        val timestamp = 837518L
        val original = arrayOf(
            byteArrayOf(0x90.toByte(), 0x45),
            byteArrayOf(0x32, 0x47),
            byteArrayOf(0x63, 0x49, 0x23)
        )
        val expected = arrayOf(
            byteArrayOf(0x90.toByte(), 0x45, 0x32),
            byteArrayOf(0x90.toByte(), 0x47, 0x63),
            byteArrayOf(0x90.toByte(), 0x49, 0x23)
        )
        checkSequence(original, expected, timestamp)
    }

    // Break up running status into single byte messages
    @Test
    @Throws(IOException::class)
    fun testByByte() {
        val timestamp = 837518L
        val original = arrayOf(
            byteArrayOf(0x90.toByte()),
            byteArrayOf(0x45),
            byteArrayOf(0x32),
            byteArrayOf(0x47),
            byteArrayOf(0x63),
            byteArrayOf(0x49),
            byteArrayOf(0x23)
        )
        val expected = arrayOf(
            byteArrayOf(0x90.toByte(), 0x45, 0x32),
            byteArrayOf(0x90.toByte(), 0x47, 0x63),
            byteArrayOf(0x90.toByte(), 0x49, 0x23)
        )
        checkSequence(original, expected, timestamp)
    }

    @Test
    @Throws(IOException::class)
    fun testControlChange() {
        val timestamp = 837518L
        val original = arrayOf(
            byteArrayOf(
                MidiConstants.STATUS_CONTROL_CHANGE, 0x07, 0x52,
                0x0A, 0x63
            )
        )
        val expected = arrayOf(
            byteArrayOf(0xB0.toByte(), 0x07, 0x52),
            byteArrayOf(0xB0.toByte(), 0x0A, 0x63)
        )
        checkSequence(original, expected, timestamp)
    }

    @Test
    @Throws(IOException::class)
    fun testProgramChange() {
        val timestamp = 837518L
        val original =
            arrayOf(byteArrayOf(MidiConstants.STATUS_PROGRAM_CHANGE, 0x05, 0x07))
        val expected = arrayOf(
            byteArrayOf(0xC0.toByte(), 0x05),
            byteArrayOf(0xC0.toByte(), 0x07)
        )
        checkSequence(original, expected, timestamp)
    }

    // ProgramChanges, SysEx, ControlChanges
    @Test
    @Throws(IOException::class)
    fun testAck() {
        val timestamp = 837518L
        val original = arrayOf(
            byteArrayOf(
                MidiConstants.STATUS_PROGRAM_CHANGE, 0x05, 0x07,
                MidiConstants.STATUS_SYSTEM_EXCLUSIVE, 0x7E, 0x03, 0x7F, 0x21,
                0xF7.toByte(), MidiConstants.STATUS_CONTROL_CHANGE, 0x07, 0x52,
                0x0A, 0x63
            )
        )
        val expected = arrayOf(
            byteArrayOf(0xC0.toByte(), 0x05),
            byteArrayOf(0xC0.toByte(), 0x07),
            byteArrayOf(0xF0.toByte(), 0x7E, 0x03, 0x7F, 0x21, 0xF7.toByte()),
            byteArrayOf(0xB0.toByte(), 0x07, 0x52),
            byteArrayOf(0xB0.toByte(), 0x0A, 0x63)
        )
        checkSequence(original, expected, timestamp)
    }

    // Split a SysEx across 3 messages.
    @Test
    @Throws(IOException::class)
    fun testSplitSysEx() {
        val timestamp = 837518L
        val original = arrayOf(
            byteArrayOf(MidiConstants.STATUS_SYSTEM_EXCLUSIVE, 0x7E),
            byteArrayOf(0x03, 0x7F),
            byteArrayOf(0x21, 0xF7.toByte())
        )
        val expected = arrayOf(
            byteArrayOf(0xF0.toByte(), 0x7E),
            byteArrayOf(0x03, 0x7F),
            byteArrayOf(0x21, 0xF7.toByte())
        )
        checkSequence(original, expected, timestamp)
    }

    // RealTime in the middle of a SysEx
    @Test
    @Throws(IOException::class)
    fun testRealSysEx() {
        val timestamp = 837518L
        val original = arrayOf(
            byteArrayOf(
                MidiConstants.STATUS_SYSTEM_EXCLUSIVE, 0x7E,
                0x03, MidiConstants.STATUS_TIMING_CLOCK, 0x7F, 0x21,
                0xF7.toByte()
            )
        )
        val expected = arrayOf(
            byteArrayOf(0xF0.toByte(), 0x7E, 0x03),
            byteArrayOf(0xF8.toByte()),
            byteArrayOf(0x7F, 0x21, 0xF7.toByte())
        )
        checkSequence(original, expected, timestamp)
    }


}