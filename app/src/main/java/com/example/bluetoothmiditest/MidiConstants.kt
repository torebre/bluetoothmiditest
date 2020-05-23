package com.example.bluetoothmiditest

/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/**
 * MIDI related constants and static methods.
 * These values are defined in the MIDI Standard 1.0
 * available from the MIDI Manufacturers Association.
 */
object MidiConstants {
    internal const val TAG = "MidiTools"
    const val STATUS_COMMAND_MASK = 0xF0.toByte()
    const val STATUS_CHANNEL_MASK = 0x0F.toByte()

    // Channel voice messages.
    const val STATUS_NOTE_OFF = 0x80.toByte()
    const val STATUS_NOTE_ON = 0x90.toByte()
    const val STATUS_POLYPHONIC_AFTERTOUCH = 0xA0.toByte()
    const val STATUS_CONTROL_CHANGE = 0xB0.toByte()
    const val STATUS_PROGRAM_CHANGE = 0xC0.toByte()
    const val STATUS_CHANNEL_PRESSURE = 0xD0.toByte()
    const val STATUS_PITCH_BEND = 0xE0.toByte()

    // System Common Messages.
    const val STATUS_SYSTEM_EXCLUSIVE = 0xF0.toByte()
    const val STATUS_MIDI_TIME_CODE = 0xF1.toByte()
    const val STATUS_SONG_POSITION = 0xF2.toByte()
    const val STATUS_SONG_SELECT = 0xF3.toByte()
    const val STATUS_TUNE_REQUEST = 0xF6.toByte()
    const val STATUS_END_SYSEX = 0xF7.toByte()

    // System Real-Time Messages
    const val STATUS_TIMING_CLOCK = 0xF8.toByte()
    const val STATUS_START = 0xFA.toByte()
    const val STATUS_CONTINUE = 0xFB.toByte()
    const val STATUS_STOP = 0xFC.toByte()
    const val STATUS_ACTIVE_SENSING = 0xFE.toByte()
    const val STATUS_RESET = 0xFF.toByte()

    /** Number of bytes in a message nc from 8c to Ec  */
    val CHANNEL_BYTE_LENGTHS = intArrayOf(3, 3, 3, 3, 2, 2, 3)

    /** Number of bytes in a message Fn from F0 to FF  */
    val SYSTEM_BYTE_LENGTHS = intArrayOf(
        1, 2, 3, 2, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1
    )
    const val MAX_CHANNELS = 16

    /**
     * MIDI messages, except for SysEx, are 1,2 or 3 bytes long.
     * You can tell how long a MIDI message is from the first status byte.
     * Do not call this for SysEx, which has variable length.
     * @param statusByte
     * @return number of bytes in a complete message, zero if data byte passed
     */
    fun getBytesPerMessage(statusByte: Int): Int {
        // Java bytes are signed so we need to mask off the high bits
        // to get a value between 0 and 255.
//        val statusInt: Int = statusByte & 0xFF
        return if (statusByte >= 0xF0) {
            // System messages use low nibble for size.
            SYSTEM_BYTE_LENGTHS[statusByte and 0x0F]
        } else if (statusByte >= 0x80) {
            // Channel voice messages use high nibble for size.
            CHANNEL_BYTE_LENGTHS[(statusByte shr 4) - 8]
        } else {
            0 // data byte
        }
    }

    /**
     * @param msg
     * @param offset
     * @param count
     * @return true if the entire message is ActiveSensing commands
     */
    fun isAllActiveSensing(
        msg: ByteArray, offset: Int,
        count: Int
    ): Boolean {
        // Count bytes that are not active sensing.
        var goodBytes = 0
        for (i in 0 until count) {
            val b = msg[offset + i]
            if (b != STATUS_ACTIVE_SENSING) {
                goodBytes++
            }
        }
        return goodBytes == 0
    }
}