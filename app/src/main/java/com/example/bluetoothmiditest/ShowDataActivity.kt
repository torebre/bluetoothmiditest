package com.example.bluetoothmiditest

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.media.midi.MidiDevice
import android.media.midi.MidiManager
import android.media.midi.MidiReceiver
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.koin.android.ext.android.inject


class ShowDataActivity : AppCompatActivity() {

    val midiMessageHandler: MidiMessageHandler by inject()

    private var openedMidiDevice: MidiDevice? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.show_midi_data)

        val dataView = findViewById<TextView>(R.id.midiData)

        if(intent.extras == null) {
            Log.e("ShowData", "No address given")
            return
        }

            intent.extras?.let { bundle ->
                bundle[Intent.EXTRA_TEXT]?.let {
                    val bluetoothDevice = it as BluetoothDevice
                    val midiManager =
                        applicationContext.getSystemService(Context.MIDI_SERVICE) as MidiManager
                    openBluetoothMidiDevice(bluetoothDevice, dataView, midiManager)
                }
            }
        }

    override fun onDestroy() {
        super.onDestroy()
        openedMidiDevice?.run {
            close()
        }
    }

    private fun openBluetoothMidiDevice(bluetoothDevice: BluetoothDevice, dataView: TextView, midiManager: MidiManager) {
        midiManager.openBluetoothDevice(bluetoothDevice,
            { device ->
                Log.i("Bluetooth", "Device opened: $device")

                openedMidiDevice = device

                device.let {
                    it.info.ports.forEach {
                        runOnUiThread {
                            dataView.append("""
                                        Name: ${it.name}
                                        Type: ${it.type}
                                        Port number: ${it.portNumber}\n
                                    """.trimIndent())
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

                            midiMessageHandler.onSend(msg, offset, count, timestamp)

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


}