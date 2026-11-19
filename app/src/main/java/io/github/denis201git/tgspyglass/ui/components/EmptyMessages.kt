package io.github.denis201git.tgspyglass.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.denis201git.tgspyglass.ui.localization.LocalStrings

@Composable
fun EmptyMessages() {

    val strings = LocalStrings.current

    Column(
        modifier = Modifier.fillMaxSize(),

        verticalArrangement = Arrangement.Center,

        horizontalAlignment = Alignment.CenterHorizontally

    ) {

        Text(
            text = strings.noMessages,
            style = MaterialTheme.typography.headlineSmall
        )


        Text(
            text = strings.noMessagesText,
            style = MaterialTheme.typography.bodyMedium
        )

    }

}