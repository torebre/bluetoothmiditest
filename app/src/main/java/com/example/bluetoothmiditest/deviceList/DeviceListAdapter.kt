package com.example.bluetoothmiditest.deviceList

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bluetoothmiditest.BluetoothDeviceData
import com.example.bluetoothmiditest.R
import timber.log.Timber

class DeviceListAdapter :
    ListAdapter<BluetoothDeviceData, DeviceListAdapter.DeviceViewHolder>(DeviceListDiffCallback) {


    class DeviceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val deviceItemTextView: TextView = view.findViewById(R.id.deviceItemText)
        private var currentDevice: BluetoothDeviceData? = null


        fun bind(deviceData: BluetoothDeviceData) {

            Timber.i("Test24")

            currentDevice = deviceData
            deviceItemTextView.text = deviceData.bluetoothDevice.name
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {

        Timber.i("Test25")

        return DeviceViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.device_item, parent, false))
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {

        Timber.i("Test26")

        holder.bind(getItem(position))
    }


}

object DeviceListDiffCallback : DiffUtil.ItemCallback<BluetoothDeviceData>() {
    override fun areItemsTheSame(
        oldItem: BluetoothDeviceData,
        newItem: BluetoothDeviceData
    ): Boolean {
        // TODO Is this the correct property to check here?
        return oldItem.bluetoothDevice.address == newItem.bluetoothDevice.address
    }

    override fun areContentsTheSame(
        oldItem: BluetoothDeviceData,
        newItem: BluetoothDeviceData
    ): Boolean {
        return oldItem == newItem
    }


}