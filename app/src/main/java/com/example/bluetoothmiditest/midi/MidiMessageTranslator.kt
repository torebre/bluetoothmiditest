package com.example.bluetoothmiditest.midi

import android.media.midi.MidiReceiver


/**
 * This is based on MidiFramer in the MidiBtlePairing project in android-midisuite, licensed other under the Apache License, Version 2.0
 */
@ExperimentalUnsignedTypes
class MidiMessageTranslator(private val receiver: MidiMessageHandler): MidiReceiver() {
    private var needed = 0
    private var inSysEx = false
    private var runningStatus: UByte = 0u
    private var currentCount = 0

    private val buffer = UByteArray(3)


    companion object {

        @ExperimentalUnsignedTypes
        fun transformByteToInt(inputByte: Byte) =
            inputByte.toUInt().and(0x000000FF.toUInt()).toInt()
    }


    @ExperimentalUnsignedTypes
    override fun onSend(
        msg: ByteArray,
        offset: Int,
        count: Int,
        timestamp: Long
    ) {
        var sysExStartOffset = if (inSysEx) {
            offset
        } else {
            -1
        }
        var tempOffset = offset

        for (i in 0 until count) {
            val currentByte = msg[tempOffset].toUByte()
            val currentInt = currentByte.toUInt()

            if (currentInt >= 0x80u) {
               // Status byte
                if (currentInt < 0xF0u) {
                    // Channel message
                    runningStatus = currentByte
                    currentCount = 1
                    needed = getBytesPerMessage(currentInt.toInt()) - 1
                } else if (currentInt < 0xF8u) {
                    // System common
                    if (currentInt == 0xF0u) {
                        // SysEx start
                        inSysEx = true
                        sysExStartOffset = tempOffset
                    } else if (currentInt == 0xF7u) {
                        // SysEx end
                        if (inSysEx) {
                            receiver.send(
                                msg.toUByteArray(),
                                sysExStartOffset,
                                tempOffset - sysExStartOffset + 1,
                                timestamp
                            )
                            inSysEx = false
                            sysExStartOffset = -1
                        }
                    } else {
                        buffer[0] = currentByte
                        runningStatus = 0u
                        currentCount = 1
                        needed = getBytesPerMessage(currentInt.toInt()) - 1
                    }
                } else {
                    // Real-time
                    if (inSysEx) {
                        receiver.send(
                            msg.toUByteArray(),
                            sysExStartOffset,
                            tempOffset - sysExStartOffset,
                            timestamp
                        )
                        sysExStartOffset = tempOffset + 1
                    }
                    receiver.send(msg.toUByteArray(), tempOffset, 1, timestamp)
                }
            } else {
                // Data byte
                if (!inSysEx) {
                    buffer[currentCount++] = currentByte
                    if (--needed == 0) {
                        if (runningStatus != 0.toUByte()) {
                            buffer[0] = runningStatus
                        }
                        receiver.send(buffer, 0, currentCount, timestamp)
                        needed = getBytesPerMessage(buffer[0].toInt()) - 1
                        currentCount = 1
                    }
                }
            }
            ++tempOffset
        }

        if (sysExStartOffset in 0 until tempOffset) {
            receiver.send(msg.toUByteArray(), sysExStartOffset, tempOffset - sysExStartOffset, timestamp)
        }
    }

}