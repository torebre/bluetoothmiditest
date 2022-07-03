package com.example.bluetoothmiditest.deviceList

import android.view.LayoutInflater
import android.view.MotionEvent
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

class DeviceListAdapter :
    ListAdapter<BluetoothDeviceData, DeviceListAdapter.DeviceViewHolder>(DeviceListDiffCallback) {

    var tracker: SelectionTracker<String>? = null

    class DeviceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val deviceItemTextView: TextView = view.findViewById(R.id.deviceItemText)
        private val deviceItemAddressView: TextView = view.findViewById(R.id.bluetooth_address)
        private var currentDevice: BluetoothDeviceData? = null


        fun bind(deviceData: BluetoothDeviceData, isActivated: Boolean) {
            currentDevice = deviceData
            with(deviceData.bluetoothDevice) {
                deviceItemTextView.text = name
                deviceItemAddressView.text = address
            }
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

                /**
                 * Overriding this to avoid having the user do a long-press to
                 * be able to select something in the list, instead a click on
                 * an item should be enough.
                 *
                 * https://stackoverflow.com/questions/55494599/how-to-select-first-item-without-long-press-using-recyclerviews-selectiontracke
                 */
                override fun inSelectionHotspot(event: MotionEvent): Boolean {
                    return true
                }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        return DeviceViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.device_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
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


    /**
     * For use with the selection tracker
     */
    class KeyProvider(private val deviceListAdapter: DeviceListAdapter) :
        ItemKeyProvider<String>(SCOPE_CACHED) {

        override fun getKey(position: Int): String =
            deviceListAdapter.getItem(position).bluetoothDevice.address

        override fun getPosition(key: String) =
            deviceListAdapter.getPosition(key) ?: RecyclerView.NO_POSITION
    }


}

object DeviceListDiffCallback : DiffUtil.ItemCallback<BluetoothDeviceData>() {
    override fun areItemsTheSame(
        oldItem: BluetoothDeviceData,
        newItem: BluetoothDeviceData
    ): Boolean {
        return oldItem.bluetoothDevice.address == newItem.bluetoothDevice.address
    }

    override fun areContentsTheSame(
        oldItem: BluetoothDeviceData,
        newItem: BluetoothDeviceData
    ): Boolean {
        return oldItem == newItem
    }


}