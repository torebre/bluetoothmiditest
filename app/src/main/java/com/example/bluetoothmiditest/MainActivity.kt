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
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*


class MainActivity : AppCompatActivity(), ConnectHandler {


    companion object {
        private const val SCAN_PERIOD = 10000L
        private val MIDI_OVER_BTLE_UUID = UUID.fromString("03B80E5A-EDE8-4B33-A751-6CE34EC4C700")
    }

    //    private lateinit var viewAdapter: MidiDeviceAdapter
    private lateinit var viewAdapter: BluetoothScanResults

    private var isScanning = false

    private val BluetoothAdapter.isDisabled: Boolean
        get() = !isEnabled


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        var viewManager = LinearLayoutManager(this)
//        val bluetoothDevices = listBluetoothDevices()
//        viewAdapter = BluetoothDeviceAdapter(bluetoothDevices, this)
//        findViewById<RecyclerView>(R.id.bluetoothDevices).apply {
//            layoutManager = viewManager
//            adapter = viewAdapter
//        }

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

        val viewManager = LinearLayoutManager(this)
        viewAdapter = BluetoothScanResults(this)
        findViewById<RecyclerView>(R.id.bluetoothDevices).apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }

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


//    private fun listBluetoothDevices(): List<BluetoothDevice> {
//        val defaultAdapter = BluetoothAdapter.getDefaultAdapter() ?: return mutableListOf()
//
//        if (!defaultAdapter.isEnabled) {
//            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
//            startActivityForResult(enableBtIntent, 1)
//        }
//
//        return defaultAdapter.bondedDevices.toList()
//    }

//    private fun scanBluetoothDevices() {
//        val defaultAdapter = BluetoothAdapter.getDefaultAdapter() ?: return mutableListOf()
//
//        if (!defaultAdapter.isEnabled) {
//            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
//            startActivityForResult(enableBtIntent, 1)
//        }
//
//        return defaultAdapter.bondedDevices.toList()
//
//
//    }

    override fun deviceEntryClicked(scanResult: ScanResult) {
        stopScanning()

        val openBluetoothDeviceIntent =
            Intent(applicationContext, ShowDataActivity::class.java).apply {
                putExtra(Intent.EXTRA_TEXT, scanResult.device)
            }

        Log.i("Bluetooth", "Entry clicked: $scanResult")

        startActivity(openBluetoothDeviceIntent)
    }


    private fun stopScanning() {
        BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner.stopScan(object : ScanCallback() {
            // Do nothing

        })
        isScanning = false


    }

}
