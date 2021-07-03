package com.example.bluetoothmiditest

object MidiMessageTestData {

    val testNoteOnMessage = MidiTestMessage(1, 3, listOf(1, -112, 72, 94, 18, -38, 62, -79, -33, -13).let {
        ByteArray(it.size) { index -> it[index].toByte() }
    }, 268142076942866)


    val testNoteOffMessage = MidiTestMessage(1, 3, listOf(1, -128, 72, 93, -63, 44, 9, -105, 120, -12).let {
        ByteArray(it.size) { index -> it[index].toByte() }
    }, 268798767213761)

}