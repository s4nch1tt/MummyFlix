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
 * One ExoPlayer instance for the whole app. In receiver mode it is sized to the
 * preview window; in fullscreen mode it fills the display. The Compose OSD is
 * always drawn on top, so opening a menu never tears down the stream.
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
    val isPaused = viewModel.isPaused.value
    val currentChannel = viewModel.activeList.value.getOrNull(viewModel.currentIndex.value)

    Box(modifier = modifier.background(Color.Black)) {
        AndroidVideoPlayer(
            videoUrl = videoUrl,
            isPaused = isPaused,
            onVideoPlaying = viewModel::onVideoPlaying,
            onVideoError = viewModel::onVideoError
        )

        if (isRadioMode) {
            RadioModeOverlay(
                channelName = currentChannel?.displayName ?: "",
                fullscreen = fullscreen
            )
        }

        if (viewModel.showNoSignal.value && viewState != ViewState.AUTOSCAN) {
            NoSignalOverlay(isLoading = viewModel.isLoading.value)
        }

        if (viewModel.isVideoPlaying.value &&
            !viewModel.showNoSignal.value &&
            !isRadioMode &&
            viewState != ViewState.AUTOSCAN
        ) {
            PlayerWatermark(
                fullscreen = fullscreen,
                modifier = Modifier.fillMaxSize()
            )
        }

        if (isPaused && viewModel.isVideoPlaying.value && !isRadioMode && !viewModel.showNoSignal.value) {
            PauseBadge(modifier = Modifier.align(Alignment.Center))
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun AndroidVideoPlayer(
    videoUrl: String,
    isPaused: Boolean,
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
        player.playWhenReady = !isPaused
    }

    LaunchedEffect(isPaused, videoUrl) {
        if (videoUrl.isBlank()) return@LaunchedEffect
        if (isPaused) player.pause() else player.play()
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                this.player = player
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                setShutterBackgroundColor(android.graphics.Color.BLACK)
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
fun RadioModeOverlay(
    channelName: String,
    fullscreen: Boolean = false,
    modifier: Modifier = Modifier
) {
    val markSize = if (fullscreen) 72.dp else 34.dp
    val titleSize = if (fullscreen) 24.sp else 13.sp
    val stationSize = if (fullscreen) 15.sp else 10.sp

    Box(
        modifier = modifier
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
                .background(
                    colorResource(R.color.bg_blue_9).copy(alpha = 0.88f),
                    RoundedCornerShape(8.dp)
                )
                .padding(
                    horizontal = if (fullscreen) 30.dp else 18.dp,
                    vertical = if (fullscreen) 18.dp else 10.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(if (fullscreen) 8.dp else 4.dp)
        ) {
            MummyFlixMark(markSize)
            Text(
                text = stringResource(R.string.brand_wordmark),
                fontSize = titleSize,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = if (fullscreen) 3.sp else 1.5.sp
            )
            Text(
                text = stringResource(R.string.radio_playing),
                fontSize = stationSize,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.brand_gold),
                letterSpacing = if (fullscreen) 2.sp else 1.sp
            )
            Text(
                text = channelName.take(if (fullscreen) 40 else 22),
                fontSize = if (fullscreen) 14.sp else 9.sp,
                color = colorResource(R.color.text_cyan_light),
                textAlign = TextAlign.Center
            )
            Row(
                modifier = Modifier.padding(top = if (fullscreen) 6.dp else 2.dp),
                horizontalArrangement = Arrangement.spacedBy(if (fullscreen) 5.dp else 3.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                repeat(if (fullscreen) 7 else 5) { index ->
                    AnimatedWaveBar(index = index, fullscreen = fullscreen)
                }
            }
        }
    }
}

@Composable
fun AnimatedWaveBar(index: Int, fullscreen: Boolean = false) {
    val infinite = rememberInfiniteTransition(label = "wave")
    val baseline = if (fullscreen) 8f else 4f
    val peak = (if (fullscreen) 22f else 12f) + (index * 4) % 12
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
            .width(if (fullscreen) 6.dp else 3.dp)
            .height(height.dp)
            .background(
                if (index % 2 == 0) colorResource(R.color.secondary)
                else colorResource(R.color.wave_blue),
                RoundedCornerShape(2.dp)
            )
    )
}

@Composable
fun NoSignalOverlay(isLoading: Boolean, modifier: Modifier = Modifier) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f)),
        contentAlignment = Alignment.Center
    ) {
        val large = maxWidth > 700.dp
        Box(
            modifier = Modifier
                .background(
                    colorResource(R.color.border_cyan_dark),
                    RoundedCornerShape(2.dp)
                )
                .padding(
                    horizontal = if (large) 16.dp else 10.dp,
                    vertical = if (large) 8.dp else 5.dp
                )
        ) {
            Text(
                text = if (isLoading) stringResource(R.string.loading_channels)
                else stringResource(R.string.no_signal),
                fontSize = if (large) 20.sp else 13.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.text_dark_blue),
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun PauseBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(50))
            .padding(horizontal = 22.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "❚❚",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            letterSpacing = 2.sp
        )
    }
}
