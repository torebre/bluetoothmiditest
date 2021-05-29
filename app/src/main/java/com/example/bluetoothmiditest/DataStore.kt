package com.example.bluetoothmiditest

import java.io.Closeable

interface DataStore: Closeable {


    fun store(message: String, timestamp: Long)

    fun getData(): String

}