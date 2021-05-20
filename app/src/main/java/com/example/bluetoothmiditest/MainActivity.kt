package com.example.bluetoothmiditest

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.midi.MidiDeviceInfo
import android.media.midi.MidiManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*


class MainActivity : AppCompatActivity() {


    companion object {
        private const val SCAN_PERIOD = 10000L
        private val MIDI_OVER_BTLE_UUID = UUID.fromString("03B80E5A-EDE8-4B33-A751-6CE34EC4C700")
    }


    private val foundBluetoothDevices = mutableSetOf<BluetoothDeviceData>()

    private var isScanning = false


    private lateinit var spinnerAdapterBluetooth: ArrayAdapter<BluetoothDeviceData>
    private lateinit var spinnerAdapterMidiData: ArrayAdapter<MidiDeviceData>

    private val BluetoothAdapter.isDisabled: Boolean
        get() = !isEnabled


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val permissionCheck = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        Log.i("Main", "Has ACCESS_FINE_LOCATION permission: $permissionCheck")
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            // ask permissions here using below code
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                2
            )
        }


        BluetoothAdapter.getDefaultAdapter()?.takeIf { it.isDisabled }?.apply {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, 1)
        }

        val spinnerBluetooth = findViewById<Spinner>(R.id.spinnerBluetoothMidiDevices)

        spinnerAdapterBluetooth = ArrayAdapter<BluetoothDeviceData>(spinnerBluetooth.context, android.R.layout.simple_spinner_item).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }


        spinnerBluetooth.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
//                TODO("Not yet implemented")

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                spinnerAdapterBluetooth.getItem(position)?.let {

                    // TODO

//                        withContext(Dispatchers.Default) {
                            deviceEntryClicked(it.bluetoothDevice)
//                        }


                }

            }

        }

        spinnerBluetooth.adapter = spinnerAdapterBluetooth
        val spinnerMidiDevices = findViewById<Spinner>(R.id.spinnerMidiDevices)
        spinnerAdapterMidiData = ArrayAdapter<MidiDeviceData>(spinnerMidiDevices.context, android.R.layout.simple_spinner_item).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }


        setupMidiDevices()


        val scanButton = findViewById<Button>(R.id.btnScan)
        scanButton.setOnClickListener {
            if (isScanning) {

                Log.i("Scanner", "Stopping scanning")

                stopScanning()
                scanButton.setText(R.string.scan)

            } else {

                Log.i("Scanner", "Starting scanning")

                BluetoothAdapter.getDefaultAdapter()?.let { scanLeDevices(it) }
                scanButton.setText(R.string.stop)
            }

        }

    }


    private fun scanLeDevices(bluetoothAdapter: BluetoothAdapter) {
        if (isScanning) {
            // Already scanning
            return
        }
        val leScanner = bluetoothAdapter.bluetoothLeScanner
        isScanning = true

        Log.i("Scanner", "Start scan")

        leScanner.startScan(
            emptyList(),
            ScanSettings.Builder().build(),
            object : ScanCallback() {

                override fun onScanResult(
                    callbackType: Int,
                    result: ScanResult?
                ) {
                    Log.i("Scanner", "Scan result. Callback type: ${callbackType}. Result: $result")
                    result?.apply {
                        runOnUiThread {
                            BluetoothDeviceData(device).let {
                                if(!foundBluetoothDevices.contains(it)) {
                                    foundBluetoothDevices.add(it)
                                    spinnerAdapterBluetooth.add(it)
                                    spinnerAdapterBluetooth.notifyDataSetChanged()
                                }
                            }
                        }
                    }
                }

                override fun onBatchScanResults(results: List<ScanResult?>?) {
                    Log.i("Scanner", "Scan results. Results: $results")

                    results?.apply {
                        filterNotNull().forEach {
                            BluetoothDeviceData(it.device).let { bluetoothDeviceData ->
                                if(!foundBluetoothDevices.contains(bluetoothDeviceData)) {
                                    foundBluetoothDevices.add(bluetoothDeviceData)
                                    spinnerAdapterBluetooth.add(bluetoothDeviceData)
                                }
                                spinnerAdapterBluetooth.notifyDataSetChanged()
                            }
                        }
                    }
                }

                override fun onScanFailed(errorCode: Int) {
                    Log.e("Scanner", "Scan failed. Error code: $errorCode")
                }
            }
        )

    }


    private fun setupMidiDevices() {
        val midiManager = getSystemService(Context.MIDI_SERVICE) as MidiManager

        midiManager.registerDeviceCallback(object: MidiManager.DeviceCallback() {

            override fun onDeviceAdded(device: MidiDeviceInfo?) {
                device?.let {deviceInfo ->
                    deviceInfo.properties[MidiDeviceInfo.PROPERTY_NAME].let {
                        spinnerAdapterMidiData.add(MidiDeviceData(it as String))
                    }
                }
            }

            override fun onDeviceRemoved(device: MidiDeviceInfo?) {
                device?.let {deviceInfo ->
                    deviceInfo.properties[MidiDeviceInfo.PROPERTY_NAME].let {
                        spinnerAdapterMidiData.remove(MidiDeviceData(it as String))
                    }
                }
            }

        }, null)
    }



    fun deviceEntryClicked(bluetoothDevice: BluetoothDevice) {
        stopScanning()

        Log.i("Bluetooth", "Entry clicked: $bluetoothDevice")

        val midiManager =
            applicationContext.getSystemService(Context.MIDI_SERVICE) as MidiManager

        midiManager.openBluetoothDevice(bluetoothDevice,
            { device ->
                Log.i("Bluetooth", "Device opened: $device")

                val openMidiDeviceIntent =
                    Intent(applicationContext, ShowDataActivity::class.java).apply {
                        putExtra(Intent.EXTRA_TEXT, device.info.id)
                    }
                startActivity(openMidiDeviceIntent)
            }, Handler { msg ->
                Log.i("Bluetooth", "Message: $msg")
                true
            }
        )




    }


    private fun connectToMidiDevice() {

        // TODO

//            Log.i("Midi", "Opened output port: $outputPort")
//
//            outputPort.connect(object : MidiReceiver() {
//                override fun onSend(
//                    msg: ByteArray?,
//                    offset: Int,
//                    count: Int,
//                    timestamp: Long
//                ) {
//
//                    midiMessageHandler.onSend(msg, offset, count, timestamp)
//
//                }
//            })
    }



    private fun stopScanning() {
        BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner.stopScan(object : ScanCallback() {
            // Do nothing

        })
        isScanning = false


    }

}
