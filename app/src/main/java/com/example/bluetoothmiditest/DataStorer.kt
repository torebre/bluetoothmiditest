package com.example.bluetoothmiditest

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.BufferedWriter
import java.io.Closeable
import java.io.File
import java.nio.file.Files

class DataStorer(val context: Context) : Closeable {

    private val outputWriter: BufferedWriter?
//    private var storeMode = false

    init {
        outputWriter = setupOutputFile()?.let {

            Log.i("ShowData", "Output file: $it")

            Files.newBufferedWriter(it.toPath())
        }
    }


    private fun setupOutputFile(): File? {
        return context.getExternalFilesDir(null)?.let { externalFilesDir ->
            Environment.getExternalStorageState(externalFilesDir).let { storageState ->

                Log.i("ShowData", "Storage state is $storageState")

                if (storageState == Environment.MEDIA_MOUNTED) {
                    externalFilesDir.resolve("midi_output.txt")
                } else {
                    null
                }
            }
        }
    }


//    fun store(store: Boolean) {
//        storeMode = store
//    }

//    fun isStoring(): Boolean {
//        return storeMode && outputWriter != null
//    }


    fun store(message: String, timestamp: Long) {
        outputWriter?.run {
            write("$timestamp: $message\n")

            // TODO Not necessary to flush data all the time
            flush()
        }
    }

    override fun close() {
        outputWriter?.close()
    }


}