package com.example.tgparser.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.tgparser.data.preferences.MediaSettings
import com.example.tgparser.model.Media


@Composable
fun MediaPreview(
    media: Media,
    mediaSettings: MediaSettings,
    onClick: () -> Unit
) {

    if (!mediaSettings.isEnabled(media.type)) {
        return
    }

    when (media.type) {

        "photo" -> PhotoPreview(
            media = media,
            onClick = onClick
        )

        "video" -> VideoPreview(
            media = media,
            onClick = onClick
        )

        "file", "document" -> FilePreview(
            media = media,
            onClick = onClick
        )

        "link_preview" -> LinkPreview(
            media = media,
            onClick = onClick
        )

    }

}


@Composable
private fun PhotoPreview(
    media: Media,
    onClick: () -> Unit
) {

    val image = media.thumbUrl ?: media.url

    if (image.isBlank()) {
        return
    }

    AsyncImage(
        model = image,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .heightIn(max = 360.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    )

}


@Composable
private fun VideoPreview(
    media: Media,
    onClick: () -> Unit
) {

    val thumb = media.thumbUrl ?: ""

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {

        if (thumb.isNotBlank()) {

            AsyncImage(
                model = thumb,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
            )

        } else {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant
                    )
            )

        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(56.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {

            Icon(
                imageVector = AppIcons.Play(),
                contentDescription = "Play",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )

        }

        if (!media.duration.isNullOrBlank()) {

            Text(
                text = media.duration,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )

        }

    }

}


@Composable
private fun FilePreview(
    media: Media,
    onClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            imageVector = AppIcons.File(),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )

        Column(
            modifier = Modifier.padding(start = 12.dp)
        ) {

            Text(
                text =
                    media.title?.takeIf { it.isNotBlank() }
                        ?: media.url,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (!media.description.isNullOrBlank()) {

                Text(
                    text = media.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )

            }

        }

    }

}


@Composable
private fun LinkPreview(
    media: Media,
    onClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(onClick = onClick)
    ) {

        if (!media.thumbUrl.isNullOrBlank()) {

            AsyncImage(
                model = media.thumbUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 220.dp)
            )

        }

        Column(
            modifier = Modifier.padding(12.dp)
        ) {

            Text(
                text = media.title ?: "",
                style = MaterialTheme.typography.titleMedium
            )

            if (media.description != null) {

                Text(
                    text = media.description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )

            }

            Text(
                text = media.url,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 6.dp)
            )

        }

    }

}
