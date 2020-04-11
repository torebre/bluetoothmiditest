package com.example.bluetoothmiditest

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.media.midi.MidiDevice
import android.media.midi.MidiManager
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
        val macAddress = intent.extras[Intent.EXTRA_TEXT] as String?

        if (macAddress == null) {
            // TODO
            return
        }

        openDevice(macAddress, dataView)
    }


    private fun openDevice(macAddress: String, dataView: TextView) {
        val defaultAdapter = BluetoothAdapter.getDefaultAdapter() ?: return

        defaultAdapter.bondedDevices.find { it.address == macAddress }?.run {

            val midiManager =
                applicationContext.getSystemService(Context.MIDI_SERVICE) as MidiManager


            midiManager.openBluetoothDevice(this,
                object : MidiManager.OnDeviceOpenedListener {
                    override fun onDeviceOpened(device: MidiDevice?) {
                        Log.i("Bluetooth", "Device opened: $device")
                    }
                }, Handler(Handler.Callback { msg ->
                    Log.i("Bluetooth", "Message: $msg")

                    dataView.append("$msg\n")
                    true
                })
            )

        }


    }


}