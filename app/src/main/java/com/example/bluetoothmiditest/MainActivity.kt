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
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.RecyclerView
import com.example.bluetoothmiditest.deviceList.*
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

    private var isScanning = false

    private val deviceListViewModel by viewModels<DeviceListViewModel> {
        DeviceListViewModelFactory()
    }

    private val BluetoothAdapter.isDisabled: Boolean
        get() = !isEnabled

    private lateinit var scanButton: Button

    private lateinit var tracker: SelectionTracker<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermission()

        BluetoothAdapter.getDefaultAdapter()?.takeIf { it.isDisabled }?.apply {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                // TODO Is it necessary to do anything here?

                Timber.i("Activity result: $it")

            }.launch(enableBtIntent)
        }

        val connectButton = findViewById<Button>(R.id.btnConnect)
        connectButton.isEnabled = false

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

        tracker = SelectionTracker.Builder(
            "selection",
            deviceView,
            DeviceListAdapter.KeyProvider(deviceListAdapter),
            DeviceDetailLookup(deviceView),
            StorageStrategy.createStringStorage()
        ).withSelectionPredicate(SelectionPredicates.createSelectSingleAnything()).build()
            .also { selectionTracker ->
                savedInstanceState?.let { savedState ->
                    selectionTracker.onRestoreInstanceState(savedState)
                }
                deviceListAdapter.tracker = selectionTracker

                selectionTracker.addObserver(object : SelectionTracker.SelectionObserver<String>() {

                    override fun onSelectionChanged() {
                        super.onSelectionChanged()
                        connectButton.isEnabled = !tracker.selection.isEmpty
                    }
                })
            }

        connectButton.setOnClickListener {
            tracker.selection.let { bluetoothDeviceSelection ->
                if (bluetoothDeviceSelection.isEmpty) {
                    Timber.w("Connect button pressed when device selection is empty")
                    return@let
                }
                DeviceDataSource.getDataSource()
                    .getDeviceList().value?.find { it.bluetoothDevice.address == bluetoothDeviceSelection.first() }
                    ?.let {
                        // Stop the scanning and open an activity that shows the MIDI data from
                        // the selected MIDI Bluetooth device
                        toggleScanningAndUpdateButtonText(false)
                        val openMidiDeviceIntent =
                            Intent(applicationContext, ShowDataActivity::class.java).apply {
                                putExtra(Intent.EXTRA_TEXT, it.bluetoothDevice)
                            }
                        startActivity(openMidiDeviceIntent)
                    }
            }
        }
    }

    private fun toggleScanningAndUpdateButtonText() {
        toggleScanningAndUpdateButtonText(!isScanning)
    }

    private fun toggleScanningAndUpdateButtonText(doScan: Boolean) {
        if (doScan == isScanning) {
            return
        }

        if (doScan) {
            Timber.i("Starting scanning")

            BluetoothAdapter.getDefaultAdapter()?.let { scanLeDevices(it) }
            scanButton.setText(R.string.stop)
        } else {
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

        override fun onScanResult(
            callbackType: Int,
            result: ScanResult?
        ) {
            Timber.i("Scan result. Callback type: ${callbackType}. Result: $result")
            result?.apply {
                this@MainActivity.runOnUiThread {
                    BluetoothDeviceData(device).let {
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
