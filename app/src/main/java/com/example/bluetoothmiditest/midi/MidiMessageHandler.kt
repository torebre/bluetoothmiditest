package com.example.bluetoothmiditest.midi


/**
 * Implementations of this interface can assume that the MIDI message being
 * sent has been aligned so that the first byte in each message is a status byte.
 */
@ExperimentalUnsignedTypes
interface MidiMessageHandler {

    fun send(msg: UByteArray, offset: Int, count: Int, timestamp: Long)

    fun close()

}