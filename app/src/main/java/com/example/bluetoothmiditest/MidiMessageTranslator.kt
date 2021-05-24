package com.example.bluetoothmiditest

import android.media.midi.MidiReceiver


/**
 * This is based on MidiFramer in the MidiBtlePairing project in android-midisuite, licensed other under the Apache License, Version 2.0
 */
class MidiMessageTranslator(private val receiver: MidiMessageHandler): MidiReceiver() {
    private var needed = 0
    private var inSysEx = false
    private var runningStatus: Byte = 0
    private var currentCount = 0

    private val buffer = ByteArray(3)


    companion object {

        enum class ChannelCommandName {
            NOTE_OFF,
            NOTE_ON,
            POLY_TOUCH,
            CONTROL,
            PROGRAM,
            PRESSURE,
            BEND
        }

        enum class SystemCommandName {
            SYS_EX,
            TIME_CODE,
            SONG_POS,
            SONG_SEL,
            F4,
            F5,
            TUNE_REQ,
            END_SYS_EX,
            TIMING_CLOCK,
            F9,
            START,
            CONTINUE,
            STOP,
            FD,
            ACTIVE_SENSING,
            RESET
        }

        @ExperimentalUnsignedTypes
        fun transformByteToInt(inputByte: Byte) =
            inputByte.toUInt().and(0x000000FF.toUInt()).toInt()
    }


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
            val currentByte = msg[tempOffset]
            // TODO Test that this is correct
            val currentInt = transformByteToInt(currentByte)

            if (currentInt > 0x80) {
                if (currentInt < 0xF0) {
                    // Channel message
                    runningStatus = currentByte
                    currentCount = 1
                    needed = MidiConstants.getBytesPerMessage(currentInt) - 1
                } else if (currentInt < 0xF8) {
                    // System common
                    if (currentInt == 0xF0) {
                        // SysEx start
                        inSysEx = true
                        sysExStartOffset = tempOffset
                    } else if (currentInt == 0xF7) {
                        // SysEx end
                        if (inSysEx) {
                            receiver.send(
                                msg,
                                sysExStartOffset,
                                tempOffset - sysExStartOffset + 1,
                                timestamp
                            )
                            inSysEx = false
                            sysExStartOffset = -1
                        }
                    } else {
                        buffer[0] = currentByte
                        runningStatus = 0
                        currentCount = 1
                        needed = MidiConstants.getBytesPerMessage(currentInt) - 1
                    }
                } else {
                    // Real-time
                    if (inSysEx) {
                        receiver.send(
                            msg,
                            sysExStartOffset,
                            tempOffset - sysExStartOffset,
                            timestamp
                        )
                        sysExStartOffset = tempOffset + 1
                    }
                    receiver.send(msg, tempOffset, 1, timestamp)
                }
            } else {
                // Data byte
                if (!inSysEx) {
                    buffer[currentCount++] = currentByte
                    if (--needed == 0) {
                        if (runningStatus != 0.toByte()) {
                            buffer[0] = runningStatus
                        }
                        receiver.send(buffer, 0, currentCount, timestamp)
                        needed = MidiConstants.getBytesPerMessage(transformByteToInt(buffer[0])) - 1
                        currentCount = 1
                    }
                }
            }
            ++tempOffset
        }

        if (sysExStartOffset >= 0 && sysExStartOffset < tempOffset) {
            receiver.send(msg, sysExStartOffset, tempOffset - sysExStartOffset, timestamp)
        }
    }




}