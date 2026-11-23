package io.github.denis201git.tgspyglass.ui.components

import android.os.SystemClock
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import io.github.denis201git.tgspyglass.data.AppSnackbarManager
import io.github.denis201git.tgspyglass.model.Media
import io.github.denis201git.tgspyglass.ui.localization.LocalStrings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun VideoViewerDialog(
    media: Media,
    onDismiss: () -> Unit
) {
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()
    val videoViewRef = remember { mutableStateOf<VideoView?>(null) }
    val isPlaying = remember { mutableStateOf(true) }
    val controlsVisible = remember { mutableStateOf(true) }
    val lastDoubleTapAt = remember { mutableLongStateOf(0L) }
    val position = remember { mutableIntStateOf(0) }
    val duration = remember { mutableIntStateOf(0) }

    fun seekBy(deltaMillis: Int) {
        val view = videoViewRef.value ?: return
        val total = view.duration
        if (total <= 0) return
        val target = (view.currentPosition + deltaMillis).coerceIn(0, total)
        view.seekTo(target)
        position.intValue = target
    }

    fun togglePlay() {
        val view = videoViewRef.value ?: return
        if (view.isPlaying) {
            view.pause()
            isPlaying.value = false
        } else {
            view.start()
            isPlaying.value = true
        }
    }

    DisposableEffect(media.url) {
        onDispose {
            videoViewRef.value?.stopPlayback()
            videoViewRef.value = null
        }
    }

    LaunchedEffect(media.url) {
        while (true) {
            videoViewRef.value?.let { view ->
                position.intValue = view.currentPosition.coerceAtLeast(0)
            }
            delay(300)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { context ->
                    VideoView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        videoViewRef.value = this

                        setOnPreparedListener { player ->
                            duration.intValue = player.duration
                            position.intValue = 0
                            isPlaying.value = true
                            controlsVisible.value = true
                            start()
                        }
                        setOnCompletionListener {
                            isPlaying.value = false
                            position.intValue = duration.intValue
                            controlsVisible.value = true
                        }
                        setOnErrorListener { _, _, _ ->
                            scope.launch {
                                AppSnackbarManager.show(
                                    strings.videoPlayerNotFound,
                                    isError = true
                                )
                            }
                            onDismiss()
                            true
                        }

                        setVideoURI(media.url.toUri())
                        requestFocus()
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                scope.launch {
                                    val timeout = ViewConfiguration.getDoubleTapTimeout().toLong()
                                    delay(timeout)
                                    if (SystemClock.uptimeMillis() - lastDoubleTapAt.longValue >= timeout) {
                                        controlsVisible.value = !controlsVisible.value
                                    }
                                }
                            },
                            onDoubleTap = { offset ->
                                lastDoubleTapAt.longValue = SystemClock.uptimeMillis()
                                if (offset.x < size.width / 2) {
                                    seekBy(-5_000)
                                } else {
                                    seekBy(5_000)
                                }
                            }
                        )
                    }
            )

            if (controlsVisible.value) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = AppIcons.Close(),
                        contentDescription = strings.close,
                        tint = Color.White
                    )
                }

                IconButton(
                    onClick = { togglePlay() },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.45f))
                ) {
                    Icon(
                        imageVector = if (isPlaying.value) AppIcons.Pause() else AppIcons.Play(),
                        contentDescription = if (isPlaying.value) strings.pause else strings.play,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.55f))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Column {
                        if (duration.intValue > 0) {
                            Slider(
                                value = position.intValue.toFloat(),
                                valueRange = 0f..duration.intValue.toFloat(),
                                onValueChange = { value ->
                                    videoViewRef.value?.seekTo(value.toInt())
                                    position.intValue = value.toInt()
                                }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = formatVideoTime(position.intValue), color = Color.White)
                            Text(text = formatVideoTime(duration.intValue), color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

private fun formatVideoTime(millis: Int): String {
    val totalSeconds = millis / 1000
    return "%02d:%02d".format(totalSeconds / 60, totalSeconds % 60)
}
