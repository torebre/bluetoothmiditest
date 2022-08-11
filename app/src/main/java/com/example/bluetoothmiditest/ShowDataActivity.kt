package com.example.bluetoothmiditest

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.media.midi.MidiDevice
import android.media.midi.MidiManager
import android.media.midi.MidiReceiver
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.bluetoothmiditest.midi.MidiMessageHandler
import com.example.bluetoothmiditest.midi.MidiMessageTranslator
import com.example.bluetoothmiditest.storage.DataMemoryStore
import com.example.bluetoothmiditest.storage.MidiMessage
import com.example.bluetoothmiditest.storage.MidiMessageListener
import com.example.bluetoothmiditest.storage.Session
import com.google.gson.Gson
import timber.log.Timber
import java.io.BufferedOutputStream
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


/**
 * Shows the incoming MIDI messages from a connected Bluetooth device. Allows the user
 * to save the messages.
 */
class ShowDataActivity : AppCompatActivity() {

    companion object {
        const val DATA_STORE_STATE = "DataStoreState"
        const val BLUETOOTH_DEVICE = "BluetoothDevice"

        private fun getDefaultTitle() =
            "midi_output_${LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)}.json"
    }

    private lateinit var midiMessageHandler: MidiMessageHandler
    private lateinit var midiMessageTranslator: MidiMessageTranslator
    private lateinit var dataStore: DataMemoryStore
    private var bluetoothDevice: BluetoothDevice? = null

    private val getFileUrl =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            activityResult.data?.data?.let { saveData(it) }
        }

    private var openedMidiDevice: MidiDevice? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val savedSession = savedInstanceState?.getSerializable(DATA_STORE_STATE) as Session?

        dataStore = DataMemoryStore(savedSession)

        setContentView(R.layout.show_midi_data)

        val noteView = findViewById<TextView>(R.id.noteData)

        val notesOn = mutableListOf<String>()
        midiMessageHandler = MidiMessageHandlerImpl().also {
           it.addMidiMessageListener(dataStore)

            it.addMidiMessageListener(object: MidiMessageListener {
                override fun store(midiMessage: MidiMessage) {
                    runOnUiThread {
                        when(midiMessage.midiCommand) {
                            MidiCommand.NoteOn -> {
                                notesOn.add(midiMessage.midiData)
                            }
                            MidiCommand.NoteOff -> {
                                notesOn.remove(midiMessage.midiData)
                            }
                            else -> {
                               // Do nothing
                            }
                        }
                        noteView.text = notesOn.joinToString()
                    }
                }

                override fun close() {
                    // Nothing to close
                }

            })

        }

        midiMessageTranslator = MidiMessageTranslator(midiMessageHandler)


        val dataView = findViewById<TextView>(R.id.midiData)

        // Get the Bluetooth device either from the saved bundle or
        // from the input given when this intent was started from
        // some other intent
        bluetoothDevice = savedInstanceState?.getParcelable(BLUETOOTH_DEVICE)
            ?: intent.extras?.let { bundle ->
                bundle[Intent.EXTRA_TEXT]?.let {
                    it as BluetoothDevice
                }
            }

        if (bluetoothDevice == null) {
            // Not expected
            Timber.e("No address given")
            return
        }


        bluetoothDevice?.let { bluetoothDevice ->
            openBluetoothMidiDevice(bluetoothDevice, dataView,
                (applicationContext.getSystemService(Context.MIDI_SERVICE) as MidiManager)
            )
        }

        findViewById<Button>(R.id.btnSave).apply {
            setOnClickListener {
                dataStore.getData().takeIf { it.midiMessages.isNotEmpty() }?.let {
                    val createDocumentIntent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "application/text"
                        putExtra(Intent.EXTRA_TITLE, getDefaultTitle())
                    }
                    getFileUrl.launch(createDocumentIntent)
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.run {
            putSerializable(DATA_STORE_STATE, dataStore.getData())

            bluetoothDevice?.let {
                putParcelable(BLUETOOTH_DEVICE, it)
            }
        }
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        openedMidiDevice?.run {
            close()
        }
        midiMessageHandler.close()
    }

    private fun openBluetoothMidiDevice(
        bluetoothDevice: BluetoothDevice,
        dataView: TextView,
        midiManager: MidiManager
    ) {
        midiManager.openBluetoothDevice(bluetoothDevice,
            { device ->
                Timber.i("Device opened: $device")

                openedMidiDevice = device

                device.let {
                    it.info.ports.forEach {
                        runOnUiThread {
                            dataView.append(
                                """
                                        Name: ${it.name}
                                        Type: ${it.type}
                                        Port number: ${it.portNumber}\n
                                    """.trimIndent()
                            )
                        }
                    }
                }

                val outputPort = device.openOutputPort(0)
                if (outputPort == null) {
                    runOnUiThread { dataView.append("Could not open port\n") }
                } else {
                    runOnUiThread { dataView.append("Opened port\n") }

                    Timber.i("Opened output port: $outputPort")

                    outputPort.connect(object : MidiReceiver() {
                        override fun onSend(
                            msg: ByteArray?,
                            offset: Int,
                            count: Int,
                            timestamp: Long
                        ) {

                            msg?.let { messageBytes ->
                                Timber.d("Offset: $offset. Count: $count. Timestamp: $timestamp. Bytes in message: ${messageBytes.joinToString { messageBytes.toString() }}")
                                midiMessageTranslator.onSend(msg, offset, count, timestamp)
                            }

                            runOnUiThread {
                                dataView.append("Got message. Count: $count\n")
                            }
                        }
                    })
                }
            }, Handler { msg ->
                Timber.i("Message: $msg")
                true
            }
        )
    }


    private fun saveData(uri: Uri) {
        Timber.i("Saving MIDI data")
        dataStore.getData().takeIf { it.midiMessages.isNotEmpty() }?.let { midiTextData ->
            Timber.i("Number of MIDI messages: ${midiTextData.midiMessages.size}")
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                BufferedOutputStream(outputStream).let { bufferedOutputStream ->
                    bufferedOutputStream.writer(StandardCharsets.UTF_8).use {
                        it.write(Gson().toJson(midiTextData))
                    }
                }
            }
        }
    }


}