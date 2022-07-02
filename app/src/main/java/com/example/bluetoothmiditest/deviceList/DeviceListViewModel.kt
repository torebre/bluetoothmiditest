package com.example.bluetoothmiditest.deviceList

import androidx.lifecycle.ViewModel

class DeviceListViewModel(val dataSource: DeviceDataSource): ViewModel() {

    val deviceLiveData = dataSource.getDeviceList()


}