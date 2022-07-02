package com.example.bluetoothmiditest.deviceList

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bluetoothmiditest.BluetoothDeviceData
import com.example.bluetoothmiditest.R
import timber.log.Timber

class DeviceListAdapter :
    ListAdapter<BluetoothDeviceData, DeviceListAdapter.DeviceViewHolder>(DeviceListDiffCallback) {

    var tracker: SelectionTracker<String>? = null

//    init {
//        setHasStableIds(true)
//    }

//    override fun getItem(position: Int): BluetoothDeviceData {
//       DeviceDataSource.getDataSource().getDeviceList().value
//    }

    class DeviceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val deviceItemTextView: TextView = view.findViewById(R.id.deviceItemText)
        private var currentDevice: BluetoothDeviceData? = null


        fun bind(deviceData: BluetoothDeviceData, isActivated: Boolean) {

            Timber.i("Test24")

            currentDevice = deviceData
            deviceItemTextView.text = deviceData.bluetoothDevice.name
            itemView.isActivated = isActivated
        }

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<String>? {
            if (currentDevice == null) {
                return null
            }

            return object : ItemDetailsLookup.ItemDetails<String>() {
                override fun getPosition(): Int {
                    return bindingAdapterPosition
                }

                override fun getSelectionKey(): String? {
                    return currentDevice?.bluetoothDevice?.address
                }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {

        Timber.i("Test25")

        return DeviceViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.device_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {

        Timber.i("Test26")

        tracker?.let { selectionTracker ->
            getItem(position).let {
                holder.bind(it, selectionTracker.isSelected(it.bluetoothDevice.address))
            }
        }

    }

    fun getPosition(key: String): Int? {
        return DeviceDataSource.getDataSource().getDeviceList()
            .value?.indexOfFirst { it.bluetoothDevice.address == key }
    }


    class MyItemKeyProvider(private val rvAdapter: DeviceListAdapter) :
        ItemKeyProvider<String>(SCOPE_CACHED) {
        override fun getKey(position: Int): String =
            rvAdapter.getItem(position).bluetoothDevice.address

        override fun getPosition(key: String): Int {
            val position = rvAdapter.getPosition(key)

            Timber.i("Test60: ${position}")

            return position ?: RecyclerView.NO_POSITION
        }
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