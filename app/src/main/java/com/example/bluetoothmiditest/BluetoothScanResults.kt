package com.example.bluetoothmiditest

import android.bluetooth.BluetoothClass
import android.bluetooth.le.ScanResult
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import timber.log.Timber

class BluetoothScanResults(private val connectHandler: ConnectHandler
) :
    RecyclerView.Adapter<BluetoothScanResults.ViewHolder>() {

    private val deviceList = mutableListOf<ScanResult>()


    class ViewHolder(
        view: View, private val deviceList: List<ScanResult>,
        private val connectHandler: ConnectHandler
    ) : RecyclerView.ViewHolder(view), View.OnClickListener {
        val textView: TextView = view.findViewById(R.id.textView)

        init {
            textView.setOnClickListener(this)
        }

        override fun onClick(view: View) {

            Timber.i("Device entry clicked")

            connectHandler.deviceEntryClicked(deviceList[adapterPosition])
        }

    }



    fun addScanResult(scanResult: ScanResult) {
        if(!deviceList.contains(scanResult)) {
            deviceList.add(scanResult)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.row_element, parent, false),
        deviceList, connectHandler
    )

    override fun getItemCount() = deviceList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = deviceList[position].let {
            """
                Name: ${it.device.name}
                Address: ${it.device.address}
                Bluetooth class: ${getMajorClass(it.device.bluetoothClass)}
            """.trimMargin()
        }
    }

    private fun getMajorClass(bluetoothClass: BluetoothClass) =
        when (bluetoothClass.majorDeviceClass) {
            BluetoothClass.Device.Major.AUDIO_VIDEO -> "Audio/video"
            BluetoothClass.Device.Major.MISC -> "Misc"
            BluetoothClass.Device.Major.COMPUTER -> "Computer"
            BluetoothClass.Device.Major.PHONE -> "Phone"
            BluetoothClass.Device.Major.NETWORKING -> "Networking"
            BluetoothClass.Device.Major.PERIPHERAL -> "Peripheral"
            BluetoothClass.Device.Major.IMAGING -> "Imaging"
            BluetoothClass.Device.Major.WEARABLE -> "Wearable"
            BluetoothClass.Device.Major.TOY -> "Toy"
            BluetoothClass.Device.Major.HEALTH -> "Health"
            BluetoothClass.Device.Major.UNCATEGORIZED -> "Uncategorized"
            else -> "Unknown"
        }



    }