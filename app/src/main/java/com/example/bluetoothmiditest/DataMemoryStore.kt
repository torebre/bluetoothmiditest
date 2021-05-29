package com.example.bluetoothmiditest


class DataMemoryStore : DataStore {

    private val stringBuilder: StringBuilder = StringBuilder()


    override fun store(message: String, timestamp: Long) {
        "$timestamp: $message\n".let {
            stringBuilder.append(it).append("\n")
        }
    }

    override fun getData() = stringBuilder.toString()


    override fun close() {
        // Nothing to close
    }


}