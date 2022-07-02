package com.example.bluetoothmiditest.deviceList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class DeviceListViewModelFactory : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeviceListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DeviceListViewModel(DeviceDataSource.getDataSource()) as T
        }
        throw IllegalArgumentException("Unknown view model")
    }

}