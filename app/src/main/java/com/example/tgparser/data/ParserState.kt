package com.example.tgparser.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object ParserState {

    var isBusy by mutableStateOf(false)
        private set

    fun lock() {
        isBusy = true
    }

    fun unlock() {
        isBusy = false
    }

}