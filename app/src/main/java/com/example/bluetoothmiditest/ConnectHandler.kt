package com.example.bluetoothmiditest

import android.bluetooth.le.ScanResult

interface ConnectHandler {

    fun deviceEntryClicked(scanResult: ScanResult)

}
