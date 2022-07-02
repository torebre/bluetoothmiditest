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
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.size
import androidx.recyclerview.widget.RecyclerView
import com.example.bluetoothmiditest.deviceList.DeviceDataSource
import com.example.bluetoothmiditest.deviceList.DeviceListAdapter
import com.example.bluetoothmiditest.deviceList.DeviceListViewModel
import com.example.bluetoothmiditest.deviceList.DeviceListViewModelFactory
import timber.log.Timber
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

    private val deviceListViewModel by viewModels<DeviceListViewModel> {
        DeviceListViewModelFactory()
    }

    private val BluetoothAdapter.isDisabled: Boolean
        get() = !isEnabled

    private lateinit var scanButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermission()

        BluetoothAdapter.getDefaultAdapter()?.takeIf { it.isDisabled }?.apply {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                // TODO Is it necessary to do anything here?

                Timber.i("Tst50: $it")

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
                toggleScanningAndUpdateButtonText(false)
                spinnerAdapterBluetooth.getItem(position)?.let {
                    currentlySelectedBluetoothDevice = it
                    connectButton.isEnabled = true
                }
            }
        }

        spinnerBluetooth.adapter = spinnerAdapterBluetooth

        scanButton = findViewById(R.id.btnScan)
        scanButton.setOnClickListener {
            toggleScanningAndUpdateButtonText()
        }

        val deviceView = findViewById<RecyclerView>(R.id.deviceView)
        val deviceListAdapter = DeviceListAdapter()
        deviceView.adapter = deviceListAdapter

        deviceListViewModel.deviceLiveData.observe(this) { liveData ->
            deviceListAdapter.submitList(liveData)
        }


    }

    private fun toggleScanningAndUpdateButtonText() {
        toggleScanningAndUpdateButtonText(!isScanning)
    }

    private fun toggleScanningAndUpdateButtonText(doScan: Boolean) {

        Timber.i("Do scan: ${doScan}")

        if(doScan == isScanning) {
            return
        }

        if(doScan) {
            Timber.i("Starting scanning")

            BluetoothAdapter.getDefaultAdapter()?.let { scanLeDevices(it) }
            scanButton.setText(R.string.stop)
        }
        else {
            Timber.i("Stopping scanning")

            stopScanning()
            scanButton.setText(R.string.scan)
        }
    }

    private fun requestPermission() {
        val permissionCheck = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        Timber.i("Has ACCESS_FINE_LOCATION permission: $permissionCheck")
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            // ask permissions here using below code
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                2
            )
        }
    }

    private fun scanLeDevices(bluetoothAdapter: BluetoothAdapter) {
        if (isScanning) {
            // Already scanning
            return
        }
        val leScanner = bluetoothAdapter.bluetoothLeScanner
        isScanning = true

        Timber.i("Start scan")

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
            Timber.i("Scan result. Callback type: ${callbackType}. Result: $result")
            result?.apply {
                this@MainActivity.runOnUiThread {
                    BluetoothDeviceData(device).let {
                        if (!foundBluetoothDevices.contains(it)) {
                            foundBluetoothDevices.add(it)
                            spinnerAdapterBluetooth.add(it)
                            spinnerAdapterBluetooth.notifyDataSetChanged()
                        }
                        DeviceDataSource.getDataSource().insertDevice(it)
                    }
                }
            }
        }

        override fun onBatchScanResults(results: List<ScanResult?>?) {
            Timber.i("Scan results. Results: $results")

            results?.apply {
                filterNotNull().forEach {
                    BluetoothDeviceData(it.device).let { bluetoothDeviceData ->
                        if (!foundBluetoothDevices.contains(bluetoothDeviceData)) {
                            foundBluetoothDevices.add(bluetoothDeviceData)
                            spinnerAdapterBluetooth.add(bluetoothDeviceData)
                        }
                        spinnerAdapterBluetooth.notifyDataSetChanged()
                        DeviceDataSource.getDataSource().insertDevice(bluetoothDeviceData)
                    }
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            isScanning = false
            Timber.e("Scan failed. Error code: $errorCode")
        }
    }

}
