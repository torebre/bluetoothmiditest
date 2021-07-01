package com.example.bluetoothmiditest

data class MidiTestMessage(val offset: Int, val count: Int, val data: ByteArray, val timestamp: Long) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MidiTestMessage

        if (offset != other.offset) return false
        if (count != other.count) return false
        if (!data.contentEquals(other.data)) return false
        if (timestamp != other.timestamp) return false

        return true
    }

    override fun hashCode(): Int {
        var result = offset
        result = 31 * result + count
        result = 31 * result + data.contentHashCode()
        result = 31 * result + timestamp.hashCode()
        return result
    }
}
