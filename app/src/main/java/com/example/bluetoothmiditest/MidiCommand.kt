package com.example.bluetoothmiditest


enum class CommandType {
    System,
    Channel,
    Data
}


enum class MidiCommand(val type: CommandType) {
    SysEx(CommandType.System),
    TimeCode(CommandType.System),
    SongPos(CommandType.System),
    SongSel(CommandType.System),
    F4(CommandType.System),
    F5(CommandType.System),
    TuneReq(CommandType.System),
    EndSysex(CommandType.System),
    TimingClock(CommandType.System),
    F9(CommandType.System),
    Start(CommandType.System),
    Continue(CommandType.System),
    Stop(CommandType.System),
    FD(CommandType.System),
    ActiveSensing(CommandType.System),
    Reset(CommandType.System),
    NoteOff(CommandType.Channel),
    NoteOn(CommandType.Channel),
    PolyTouch(CommandType.Channel),
    Control(CommandType.Channel),
    Program(CommandType.Channel),
    Pressure(CommandType.Channel),
    Bend(CommandType.Channel),
    Data(CommandType.Data)
}

val NUMBER_OF_SYSTEM_COMMANDS = MidiCommand.values().count { it.type == CommandType.System }
