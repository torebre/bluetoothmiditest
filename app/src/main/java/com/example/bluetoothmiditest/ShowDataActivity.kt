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
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.bluetoothmiditest.storage.Session
import com.google.gson.Gson
import java.io.BufferedOutputStream
import java.nio.charset.StandardCharsets


class ShowDataActivity : AppCompatActivity() {

    companion object {
        const val DATA_STORE_STATE = "DataStoreState"
        const val BLUETOOTH_DEVICE = "BluetoothDevice"
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

        val savedSession = savedInstanceState?.getSerializable(DATA_STORE_STATE)?.let {
            it as Session
        }

        dataStore = DataMemoryStore(savedSession ?: Session())

        midiMessageHandler = MidiMessageHandlerImpl(dataStore)
        midiMessageTranslator = MidiMessageTranslator(midiMessageHandler)
        midiMessageHandler.store(true)

        setContentView(R.layout.show_midi_data)

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
            Log.e("ShowData", "No address given")
            return
        }

        bluetoothDevice?.let {
            setupBluetoothConnection(it, dataView)
        }

        val button = findViewById<Button>(R.id.btnSave)
        button.setOnClickListener {
            dataStore.getData().takeIf { it.midiMessages.isNotEmpty() }?.let {
                val createDocumentIntent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/text"
                    putExtra(Intent.EXTRA_TITLE, "midi_output.txt")
                }
                getFileUrl.launch(createDocumentIntent)
            }
        }
    }

    private fun setupBluetoothConnection(bluetoothDevice: BluetoothDevice, dataView: TextView) {
        val midiManager =
            applicationContext.getSystemService(Context.MIDI_SERVICE) as MidiManager
        openBluetoothMidiDevice(bluetoothDevice, dataView, midiManager)

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
                Log.i("Bluetooth", "Device opened: $device")

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

                    Log.i("Midi", "Opened output port: $outputPort")

                    outputPort.connect(object : MidiReceiver() {
                        override fun onSend(
                            msg: ByteArray?,
                            offset: Int,
                            count: Int,
                            timestamp: Long
                        ) {
                            msg?.let {
                                midiMessageTranslator.onSend(msg, offset, count, timestamp)
                            }

                            runOnUiThread {
                                dataView.append("Got message. Count: $count\n")
                            }
                        }
                    })
                }
            }, Handler { msg ->
                Log.i("Bluetooth", "Message: $msg")
                true
            }
        )
    }


    private fun saveData(uri: Uri) {
        dataStore.getData().takeIf { it.midiMessages.isNotEmpty() }?.let { midiTextData ->
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                BufferedOutputStream(outputStream).let { bufferedOutputStream ->
                    bufferedOutputStream.writer(StandardCharsets.UTF_8).use {
                        Gson().toJson(midiTextData)
                    }
                }
            }
        }
    }

}