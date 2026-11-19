package io.github.denis201git.tgspyglass.data

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals

class AppSnackbarVisuals(
    override val message: String,
    val isError: Boolean,
    override val actionLabel: String? = null,
    override val withDismissAction: Boolean = false,
    override val duration: SnackbarDuration = SnackbarDuration.Short
) : SnackbarVisuals

object AppSnackbarManager {

    val hostState = SnackbarHostState()

    suspend fun show(
        message: String,
        isError: Boolean = false
    ) {
        hostState.showSnackbar(
            AppSnackbarVisuals(
                message = message,
                isError = isError
            )
        )
    }

}
