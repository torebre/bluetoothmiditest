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


class ShowDataActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.show_midi_data)

        val dataView = findViewById<TextView>(R.id.midiData)
        val bluetoothDevice = intent.extras[Intent.EXTRA_TEXT] as BluetoothDevice?

        if (bluetoothDevice == null) {
            Log.e("ShowData", "No address given")
            return
        }

        openDevice(bluetoothDevice, dataView)
    }

    private fun openDevice(bluetoothDevice: BluetoothDevice, dataView: TextView) {
        val midiManager =
            applicationContext.getSystemService(Context.MIDI_SERVICE) as MidiManager

        dataView.append("Opening device: $bluetoothDevice\n")

        midiManager.openBluetoothDevice(bluetoothDevice,
            { device ->
                Log.i("Bluetooth", "Device opened: $device")

                midiManager.openDevice(device.info, object : MidiManager.OnDeviceOpenedListener {

                    override fun onDeviceOpened(device: MidiDevice?) {
                        if (device == null) {
                            Log.i("Midi", "Device is null")

                            runOnUiThread {
                                dataView.append("Device is null\n")
                            }

                            return
                        }


                        device?.let {
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
                                    runOnUiThread {
                                        dataView.append("Got message. Count: $count\n")
                                    }

                                }

                            })
                        }
                    }


                }, null)




                listMidiDevices(dataView)
            }, Handler(Handler.Callback { msg ->
                Log.i("Bluetooth", "Message: $msg")

                dataView.append("$msg\n")
                true
            })
        )
    }


    private fun listMidiDevices(dataView: TextView) {
        val midiManager = applicationContext.getSystemService(Context.MIDI_SERVICE) as MidiManager
//        val deviceList = midiManager.devices.map { it.id }.map { it.toString() }.toMutableList()

//        midiManager.registerDeviceCallback(object: MidiManager.DeviceCallback() {
//
//            override fun onDeviceAdded(device: MidiDeviceInfo?) {
//                device?.let { deviceList.add(it.id.toString())
//                    Log.i("Midi", "Device added: ${device}")
//                }
//            }
//
//            override fun onDeviceRemoved(device: MidiDeviceInfo?) {
//                device?.let { deviceList.remove(it.id.toString())
//                    Log.i("Midi", "Device removed: ${device}")
//                }
//            }
//
//            override fun onDeviceStatusChanged(status: MidiDeviceStatus?) {
//                Log.i("Midi", "Status: $status")
//            }
//
//
//        }, null)

        midiManager.devices.forEach {
            dataView.append("Midi device: ${it}\n")
        }

    }


}