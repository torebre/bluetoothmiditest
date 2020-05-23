package com.example.bluetoothmiditest

import android.media.midi.MidiReceiver

class MidiMessageTranslator(val receiver: MidiReceiver) {
    private var needed = 0
    private var inSysEx = false
    private var runningStatus : Byte = 0
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
    }


    fun processMessage(msg: ByteArray?,
                       offset: Int,
                       count: Int,
                       timestamp: Long) {
        if(msg == null) {
            return
        }
        var sysExStartOffset = if(inSysEx) { offset } else { -1 }
        var tempOffset = offset



        for(i in 0 until count) {
            val currentByte = msg[i + offset]
            // TODO Test that this is correct
            val currentInt = transformByteToInt(currentByte)


            if(currentInt > 0x80) {
                if(currentInt < 0xF0) {
                    // SysEx start
                    runningStatus = currentByte
                    currentCount = 1
                    needed = MidiConstants.getBytesPerMessage(currentInt) - 1

                }
                else if(currentInt < 0xF8) {
                    if(currentInt == 0xF0) {
                        inSysEx = true
                        sysExStartOffset = offset
                    }
                    else if(currentInt == 0xF7) {
                        // SysEx end
                        if(inSysEx) {
                            receiver.send(msg, sysExStartOffset, offset - sysExStartOffset + 1, timestamp)
                            inSysEx = false
                            sysExStartOffset = -1
                        }
                    }
                    else {
                        buffer[0] = currentByte
                        runningStatus = 0
                        currentCount = 1
                        needed = MidiConstants.getBytesPerMessage(currentInt) - 1
                    }
                }
                else {
                    buffer[0] = currentByte
                    runningStatus = 0
                    currentCount = 1
                    needed = MidiConstants.getBytesPerMessage(currentInt) - 1
                }

            }
            else {
                if(!inSysEx) {
                    buffer[currentCount++] = currentByte
                    if(--needed == 0) {
                        if(runningStatus != 0.toByte()) {
                            buffer[0] = runningStatus
                        }
                        receiver.send(buffer, 0, currentCount, timestamp)
                        needed = MidiConstants.getBytesPerMessage(transformByteToInt(buffer[0]) - 1)
                        currentCount = 1
                    }
                }
            }
            ++tempOffset
        }


        if(sysExStartOffset in 0 until tempOffset) {
            receiver.send(msg, sysExStartOffset, offset - sysExStartOffset, timestamp)

        }



    }


    private fun transformByteToInt(inputByte: Byte) = inputByte.toUInt().and(0x000000FF.toUInt()).toInt()

    private fun translateMessage(data: ByteArray, offset: Int, count: Int) {
        var tempOffset = offset
        val statusByte = data[tempOffset++]
        val status = transformByteToInt(statusByte)


        // TODO
        if (status >= 0xF0) {
            val index = status and 0x0F
            SystemCommandName.values()[index]
        } else if (status >= 0x80) {
            val index = status shr 4 and 0x07
            ChannelCommandName.values()[index]
        } else {
            null
        }

        val numberOfBytes = MidiConstants.getBytesPerMessage(status) - 1

        if((status >= 0x80) && (status < 0xF0)) {
            // Channel message
            val channel = status.and(0xF0)

        }







    }



}