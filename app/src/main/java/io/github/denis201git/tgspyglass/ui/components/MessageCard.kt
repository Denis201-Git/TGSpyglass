package io.github.denis201git.tgspyglass.ui.components

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
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
import io.github.denis201git.tgspyglass.data.AppSnackbarManager
import io.github.denis201git.tgspyglass.data.preferences.MediaSettings
import io.github.denis201git.tgspyglass.data.preferences.MessageStyle
import io.github.denis201git.tgspyglass.model.Media
import io.github.denis201git.tgspyglass.model.Message
import io.github.denis201git.tgspyglass.model.TextLink
import io.github.denis201git.tgspyglass.ui.localization.LocalStrings
import io.github.denis201git.tgspyglass.utils.formatMessageDate
import kotlinx.coroutines.launch

private val LINK_REGEX = Regex(
    """(?:https?://|www\.|t\.me/|telegram\.me/)[^\s<>"']+""" +
        """|(?<![\w@./-])(?:[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?\.)+""" +
        """(?:com|org|net|io|ru|me|dev|app|ai|co|uk|de|fr|tv|info|biz|xyz|site|online|store|blog|tech|cloud|link|page|su)""" +
        """(?:/[^\s<>"']*)?"""
)

private val TRAILING_PUNCTUATION = setOf('.', ',', ';', ':', '!', '?', ')', ']', '}', '"', '\'')

private data class LinkRange(
    val start: Int,
    val end: Int,
    val url: String
)

@Composable
private fun linkify(
    text: String,
    links: List<TextLink>
): AnnotatedString {
    val linkColor = MaterialTheme.colorScheme.primary
    return remember(text, links, linkColor) {
        buildAnnotatedString {
            var lastIndex = 0
            buildLinkRanges(text, links).forEach { range ->
                if (range.start < lastIndex) return@forEach
                append(text.substring(lastIndex, range.start))
                withLink(
                    LinkAnnotation.Url(
                        url = range.url,
                        styles = TextLinkStyles(
                            style = SpanStyle(
                                color = linkColor,
                                textDecoration = TextDecoration.Underline
                            )
                        )
                    )
                ) {
                    append(text.substring(range.start, range.end))
                }
                lastIndex = range.end
            }
            append(text.substring(lastIndex))
        }
    }
}

private fun buildLinkRanges(
    text: String,
    links: List<TextLink>
): List<LinkRange> {
    val ranges = mutableListOf<LinkRange>()

    links.forEach { link ->
        val start = link.start.coerceIn(0, text.length)
        val end = (link.start + link.length).coerceIn(start, text.length)
        if (start < end && link.url.isNotBlank()) {
            ranges += LinkRange(start, end, link.url)
        }
    }

    LINK_REGEX.findAll(text).forEach { match ->
        val raw = match.value.trimEnd { it in TRAILING_PUNCTUATION }
        if (raw.isEmpty()) return@forEach
        val start = match.range.first
        val end = start + raw.length
        if (ranges.any { start < it.end && end > it.start }) return@forEach
        val url =
            if (raw.startsWith("http://") || raw.startsWith("https://")) raw
            else "https://$raw"
        ranges += LinkRange(start, end, url)
    }

    return ranges.sortedBy { it.start }
}

@Composable
fun MessageCard(
    message: Message,
    versionsCount: Int,
    mediaSettings: MediaSettings,
    messageStyle: MessageStyle = MessageStyle.CLASSIC,
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

    var viewerVideo by remember(message.id) {
        mutableStateOf<Media?>(null)
    }

    val openMedia: (Media) -> Unit = { media ->

        when (media.type) {

            "photo" -> viewerMedia = media

            "video" -> {

                if (media.url.isBlank()) {

                    scope.launch {
                        AppSnackbarManager.show(strings.videoPlayerNotFound, isError = true)
                    }

                } else {

                    viewerVideo = media

                }

            }

            else -> openLink(context, media.url.ifBlank { message.url })

        }

    }

    val containerModifier = when (messageStyle) {

        MessageStyle.CLASSIC -> Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 8.dp,
                vertical = 4.dp
            )
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(
                horizontal = 12.dp,
                vertical = 10.dp
            )

        MessageStyle.BORDERLESS -> Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 10.dp
            )

    }

    Column(

        modifier = containerModifier

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

                text = linkify(message.text, message.links),

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

        viewerVideo?.let { media ->

            VideoViewerDialog(
                media = media,
                onDismiss = {
                    viewerVideo = null
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
