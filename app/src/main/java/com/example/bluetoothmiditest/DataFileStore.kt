package com.example.bluetoothmiditest

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.BufferedWriter
import java.io.File
import java.nio.file.Files


/**
 * @deprecated
 */
class DataFileStore(val context: Context) {
    private val outputWriter: BufferedWriter?
    private val stringBuilder: StringBuilder = StringBuilder()

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


    fun store(message: String, timestamp: Long) {
        outputWriter?.run {
            "$timestamp: $message\n".let {
                write(it)
                stringBuilder.append(it).append("\n")
            }

            // TODO Not necessary to flush data all the time
            flush()
        }
    }

     fun getData() = stringBuilder.toString()


     fun close() {
        outputWriter?.close()
    }


}