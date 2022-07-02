package com.example.bluetoothmiditest

import com.example.bluetoothmiditest.midi.getBytesPerMessage
import com.example.bluetoothmiditest.storage.DataStore
import com.example.bluetoothmiditest.storage.MidiMessage
import timber.log.Timber
import java.io.Closeable


class MidiMessageHandlerImpl(
    private val dataStore: DataStore,
    private var storeMode: Boolean = false
) : MidiMessageHandler, Closeable {

    @ExperimentalUnsignedTypes
    override fun send(msg: UByteArray, offset: Int, count: Int, timestamp: Long) {
        if (storeMode) {
            Timber.d(
                "Translated MIDI message: ${
                    translateMidiMessage(
                        msg, offset, timestamp
                    )
                }"
            )

            dataStore.store(translateMidiMessage(msg, offset, timestamp))
        }
    }

    override fun store(store: Boolean) {
        this.storeMode = store
    }

    override fun isStoring() = storeMode


    companion object {

        @ExperimentalUnsignedTypes
        fun translateMidiMessage(data: UByteArray, inputOffset: Int, timestamp: Long): MidiMessage {
            var offset = inputOffset
            val statusByte = data[offset++]
            val status = statusByte.toInt()

            Timber.i("Status byte: $statusByte. Status: $status")

            val statusAsString = getName(status)
            val numData = getBytesPerMessage(statusByte.toInt()) - 1
            val channel = if (status in 0x80..0xef) {
                status and 0x0F
            } else {
                null
            }
            val midiData = (0 until numData).map { data[offset + it] }.joinToString()

            return MidiMessage(statusAsString, midiData, channel, timestamp)
        }

        private fun getName(status: Int): MidiCommand {
            return when {
                status >= 0xF0 -> {
                    val index = status and 0x0F
                    MidiCommand.values()[index]
                }
                status >= 0x80 -> {
                    val index = status shr 4 and 0x07
                    MidiCommand.values()[index + NUMBER_OF_SYSTEM_COMMANDS]
                }
                else -> {
                   MidiCommand.Data
                }
            }
        }
    }

    override fun close() {
        dataStore.close()
    }

}
