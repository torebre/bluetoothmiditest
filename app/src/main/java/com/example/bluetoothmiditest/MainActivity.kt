package com.example.bluetoothmiditest

import android.content.Context
import android.content.pm.PackageManager
import android.media.midi.MidiDeviceInfo
import android.media.midi.MidiDeviceStatus
import android.media.midi.MidiManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class MainActivity : AppCompatActivity() {
    private lateinit var viewAdapter: MidiDeviceAdapter

    private val TAG = "Main"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_MIDI)) {
            Toast.makeText(this@MainActivity, "No MIDI support", Toast.LENGTH_LONG).show()
        }

        var viewManager = LinearLayoutManager(this)

        val midiManager = applicationContext.getSystemService(Context.MIDI_SERVICE) as MidiManager
        val deviceList = midiManager.devices.map { it.id }.map { it.toString() }.toMutableList()

        deviceList.add("1")
        deviceList.add("2")

        viewAdapter = MidiDeviceAdapter(deviceList)

        midiManager.registerDeviceCallback(object: MidiManager.DeviceCallback() {

            override fun onDeviceAdded(device: MidiDeviceInfo?) {
                device?.let { deviceList.add(it.id.toString())
                    viewAdapter.notifyDataSetChanged()
                }
            }

            override fun onDeviceRemoved(device: MidiDeviceInfo?) {
                device?.let { deviceList.remove(it.id.toString())
                    viewAdapter.notifyDataSetChanged()
                }
            }

            override fun onDeviceStatusChanged(status: MidiDeviceStatus?) {
                Log.i(TAG, "Status: $status")
            }


        }, null)


        val recyclerView = findViewById<RecyclerView>(R.id.midiDevices).apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }

    }

}
