package com.example.tgparser.data

import androidx.compose.material3.SnackbarHostState

object AppSnackbarManager {

    val hostState = SnackbarHostState()

    suspend fun show(message: String) {
        hostState.showSnackbar(message)
    }

}