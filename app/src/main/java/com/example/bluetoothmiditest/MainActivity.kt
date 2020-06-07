package com.example.bluetoothmiditest

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.midi.MidiDevice
import android.media.midi.MidiDeviceInfo
import android.media.midi.MidiManager
import android.media.midi.MidiReceiver
import android.os.Bundle
import android.os.Handler
import android.os.ParcelUuid
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.koin.ext.getScopeName
import java.util.*


class MainActivity : AppCompatActivity(), ConnectHandler {


    companion object {
        private const val SCAN_PERIOD = 10000L
        private val MIDI_OVER_BTLE_UUID = UUID.fromString("03B80E5A-EDE8-4B33-A751-6CE34EC4C700")
    }

    //    private lateinit var viewAdapter: MidiDeviceAdapter
    private lateinit var viewAdapter: BluetoothScanResults

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

        viewAdapter = BluetoothScanResults(this)
        val spinnerBluetooth = findViewById<Spinner>(R.id.spinnerBluetoothMidiDevices)

        spinnerAdapterBluetooth = ArrayAdapter<BluetoothDeviceData>(spinnerBluetooth.context, android.R.layout.simple_spinner_item).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
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
            listOf(ScanFilter.Builder().setServiceUuid(ParcelUuid(MIDI_OVER_BTLE_UUID)).build()),
            ScanSettings.Builder().build(),
            object : ScanCallback() {

                override fun onScanResult(
                    callbackType: Int,
                    result: ScanResult?
                ) {
                    Log.i("Scanner", "Scan result. Callback type: ${callbackType}. Result: $result")
                    result?.apply {
                        runOnUiThread {
                            viewAdapter.addScanResult(this)
                            viewAdapter.notifyDataSetChanged()
                        }
                    }
                }

                override fun onBatchScanResults(results: List<ScanResult?>?) {
                    Log.i("Scanner", "Scan results. Results: $results")

                    results?.apply {
                        filterNotNull().forEach { viewAdapter.addScanResult(it) }
                        viewAdapter.notifyDataSetChanged()
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



    override fun deviceEntryClicked(scanResult: ScanResult) {
        stopScanning()

//        val openBluetoothDeviceIntent =
//            Intent(applicationContext, ShowDataActivity::class.java).apply {
//                putExtra(Intent.EXTRA_TEXT, scanResult.device)
//            }
//
//        Log.i("Bluetooth", "Entry clicked: $scanResult")
//
//        startActivity(openBluetoothDeviceIntent)


        val midiManager =
            applicationContext.getSystemService(Context.MIDI_SERVICE) as MidiManager

//        dataView.append("Opening device: $bluetoothDevice\n")

        midiManager.openBluetoothDevice(scanResult.device,
            { device ->
                Log.i("Bluetooth", "Device opened: $device")

                midiManager.openDevice(device.info, object : MidiManager.OnDeviceOpenedListener {

                    override fun onDeviceOpened(device: MidiDevice?) {
                        if (device == null) {
                            Log.i("Midi", "Device is null")

//                            runOnUiThread {
//                                dataView.append("Device is null\n")
//                            }

                            return
                        }


//                        device?.let {
//                            it.info.ports.forEach {
//                                runOnUiThread {
//                                    dataView.append("""
//                                        Name: ${it.name}
//                                        Type: ${it.type}
//                                        Port number: ${it.portNumber}\n
//                                    """.trimIndent())
//                                }
//                            }
//                        }

                        val outputPort = device.openOutputPort(0)

                        // TODO

                    }


                }, null)




//                listMidiDevices(dataView)
            }, Handler(Handler.Callback { msg ->
                Log.i("Bluetooth", "Message: $msg")

//                dataView.append("$msg\n")
                true
            })
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
