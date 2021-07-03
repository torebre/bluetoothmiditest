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
@ExperimentalUnsignedTypes
object MidiConstants {
    internal const val TAG = "MidiTools"
    val STATUS_COMMAND_MASK: UByte = 0xF0.toUByte()
    val STATUS_CHANNEL_MASK: UByte = 0x0F.toUByte()

    // Channel voice messages.
    val STATUS_NOTE_OFF: UByte = 0x80.toUByte()
    val STATUS_NOTE_ON: UByte = 0x90.toUByte()
    val STATUS_POLYPHONIC_AFTERTOUCH: UByte = 0xA0.toUByte()
    val STATUS_CONTROL_CHANGE: UByte = 0xB0.toUByte()
    val STATUS_PROGRAM_CHANGE: UByte = 0xC0.toUByte()
    val STATUS_CHANNEL_PRESSURE: UByte = 0xD0.toUByte()
    val STATUS_PITCH_BEND: UByte = 0xE0.toUByte()

    // System Common Messages.
    val STATUS_SYSTEM_EXCLUSIVE: UByte = 0xF0.toUByte()
    val STATUS_MIDI_TIME_CODE: UByte = 0xF1.toUByte()
    val STATUS_SONG_POSITION: UByte = 0xF2.toUByte()
    val STATUS_SONG_SELECT: UByte = 0xF3.toUByte()
    val STATUS_TUNE_REQUEST: UByte = 0xF6.toUByte()
    val STATUS_END_SYSEX: UByte = 0xF7.toUByte()

    // System Real-Time Messages
    val STATUS_TIMING_CLOCK: UByte = 0xF8.toUByte()
    val STATUS_START: UByte = 0xFA.toUByte()
    val STATUS_CONTINUE: UByte = 0xFB.toUByte()
    val STATUS_STOP: UByte = 0xFC.toUByte()
    val STATUS_ACTIVE_SENSING: UByte = 0xFE.toUByte()
    val STATUS_RESET: UByte = 0xFF.toUByte()

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
    @ExperimentalUnsignedTypes
    fun isAllActiveSensing(
        msg: UByteArray, offset: Int,
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