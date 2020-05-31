package com.example.bluetoothmiditest

import android.app.Application
import org.koin.core.context.startKoin
import org.koin.dsl.module


class BluetoothMidiApplication: Application() {

    private val messageModule = module {

        single<MidiMessageHandler> { MidiMessageHandlerImpl() }


    }


    override fun onCreate() {
        super.onCreate()

        startKoin {
            printLogger()
//            androidContext(this@BluetoothMidiApplication)
            modules(messageModule)
        }
    }






}