package com.example.bluetoothmiditest

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.ParcelUuid
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*


class MainActivity : AppCompatActivity() {


    companion object {
        private const val SCAN_PERIOD = 10000L
        private val MIDI_OVER_BTLE_UUID = UUID.fromString("03B80E5A-EDE8-4B33-A751-6CE34EC4C700")
    }

    private val scanFilter =
        ScanFilter.Builder().setServiceUuid(ParcelUuid(MIDI_OVER_BTLE_UUID)).build()


    private val bluetoothScanCallback = BluetoothScanCallback()


    private var currentlySelectedBluetoothDevice: BluetoothDeviceData? = null

    private var isScanning = false

    private lateinit var spinnerAdapterBluetooth: ArrayAdapter<BluetoothDeviceData>

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
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                // TODO Is it necessary to do anything here?

            }.launch(enableBtIntent)
        }

        val connectButton = findViewById<Button>(R.id.btnConnect)
        connectButton.isEnabled = false
        connectButton.setOnClickListener {
            currentlySelectedBluetoothDevice?.let {
                val openMidiDeviceIntent =
                    Intent(applicationContext, ShowDataActivity::class.java).apply {
                        putExtra(Intent.EXTRA_TEXT, it.bluetoothDevice)
                    }
                startActivity(openMidiDeviceIntent)

            }
        }

        val spinnerBluetooth = findViewById<Spinner>(R.id.spinnerBluetoothMidiDevices)

        spinnerAdapterBluetooth = ArrayAdapter<BluetoothDeviceData>(
            spinnerBluetooth.context,
            android.R.layout.simple_spinner_item
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        spinnerBluetooth.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                connectButton.isEnabled = false
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                stopScanning()
                spinnerAdapterBluetooth.getItem(position)?.let {
                    currentlySelectedBluetoothDevice = it
                    connectButton.isEnabled = true

                }
            }
        }

        spinnerBluetooth.adapter = spinnerAdapterBluetooth

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
            listOf(scanFilter),
            ScanSettings.Builder().build(),
            bluetoothScanCallback
        )
    }

    private fun stopScanning() {
        BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner.stopScan(bluetoothScanCallback)
        isScanning = false
    }


    inner class BluetoothScanCallback : ScanCallback() {

        private val foundBluetoothDevices = mutableSetOf<BluetoothDeviceData>()


        override fun onScanResult(
            callbackType: Int,
            result: ScanResult?
        ) {
            Log.i("Scanner", "Scan result. Callback type: ${callbackType}. Result: $result")
            result?.apply {
                this@MainActivity.runOnUiThread {
                    BluetoothDeviceData(device).let {
                        if (!foundBluetoothDevices.contains(it)) {
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
                        if (!foundBluetoothDevices.contains(bluetoothDeviceData)) {
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

}
