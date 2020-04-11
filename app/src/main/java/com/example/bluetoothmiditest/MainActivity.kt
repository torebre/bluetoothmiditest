package com.example.bluetoothmiditest

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
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


class MainActivity : AppCompatActivity(), ConnectHandler {
//    private lateinit var viewAdapter: MidiDeviceAdapter
    private lateinit var viewAdapter: BluetoothDeviceAdapter

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


//        viewAdapter = MidiDeviceAdapter(deviceList)
//
//        midiManager.registerDeviceCallback(object: MidiManager.DeviceCallback() {
//
//            override fun onDeviceAdded(device: MidiDeviceInfo?) {
//                device?.let { deviceList.add(it.id.toString())
//                    viewAdapter.notifyDataSetChanged()
//                }
//            }
//
//            override fun onDeviceRemoved(device: MidiDeviceInfo?) {
//                device?.let { deviceList.remove(it.id.toString())
//                    viewAdapter.notifyDataSetChanged()
//                }
//            }
//
//            override fun onDeviceStatusChanged(status: MidiDeviceStatus?) {
//                Log.i(TAG, "Status: $status")
//            }
//
//
//        }, null)


        val bluetoothDevices = listBluetoothDevices()

        viewAdapter = BluetoothDeviceAdapter(bluetoothDevices, this)
        findViewById<RecyclerView>(R.id.midiDevices).apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }

    }


    private fun listBluetoothDevices(): MutableList<Pair<String, String>> {
        val defaultAdapter = BluetoothAdapter.getDefaultAdapter() ?: return mutableListOf()

        if(!defaultAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)


        }

        val bondedDevices = defaultAdapter.bondedDevices


        return bondedDevices.map {
            Pair(it.name, it.address)
        }.toMutableList()



    }

    override fun deviceEntryClicked(macAddress: String) {
//        val defaultAdapter = BluetoothAdapter.getDefaultAdapter() ?: return

//        defaultAdapter.bondedDevices.find { it.address == macAddress }?.let {
            val openBluetoothDeviceIntent = Intent(applicationContext, BluetoothDeviceAdapter::class.java).apply {
                putExtra(Intent.EXTRA_TEXT, macAddress)

            }

            startService(openBluetoothDeviceIntent)


//        }



    }

}
