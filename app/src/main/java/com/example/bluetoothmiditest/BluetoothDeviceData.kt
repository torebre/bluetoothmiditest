package com.example.bluetoothmiditest

import android.bluetooth.BluetoothDevice

data class BluetoothDeviceData(val bluetoothDevice: BluetoothDevice) {

    override fun toString(): String {
        return "Name: ${bluetoothDevice.name}. Address: ${bluetoothDevice.address}"
    }

}