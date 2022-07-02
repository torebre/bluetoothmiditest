package com.example.bluetoothmiditest

import com.example.bluetoothmiditest.midi.MidiMessageTranslator
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import timber.log.Timber
import java.io.IOException


/**
 * These are the tests from TestMidiFramer in the MidiBtlePairing project in android-midisuite, licensed other under the Apache License, Version 2.0
 *
 */
@ExperimentalUnsignedTypes
class MidiMessageTranslatorTest {

    private lateinit var midiReceiver: DummyMidiReceiver


    @Before
    fun setup() {
        Timber.plant(TimberSystemOutTree())
        midiReceiver = DummyMidiReceiver()
    }

    @Test
    fun testNoteOn() {
        val messageTranslator = MidiMessageTranslator(midiReceiver)
        val data = ubyteArrayOf(0x90u, 0x45u, 0x32u)
        messageTranslator.onSend(data.toByteArray(), 0, data.size, 0)

        assertEquals(1, midiReceiver.receivedMessages.size)
        midiReceiver.receivedMessages[0].let {
            assertEquals(0, it.offset)
            assertEquals(3, it.length)
            assertEquals(0L, it.timestamp)
        }

    }

    @Throws(IOException::class)
    private fun checkSequence(
        original: Array<MidiMessage>,
        expected: Array<MidiMessage>
    ) {
        val framer = MidiMessageTranslator(midiReceiver)
        for (message in original) {
            framer.onSend(
                message.data.toByteArray(), 0, message.data.size,
                message.timestamp
            )
        }
        assertEquals(
            "command count", expected.size,
            midiReceiver.receivedMessages.size
        )
        for (i in expected.indices) {
            expected[i].check(midiReceiver.receivedMessages[i])
        }
    }

    @Throws(IOException::class)
    private fun checkSequence(
        original: Array<UByteArray>, expected: Array<UByteArray>,
        timestamp: Long
    ) {
        val originalMessages =
            original.map { MidiMessage(it, length = it.size, timestamp = timestamp) }.toTypedArray()
        val expectedMessages =
            expected.map { MidiMessage(it, length = it.size, timestamp = timestamp) }.toTypedArray()
        checkSequence(originalMessages, expectedMessages)
    }

