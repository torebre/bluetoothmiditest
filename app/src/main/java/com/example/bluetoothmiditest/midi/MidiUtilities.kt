package com.example.bluetoothmiditest.midi

object MidiUtilities {

    fun pitchToNoteAndOctave(pitch: Int): Pair<String, Int> {
        val remainder = pitch.rem(12)
        val noteType = when (remainder) {
            9 -> "A"
            10 -> "A#"
            11 -> "H"
            0 -> "C"
            1 -> "C#"
            2 -> "D"
            3 -> "D#"
            4 -> "E"
            5 -> "F"
            6 -> "F#"
            7 -> "G"
            8 -> "G#"
            else -> throw IllegalArgumentException("Unhandled pitch: $pitch")
        }

        return Pair(noteType, pitch.minus(remainder).div(12))
    }





}