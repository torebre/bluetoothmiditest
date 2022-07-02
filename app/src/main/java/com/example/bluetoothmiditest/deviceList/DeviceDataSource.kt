package com.example.bluetoothmiditest.deviceList

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.bluetoothmiditest.BluetoothDeviceData

class DeviceDataSource {
    private val deviceLiveData = MutableLiveData(listOf<BluetoothDeviceData>())


    fun getDeviceList(): LiveData<List<BluetoothDeviceData>> {
        return deviceLiveData
    }

    fun insertDevice(bluetoothDeviceData: BluetoothDeviceData) {
        val currentList = deviceLiveData.value

        if (currentList == null) {
            deviceLiveData.postValue(listOf(bluetoothDeviceData))
        } else if (!currentList.contains(bluetoothDeviceData)) {
            val updateList = currentList.toMutableList()

            updateList.add(0, bluetoothDeviceData)
            deviceLiveData.postValue(updateList)
        }
    }


    companion object {
        private var INSTANCE: DeviceDataSource? = null

        fun getDataSource(): DeviceDataSource {
            return synchronized(DeviceDataSource::class) {
                INSTANCE ?: DeviceDataSource().also { INSTANCE = it }
            }
        }

    }


}
