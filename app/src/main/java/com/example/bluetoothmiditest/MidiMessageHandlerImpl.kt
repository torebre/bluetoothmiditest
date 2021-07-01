package com.example.bluetoothmiditest

import com.example.bluetoothmiditest.storage.MidiMessage
import timber.log.Timber
import java.io.Closeable


class MidiMessageHandlerImpl(private val dataStore: DataStore, private var storeMode: Boolean = false) : MidiMessageHandler, Closeable {

    @ExperimentalUnsignedTypes
    override fun send(msg: ByteArray, offset: Int, count: Int, timestamp: Long) {
        if (storeMode) {

            Timber.i("Translated MIDI message: ${translateMidiMessage(msg, offset, timestamp)}")

            dataStore.store(translateMidiMessage(msg, offset, timestamp))
        }
    }

    private fun getName(status: Int): String {
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

//    @ExperimentalUnsignedTypes
//    private fun translateMidiMessage(data: ByteArray, inputOffset: Int): String {
//        var offset = inputOffset
//        val sb = StringBuilder()
//        val statusByte = data[offset++]
//        val status: Int = MidiMessageTranslator.transformByteToInt(statusByte).and(0xFF)
//        sb.append(getName(status)).append("(")
//        val numData = MidiConstants.getBytesPerMessage(statusByte.toInt()) - 1
//        if (status in 0x80..0xef) {
//            val channel = status and 0x0F
//            sb.append(channel).append(", ")
//        }
//        for (i in 0 until numData) {
//            if (i > 0) {
//                sb.append(", ")
//            }
//            sb.append(data[offset++])
//        }
//        sb.append(")")
//        return sb.toString()
//    }

    @ExperimentalUnsignedTypes
    private fun translateMidiMessage(data: ByteArray, inputOffset: Int, timestamp: Long): MidiMessage {
        var offset = inputOffset
        val statusByte = data[offset++]
        val status: Int = MidiMessageTranslator.transformByteToInt(statusByte).and(0xFF)

        Timber.i("Status byte: $statusByte. Status: $status")


        val statusAsString = getName(status)
        val numData = MidiConstants.getBytesPerMessage(statusByte.toInt()) - 1
        val channel = if (status in 0x80..0xef) {
            status and 0x0F
        }
        else {
            null
        }
        val midiData = (0 until numData).map { data[offset + it] }.joinToString()

        return MidiMessage(statusAsString, midiData, channel, timestamp)
    }

    override fun store(store: Boolean) {
        this.storeMode = store
    }

    override fun isStoring() = storeMode


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
        dataStore.close()
    }

}
