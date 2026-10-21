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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
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
 * Single ExoPlayer instance that fills the screen. Every OSD screen draws its
 * panels on top of it, so switching between list/menu/fullscreen never
 * re-creates the player or interrupts playback.
 */
@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerLayer(viewModel: MummyFlixViewModel) {
    val videoUrl = viewModel.videoUrl.value
    val isRadioMode = viewModel.isRadioMode.value
    val viewState = viewModel.viewState.value
    val currentChannel = viewModel.activeList.value.getOrNull(viewModel.currentIndex.value)

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidVideoPlayer(
            videoUrl = videoUrl,
            onVideoPlaying = viewModel::onVideoPlaying,
            onVideoError = viewModel::onVideoError
        )

        if (isRadioMode) {
            RadioModeOverlay(channelName = currentChannel?.displayName ?: "")
        }

        if (viewModel.showNoSignal.value && viewState != ViewState.AUTOSCAN) {
            NoSignalOverlay(isLoading = viewModel.isLoading.value)
        }

        // MummyFlix branding watermark - only show when video is playing and not in radio mode
        if (viewModel.isVideoPlaying.value && !viewModel.showNoSignal.value && !isRadioMode && viewState != ViewState.AUTOSCAN) {
            MummyFlixBranding()
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun AndroidVideoPlayer(
    videoUrl: String,
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
            .setUserAgent("MummyFlix/1.0")
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
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun RadioModeOverlay(channelName: String) {
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

@Composable
fun NoSignalOverlay(isLoading: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isLoading) stringResource(R.string.loading_channels)
                else stringResource(R.string.no_signal),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.text_cyan),
                letterSpacing = 1.sp
            )
            if (!isLoading) {
                Text(
                    text = "Press MENU to open the channel list",
                    fontSize = 11.sp,
                    color = colorResource(R.color.border_cyan_light),
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

