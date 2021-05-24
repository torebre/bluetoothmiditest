package com.example.bluetoothmiditest

import android.util.Log
import java.io.BufferedWriter
import java.io.Closeable
import java.io.File
import java.nio.file.Files


class MidiMessageHandlerImpl(outputFile: File?): MidiMessageHandler, Closeable {

    private val outputWriter: BufferedWriter?
    private var storeMode = false

    init {
        outputWriter = outputFile?.let {
            Files.newBufferedWriter(it.toPath())
        }
    }

    override fun store(store: Boolean) {
        storeMode = store
    }

    override fun isStoring(): Boolean {
        return storeMode && outputWriter != null
    }


    override fun send(msg: ByteArray, offset: Int, count: Int, timestamp: Long) {
        if(storeMode) {
            outputWriter?.let {
                val message = translateMidiMessage(msg, offset)
                it.write("$timestamp: $message\n")
            }
        }

//        Log.i("MessageHandler", "Got message ${msg}")
    }


    fun getName(status: Int): String {
        return when {
            status >= 0xF0 -> {
                val index = status and 0x0F
                SystemCommandName.values()[index].name
            }
            status >= 0x80 -> {
                val index = status shr 4 and 0x07
                ChannelCommandName.values()[index].name
            }
            else -> {
                "data"
            }
        }
    }

    private fun translateMidiMessage(data: ByteArray, inputOffset: Int): String {
        var offset = inputOffset
        val sb = StringBuilder()
        val statusByte = data[offset++]
        val status: Int = MidiMessageTranslator.transformByteToInt(statusByte).and(0xFF)
        sb.append(getName(status)).append("(")
        val numData = MidiConstants.getBytesPerMessage(statusByte.toInt()) - 1
        if (status >= 0x80 && status < 0xF0) {
            val channel = status and 0x0F
            sb.append(channel).append(", ")
        }
        for (i in 0 until numData) {
            if (i > 0) {
                sb.append(", ")
            }
            sb.append(data[offset++])
        }
        sb.append(")")
        return sb.toString()
    }



    companion object {

        enum class ChannelCommandName {
            NoteOff,
            NoteOn,
            PolyTouch,
            Control,
            Program,
            Pressure,
            Bend
        }

        enum class SystemCommandName {
            SysEx,
            TimeCode,
            SongPos,
            SongSel,
            F4,
            F5,
            TuneReq,
            EndSysex,
            TimingClock,
            F9,
            Start,
            Continue,
            Stop,
            FD,
            ActiveSensing,
            Reset
        }
    }

    override fun close() {
        outputWriter.run { close() }
    }

}
