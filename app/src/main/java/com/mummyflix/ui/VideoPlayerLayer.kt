package com.mummyflix.ui

import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.mummyflix.R
import com.mummyflix.model.ViewState
import com.mummyflix.viewmodel.MummyFlixViewModel

/**
 * One ExoPlayer instance for the whole app. It is placed inside the preview
 * window in every OSD screen and expanded to the full display in fullscreen
 * mode, so switching screens never interrupts playback.
 */
@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerLayer(
    viewModel: MummyFlixViewModel,
    modifier: Modifier = Modifier,
    fullscreen: Boolean = false
) {
    val videoUrl = viewModel.videoUrl.value
    val isRadioMode = viewModel.isRadioMode.value
    val viewState = viewModel.viewState.value
    val currentChannel = viewModel.activeList.value.getOrNull(viewModel.currentIndex.value)
    val settings = viewModel.appSettings.value
    val isLoading = viewModel.isLoading.value
    val noSignal = viewModel.showNoSignal.value

    val resizeMode = if (settings.aspectRatio.startsWith("Full")) {
        AspectRatioFrameLayout.RESIZE_MODE_FILL
    } else {
        AspectRatioFrameLayout.RESIZE_MODE_FIT
    }

    Box(
        modifier = modifier
            .clipToBounds()
            .background(Color.Black)
    ) {
        AndroidVideoPlayer(
            videoUrl = videoUrl,
            resizeMode = resizeMode,
            onVideoPlaying = viewModel::onVideoPlaying,
            onVideoError = viewModel::onVideoError
        )

        if (isRadioMode) {
            RadioModeScreen(channelName = currentChannel?.displayName ?: "")
        }

        when {
            isLoading -> BrandSplash(
                message = stringResource(R.string.loading_channels),
                modifier = Modifier.fillMaxSize()
            )

            noSignal && viewState != ViewState.AUTOSCAN -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { NoSignalBadge() }
        }

        if (!isRadioMode && !noSignal && !isLoading && viewModel.isVideoPlaying.value) {
            PlayerWatermark(
                fullscreen = fullscreen,
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun AndroidVideoPlayer(
    videoUrl: String,
    resizeMode: Int,
    onVideoPlaying: () -> Unit,
    onVideoError: () -> Unit
) {
    val context = LocalContext.current
    val currentOnPlaying by rememberUpdatedState(onVideoPlaying)
    val currentOnError by rememberUpdatedState(onVideoError)

    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
            repeatMode = Player.REPEAT_MODE_OFF
        }
    }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) currentOnPlaying()
            }

            override fun onPlayerError(error: PlaybackException) {
                currentOnError()
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) currentOnPlaying()
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }

    LaunchedEffect(videoUrl) {
        if (videoUrl.isBlank()) {
            player.stop()
            player.clearMediaItems()
            return@LaunchedEffect
        }

        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("MummyFlix/${com.mummyflix.BuildConfig.VERSION_NAME}")
            .setConnectTimeoutMs(10_000)
            .setReadTimeoutMs(10_000)
            .setAllowCrossProtocolRedirects(true)

        val mediaSource = HlsMediaSource.Factory(dataSourceFactory)
            .setAllowChunklessPreparation(true)
            .createMediaSource(MediaItem.fromUri(videoUrl))

        player.setMediaSource(mediaSource)
        player.prepare()
        player.playWhenReady = true
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                this.player = player
                useController = false
                this.resizeMode = resizeMode
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        },
        update = { view ->
            view.resizeMode = resizeMode
        },
        modifier = Modifier.fillMaxSize()
    )
}

/** Retro set-top-box radio face, drawn inside the preview window. */
@Composable
fun RadioModeScreen(channelName: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    listOf(
                        colorResource(R.color.bg_radial_1),
                        colorResource(R.color.bg_radial_2),
                        colorResource(R.color.bg_radial_3)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .background(colorResource(R.color.bg_blue_9).copy(alpha = 0.88f), RoundedCornerShape(8.dp))
                .padding(horizontal = 26.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "♫ RADIO",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.secondary),
                letterSpacing = 3.sp
            )
            Text(
                text = channelName.take(28),
                fontSize = 14.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Row(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                repeat(7) { index -> AnimatedWaveBar(index) }
            }
        }
    }
}

@Composable
fun AnimatedWaveBar(index: Int) {
    val infinite = rememberInfiniteTransition(label = "wave")
    val baseline = 6f
    val peak = (18 + (index * 5) % 16).toFloat()
    val height by infinite.animateFloat(
        initialValue = baseline,
        targetValue = peak,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 500 + index * 90,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "waveHeight"
    )

    Box(
        modifier = Modifier
            .width(6.dp)
            .height(height.dp)
            .background(
                if (index % 2 == 0) colorResource(R.color.secondary)
                else colorResource(R.color.wave_blue),
                RoundedCornerShape(2.dp)
            )
    )
}

/** Small receiver-style "No Signal" placard, exactly like the reference OSD. */
@Composable
fun NoSignalBadge() {
    Box(
        modifier = Modifier
            .background(colorResource(R.color.wave_blue), RoundedCornerShape(2.dp))
            .border(2.dp, colorResource(R.color.border_cyan_light), RoundedCornerShape(2.dp))
            .padding(horizontal = 30.dp, vertical = 8.dp)
    ) {
        Text(
            text = stringResource(R.string.no_signal),
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.bg_blue_15),
            letterSpacing = 1.sp
        )
    }
}
