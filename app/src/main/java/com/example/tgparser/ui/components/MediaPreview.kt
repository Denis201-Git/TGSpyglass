package com.example.tgparser.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tgparser.model.Media
import androidx.compose.foundation.clickable


@Composable
fun MediaPreview(
    media: Media,
    onClick: () -> Unit
) {

    if (media.type != "link_preview") {
        return
    }


    Column(

        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .background(
                MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable {
                onClick()
            }
            .padding(12.dp)

    ) {


        Text(
            text = media.title ?: "",
            style = MaterialTheme.typography.titleMedium
        )


        if (media.description != null) {

            Text(
                text = media.description,

                style = MaterialTheme.typography.bodyMedium,

                modifier = Modifier.padding(
                    top = 4.dp
                )

            )

        }


        Text(
            text = media.url,

            style = MaterialTheme.typography.labelSmall,

            color = MaterialTheme.colorScheme.primary,

            modifier = Modifier.padding(
                top = 6.dp
            )

        )

    }

}