    @Test
    @Throws(IOException::class)
    fun testSimpleSequence() {
        val timestamp = 8263518L
        val data = ubyteArrayOf(0x90u, 0x45u, 0x32u)
        val original: Array<MidiMessage> =
            arrayOf(
                MidiMessage(
                    data,
                    length = data.size,
                    timestamp = timestamp
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
            arrayOf(ubyteArrayOf(0x90u, 0x45u, 0x32u, 0x45u, 0x00u))
        val expected = arrayOf(
            ubyteArrayOf(0x90u, 0x45u, 0x32u),
            ubyteArrayOf(0x90u, 0x45u, 0x00u)
        )
        checkSequence(original, expected, timestamp)
    }

    // Start with unresolved running status that should be ignored
    @Test
    @Throws(IOException::class)
    fun testStartMiddle() {
        val timestamp = 837518L
        val original = arrayOf(
            ubyteArrayOf(
                0x23u,
                0x34u,
                0x90u,
                0x45u,
                0x32u,
                0x45u,
                0x00u
            )
        )

        Timber.i("${original[0]}")

        val expected = arrayOf(
            ubyteArrayOf(0x90u, 0x45u, 0x32u),
            ubyteArrayOf(0x90u, 0x45u, 0x00u)
        )
        checkSequence(original, expected, timestamp)
    }

    @Test
    @Throws(IOException::class)
    fun testTwoOn() {
        val timestamp = 837518L
        val original =
            arrayOf(ubyteArrayOf(0x90u, 0x45u, 0x32u, 0x47u, 0x63u))
        val expected = arrayOf(
            ubyteArrayOf(0x90u, 0x45u, 0x32u),
            ubyteArrayOf(0x90u, 0x47u, 0x63u)
        )
        checkSequence(original, expected, timestamp)
    }

    @Test
    @Throws(IOException::class)
    fun testThreeOn() {
        val timestamp = 837518L
        val original = arrayOf(
            ubyteArrayOf(
                0x90u,
                0x45u,
                0x32u,
                0x47u,
                0x63u,
                0x49u,
                0x23u
            )
        )
        val expected = arrayOf(
            ubyteArrayOf(0x90u, 0x45u, 0x32u),
            ubyteArrayOf(0x90u, 0x47u, 0x63u),
            ubyteArrayOf(0x90u, 0x49u, 0x23u)
        )
        checkSequence(original, expected, timestamp)
    }

    // A RealTime message before a NoteOn
    @Test
    @Throws(IOException::class)
    fun testRealTimeBefore() {
        val timestamp = 8375918L
        val original = arrayOf(
            ubyteArrayOf(
                MidiConstants.STATUS_TIMING_CLOCK, 0x90u,
                0x45u, 0x32u
            )
        )
        val expected = arrayOf(
            ubyteArrayOf(MidiConstants.STATUS_TIMING_CLOCK),
            ubyteArrayOf(0x90u, 0x45u, 0x32u)
        )
        checkSequence(original, expected, timestamp)
    }

    // A RealTime message in the middle of a NoteOn
    @Test
    @Throws(IOException::class)
    fun testRealTimeMiddle1() {
        val timestamp = 8375918L
        val original = arrayOf(
            ubyteArrayOf(
                0x90u, MidiConstants.STATUS_TIMING_CLOCK,
                0x45u, 0x32u
            )
        )
        val expected = arrayOf(
            ubyteArrayOf(MidiConstants.STATUS_TIMING_CLOCK),
            ubyteArrayOf(0x90u, 0x45u, 0x32u)
        )
        checkSequence(original, expected, timestamp)
    }

    @Test
    @Throws(IOException::class)
    fun testRealTimeMiddle2() {
        val timestamp = 8375918L
        val original = arrayOf(
            ubyteArrayOf(
                0x90u, 0x45u,
                MidiConstants.STATUS_TIMING_CLOCK, 0x32u
            )
        )
        val expected = arrayOf(
            ubyteArrayOf(0xF8u),
            ubyteArrayOf(0x90u, 0x45u, 0x32u)
        )
        checkSequence(original, expected, timestamp)
    }

    // A RealTime message after a NoteOn
    @Test
    @Throws(IOException::class)
    fun testRealTimeAfter() {
        val timestamp = 8375918L
        val original = arrayOf(
            ubyteArrayOf(
                0x90u, 0x45u, 0x32u,
                MidiConstants.STATUS_TIMING_CLOCK
            )
        )
        val expected = arrayOf(
            ubyteArrayOf(0x90u, 0x45u, 0x32u),
            ubyteArrayOf(0xF8u)
        )
        checkSequence(original, expected, timestamp)
    }

    // Break up running status across multiple messages
    @Test
    @Throws(IOException::class)
    fun testPieces() {
        val timestamp = 837518L
        val original = arrayOf(
            ubyteArrayOf(0x90u, 0x45u),
            ubyteArrayOf(0x32u, 0x47u),
            ubyteArrayOf(0x63u, 0x49u, 0x23u)
        )
        val expected = arrayOf(
            ubyteArrayOf(0x90u, 0x45u, 0x32u),
            ubyteArrayOf(0x90u, 0x47u, 0x63u),
            ubyteArrayOf(0x90u, 0x49u, 0x23u)
        )
        checkSequence(original, expected, timestamp)
    }

    // Break up running status into single byte messages
    @Test
    @Throws(IOException::class)
    fun testByByte() {
        val timestamp = 837518L
        val original = arrayOf(
            ubyteArrayOf(0x90u),
            ubyteArrayOf(0x45u),
            ubyteArrayOf(0x32u),
            ubyteArrayOf(0x47u),
            ubyteArrayOf(0x63u),
            ubyteArrayOf(0x49u),
            ubyteArrayOf(0x23u)
        )
        val expected = arrayOf(
            ubyteArrayOf(0x90u, 0x45u, 0x32u),
            ubyteArrayOf(0x90u, 0x47u, 0x63u),
            ubyteArrayOf(0x90u, 0x49u, 0x23u)
        )
        checkSequence(original, expected, timestamp)
    }

    @Test
    @Throws(IOException::class)
    fun testControlChange() {
        val timestamp = 837518L
        val original = arrayOf(
            ubyteArrayOf(
                MidiConstants.STATUS_CONTROL_CHANGE, 0x07u, 0x52u,
                0x0Au, 0x63u
            )
        )
        val expected = arrayOf(
            ubyteArrayOf(0xB0u, 0x07u, 0x52u),
            ubyteArrayOf(0xB0u, 0x0Au, 0x63u)
        )
        checkSequence(original, expected, timestamp)
    }

    @Test
    @Throws(IOException::class)
    fun testProgramChange() {
        val timestamp = 837518L
        val original =
            arrayOf(ubyteArrayOf(MidiConstants.STATUS_PROGRAM_CHANGE, 0x05u, 0x07u))
        val expected = arrayOf(
            ubyteArrayOf(0xC0u, 0x05u),
            ubyteArrayOf(0xC0u, 0x07u)
        )
        checkSequence(original, expected, timestamp)
    }

    // ProgramChanges, SysEx, ControlChanges
    @Test
    @Throws(IOException::class)
    fun testAck() {
        val timestamp = 837518L
        val original = arrayOf(
            ubyteArrayOf(
                MidiConstants.STATUS_PROGRAM_CHANGE, 0x05u, 0x07u,
                MidiConstants.STATUS_SYSTEM_EXCLUSIVE, 0x7Eu, 0x03u, 0x7Fu, 0x21u,
                0xF7u, MidiConstants.STATUS_CONTROL_CHANGE, 0x07u, 0x52u,
                0x0Au, 0x63u
            )
        )
        val expected = arrayOf(
            ubyteArrayOf(0xC0u, 0x05u),
            ubyteArrayOf(0xC0u, 0x07u),
            ubyteArrayOf(0xF0u, 0x7Eu, 0x03u, 0x7Fu, 0x21u, 0xF7u),
            ubyteArrayOf(0xB0u, 0x07u, 0x52u),
            ubyteArrayOf(0xB0u, 0x0Au, 0x63u)
        )
        checkSequence(original, expected, timestamp)
    }

    // Split a SysEx across 3 messages.
    @Test
    @Throws(IOException::class)
    fun testSplitSysEx() {
        val timestamp = 837518L
        val original = arrayOf(
            ubyteArrayOf(MidiConstants.STATUS_SYSTEM_EXCLUSIVE, 0x7Eu),
            ubyteArrayOf(0x03u, 0x7Fu),
            ubyteArrayOf(0x21u, 0xF7u)
        )
        val expected = arrayOf(
            ubyteArrayOf(0xF0u, 0x7Eu),
            ubyteArrayOf(0x03u, 0x7Fu),
            ubyteArrayOf(0x21u, 0xF7u)
        )
        checkSequence(original, expected, timestamp)
    }

    // RealTime in the middle of a SysEx
    @Test
    @Throws(IOException::class)
    fun testRealSysEx() {
        val timestamp = 837518L
        val original = arrayOf(
            ubyteArrayOf(
                MidiConstants.STATUS_SYSTEM_EXCLUSIVE, 0x7Eu,
                0x03u, MidiConstants.STATUS_TIMING_CLOCK, 0x7Fu, 0x21u,
                0xF7u
            )
        )
        val expected = arrayOf(
            ubyteArrayOf(0xF0u, 0x7Eu, 0x03u),
            ubyteArrayOf(0xF8u),
            ubyteArrayOf(0x7Fu, 0x21u, 0xF7u)
        )
        checkSequence(original, expected, timestamp)
    }

}