package com.example.bluetoothmiditest

import timber.log.Timber

class TimberSystemOutTree: Timber.Tree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        println("Priority: $priority. Tag: $tag. Message: $message. Throwable: $t")
    }

}