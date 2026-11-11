package com.example.tgparser.ui.components

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.example.tgparser.data.AppSnackbarManager
import com.example.tgparser.data.preferences.MediaSettings
import com.example.tgparser.model.Media
import com.example.tgparser.model.Message
import com.example.tgparser.ui.localization.LocalStrings
import com.example.tgparser.utils.formatMessageDate
import kotlinx.coroutines.launch

private val LINK_REGEX = Regex(
    """(?:https?://|www\.|t\.me/|telegram\.me/)[^\s<>"']+""" +
        """|(?<![\w@./-])(?:[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?\.)+""" +
        """(?:com|org|net|io|ru|me|dev|app|ai|co|uk|de|fr|tv|info|biz|xyz|site|online|store|blog|tech|cloud|link|page|su)""" +
        """(?:/[^\s<>"']*)?"""
)

private val TRAILING_PUNCTUATION = setOf('.', ',', ';', ':', '!', '?', ')', ']', '}', '"', '\'')

@Composable
private fun linkify(text: String): AnnotatedString {
    val linkColor = MaterialTheme.colorScheme.primary
    return remember(text, linkColor) {
        buildAnnotatedString {
            var lastIndex = 0
            LINK_REGEX.findAll(text).forEach { match ->
                val raw = match.value.trimEnd { it in TRAILING_PUNCTUATION }
                if (raw.isEmpty()) return@forEach
                append(text.substring(lastIndex, match.range.first))
                val url =
                    if (raw.startsWith("http://") || raw.startsWith("https://")) raw
                    else "https://$raw"
                withLink(
                    LinkAnnotation.Url(
                        url = url,
                        styles = TextLinkStyles(
                            style = SpanStyle(
                                color = linkColor,
                                textDecoration = TextDecoration.Underline
                            )
                        )
                    )
                ) {
                    append(raw)
                }
                lastIndex = match.range.first + raw.length
            }
            append(text.substring(lastIndex))
        }
    }
}

@Composable
fun MessageCard(
    message: Message,
    versionsCount: Int,
    mediaSettings: MediaSettings,
    onPreviousVersion: (() -> Unit)? = null,
    onNextVersion: (() -> Unit)? = null
) {

    val context = LocalContext.current

    val strings = LocalStrings.current

    val scope = rememberCoroutineScope()

    var showCopyButton by remember(message.id) {
        mutableStateOf(false)
    }

    var viewerMedia by remember(message.id) {
        mutableStateOf<Media?>(null)
    }

    val openMedia: (Media) -> Unit = { media ->

        when (media.type) {

            "photo" -> viewerMedia = media

            "video" -> {

                if (!openVideo(context, media.url)) {

                    scope.launch {
                        AppSnackbarManager.show(strings.videoPlayerNotFound, isError = true)
                    }

                }

            }

            else -> openLink(context, media.url.ifBlank { message.url })

        }

    }

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


        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
                .clickable(enabled = message.text.isNotBlank()) {
                    showCopyButton = !showCopyButton
                }
        ) {

            Text(

                text = linkify(message.text),

                style = MaterialTheme.typography.bodyLarge,

                modifier = Modifier
                    .fillMaxWidth()

            )

            if (showCopyButton) {

                TextButton(
                    onClick = {
                        val clipboard = context.getSystemService(
                            Context.CLIPBOARD_SERVICE
                        ) as android.content.ClipboardManager

                        clipboard.setPrimaryClip(
                            ClipData.newPlainText("TGParser", message.text)
                        )

                        showCopyButton = false

                        scope.launch {
                            AppSnackbarManager.show(strings.copied)
                        }
                    }
                ) {

                    Text(text = strings.copy)

                }

            }

        }

        message.media.forEach { media ->

            MediaPreview(
                media = media,
                mediaSettings = mediaSettings,
                onClick = {
                    openMedia(media)
                }
            )

        }

        viewerMedia?.let { media ->

            PhotoViewerDialog(
                media = media,
                onDismiss = {
                    viewerMedia = null
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

private fun openVideo(context: Context, url: String): Boolean {
    if (url.isBlank()) {
        return false
    }

    return try {
        context.startActivity(
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(url.toUri(), "video/*")
            }
        )
        true
    } catch (e: ActivityNotFoundException) {
        false
    }
}

private fun openLink(context: Context, url: String): Boolean {
    if (url.isBlank()) {
        return false
    }

    return try {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, url.toUri())
        )
        true
    } catch (e: ActivityNotFoundException) {
        false
    }
}
