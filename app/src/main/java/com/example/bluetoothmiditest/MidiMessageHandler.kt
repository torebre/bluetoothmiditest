package com.example.bluetoothmiditest


/**
 * Implementations of this interface can assume that the MIDI message being
 * sent has been aligned so that the first byte in each message is a status byte.
 */
interface MidiMessageHandler {

    fun store(store: Boolean)

    fun isStoring(): Boolean

    fun send(msg: ByteArray, offset: Int, count: Int, timestamp: Long)

    fun close()

}