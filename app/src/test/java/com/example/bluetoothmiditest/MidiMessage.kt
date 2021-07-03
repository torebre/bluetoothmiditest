package com.example.bluetoothmiditest

import org.junit.Assert

// Store a complete MIDI message.
@ExperimentalUnsignedTypes
internal class MidiMessage(
    buffer: UByteArray,
    val offset: Int = 0,
    val length: Int,
    val timestamp: Long
) {
    val data: UByteArray = UByteArray(length).also {
        buffer.copyInto(it, 0, offset, length + offset)
    }
    val timeReceived: Long = System.nanoTime()

    // Check whether these two messages are the same
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
}