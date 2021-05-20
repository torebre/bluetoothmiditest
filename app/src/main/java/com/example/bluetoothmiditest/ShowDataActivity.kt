package com.example.bluetoothmiditest

import android.content.Context
import android.content.Intent
import android.media.midi.MidiDevice
import android.media.midi.MidiManager
import android.media.midi.MidiReceiver
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.koin.android.ext.android.inject


class ShowDataActivity : AppCompatActivity() {

    val midiMessageHandler: MidiMessageHandler by inject()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.show_midi_data)

        val dataView = findViewById<TextView>(R.id.midiData)

        if(intent.extras != null) {
            Log.e("ShowData", "No address given")
            return
        }

            intent.extras?.let { bundle ->
                bundle[Intent.EXTRA_TEXT]?.let {
                    val deviceId =  it as Int
                    val midiManager =
                        applicationContext.getSystemService(Context.MIDI_SERVICE) as MidiManager
                    openDevice(deviceId, dataView, midiManager)
                }
            }
        }




    private fun openDevice(deviceId: Int, dataView: TextView, midiManager: MidiManager) {
        val midiDevice = midiManager.devices.firstOrNull { midiDeviceInfo ->
            midiDeviceInfo.id == deviceId
        }

        if(midiDevice == null) {
            Log.e("ShowData", "MIDI device not found")
           return
        }

        midiManager.openDevice(midiDevice, object : MidiManager.OnDeviceOpenedListener {

            override fun onDeviceOpened(device: MidiDevice?) {
                if (device == null) {
                    Log.i("Midi", "Device is null")

                    runOnUiThread {
                        dataView.append("Device is null\n")
                    }

                    return
                }


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
            }


        }, null)

        dataView.append("Opening device: $midiDevice\n")
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