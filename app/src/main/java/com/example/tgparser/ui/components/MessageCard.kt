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
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import androidx.core.net.toUri
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import com.example.tgparser.ui.components.AppIcons
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.IconButton

@Composable
fun MessageCard(
    message: Message,
    versionsCount: Int,
    onPreviousVersion: (() -> Unit)? = null,
    onNextVersion: (() -> Unit)? = null
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


        Row(

            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),

            verticalAlignment = Alignment.CenterVertically

        ) {

            Text(

                text = "👁 ${message.views}   ${formatMessageDate(message.publishedAt)}",

                style = MaterialTheme.typography.labelSmall,

                color = MaterialTheme.colorScheme.onSurfaceVariant

            )

            Spacer(
                modifier = Modifier.weight(1f)
            )

            if (versionsCount > 1) {

                IconButton(

                    onClick = {
                        onPreviousVersion?.invoke()
                    },

                    enabled = message.version > 1

                ) {

                    Icon(

                        imageVector = AppIcons.KeyboardArrowLeft(),

                        contentDescription = "Previous version",

                        tint =
                            if (message.version > 1)
                                MaterialTheme.colorScheme.onSurfaceVariant
                            else
                                MaterialTheme.colorScheme.outline

                    )

                }

                Text(

                    text = "Version ${message.version} of $versionsCount",

                    style = MaterialTheme.typography.labelSmall,

                    color = MaterialTheme.colorScheme.onSurfaceVariant

                )

                IconButton(

                    onClick = {
                        onNextVersion?.invoke()
                    },

                    enabled = message.isEdited

                ) {

                    Icon(

                        imageVector = AppIcons.KeyboardArrowRight(),

                        contentDescription = "Next version",

                        tint =
                            if (message.isEdited)
                                MaterialTheme.colorScheme.onSurfaceVariant
                            else
                                MaterialTheme.colorScheme.outline

                    )

                }

            }

            if (message.isDeleted) {

                Icon(

                    imageVector = AppIcons.Delete(),

                    contentDescription = "Deleted",

                    modifier = Modifier.size(16.dp),

                    tint = MaterialTheme.colorScheme.outline

                )

            }

        }

    }

}
