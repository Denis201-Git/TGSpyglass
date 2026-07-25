package com.example.tgparser.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tgparser.model.Message
import com.example.tgparser.utils.formatMessageDate
import com.example.tgparser.ui.components.MediaPreview
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import androidx.core.net.toUri


@Composable
fun MessageCard(
    message: Message
) {

    val context = LocalContext.current

    Column(

        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 10.dp
            )

    ) {


        Text(
            text = message.author,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )


        Text(

            text = message.text,

            style = MaterialTheme.typography.bodyLarge,

            modifier = Modifier.padding(
                top = 6.dp
            )

        )

        message.media.forEach { media ->

            MediaPreview(
                media = media,
                onClick = {

                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        media.url.toUri()
                    )

                    context.startActivity(intent)

                }
            )

        }


        Text(

            text = "👁 ${message.views}   ${formatMessageDate(message.date)}",

            style = MaterialTheme.typography.labelSmall,

            color = MaterialTheme.colorScheme.onSurfaceVariant,

            modifier = Modifier
                .padding(top = 8.dp)

        )

    }

}