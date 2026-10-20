package com.mummyflix.ui

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.MediaItem
import androidx.media3.exoplayer.source.hls.HlsMediaSource
import androidx.media3.exoplayer.upstream.DefaultHttpDataSource
import androidx.media3.ui.PlayerView
import com.mummyflix.model.ViewState
import com.mummyflix.viewmodel.MummyFlixViewModel
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerLayer(
    viewModel: MummyFlixViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val videoUrl = viewModel.videoUrl.value
    val isRadioMode = viewModel.isRadioMode.value
    val viewState = viewModel.viewState.value
    val currentChannelName = viewModel.activeList.value.getOrNull(viewModel.currentIndex.value)?.name ?: ""

    // PlayerView container that fills the preview area in LIST/MENU/SUBMENU mode
    // or full screen in FULLSCREEN mode
    val containerWidth = when (viewState) {
        ViewState.LIST, ViewState.MENU, ViewState.SUBMENU -> 55.5f
        ViewState.AUTOSCAN -> 0f
        ViewState.FULLSCREEN -> 100f
    }

    val containerHeight = when (viewState) {
        ViewState.LIST, ViewState.MENU, ViewState.SUBMENU -> 52f
        ViewState.AUTOSCAN -> 0f
        ViewState.FULLSCREEN -> 100f
    }

    val containerLeft = when (viewState) {
        ViewState.LIST, ViewState.MENU, ViewState.SUBMENU -> 42.5f
        else -> 0f
    }

    val containerTop = when (viewState) {
        ViewState.LIST, ViewState.MENU, ViewState.SUBMENU -> 13f
        else -> 0f
    }

    Box(
        modifier = modifier
            .width(if (viewState == ViewState.FULLSCREEN || viewState == ViewState.AUTOSCAN) 0.dp else containerWidth.dp)
            .height(if (viewState == ViewState.FULLSCREEN || viewState == ViewState.AUTOSCAN) 0.dp else containerHeight.dp)
            .offset(x = containerLeft.dp, y = containerTop.dp)
            .background(Color.Black)
            .border(width = if (viewState == ViewState.FULLSCREEN) 0.dp else 3.dp, color = com.mummyflix.ui.colorResource(com.mummyflix.R.color.border_cyan_light))
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(0.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (viewState != ViewState.FULLSCREEN && viewState != ViewState.AUTOSCAN) {
            AndroidVideoPlayer(
                videoUrl = videoUrl,
                isRadioMode = isRadioMode,
                currentChannelName = currentChannelName,
                onVideoPlaying = { viewModel.onVideoPlaying() },
                onVideoError = { viewModel.onVideoError() }
            )
        }
    }

    // Fullscreen video player
    if (viewState == ViewState.FULLSCREEN) {
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidVideoPlayer(
                videoUrl = videoUrl,
                isRadioMode = isRadioMode,
                currentChannelName = currentChannelName,
                onVideoPlaying = { viewModel.onVideoPlaying() },
                onVideoError = { viewModel.onVideoError() },
                isFullscreen = true
            )
        }
    }

    // Radio mode overlay
    if (isRadioMode && viewState != ViewState.FULLSCREEN) {
        RadioModeOverlay(channelName = currentChannelName)
    }

    // No signal overlay
    if (viewModel.showNoSignal.value && viewState != ViewState.FULLSCREEN && viewState != ViewState.AUTOSCAN) {
        NoSignalOverlay()
    }
}

@Composable
fun AndroidVideoPlayer(
    videoUrl: String,
    isRadioMode: Boolean,
    currentChannelName: String,
    onVideoPlaying: () -> Unit,
    onVideoError: () -> Unit,
    isFullscreen: Boolean = false
) {
    val context = LocalContext.current
    val player = remember { createExoPlayer(context) }

    DisposableEffect(Unit) {
        onDispose { player.release() }
    }

    LaunchedEffect(videoUrl) {
        if (videoUrl.isNotEmpty()) {
            playVideo(player, videoUrl, context)
        }
    }

    // Observe playback state
    LaunchedEffect(player.playWhenReady, player.playbackState) {
        when (player.playbackState) {
            androidx.media3.exoplayer.ExoPlayer.STATE_READY -> {
                if (player.playWhenReady) {
                    onVideoPlaying()
                }
            }
            androidx.media3.exoplayer.ExoPlayer.STATE_ENDED -> {
                // Handle end
            }
        }
    }

    // Handle errors
    LaunchedEffect(player.playbackState) {
        if (player.playbackState == androidx.media3.exoplayer.ExoPlayer.STATE_IDLE) {
            val error = player.playerError
            if (error != null) {
                onVideoError()
            }
        }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                this.player = player
                useController = false
                resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

private fun createExoPlayer(context: Context): ExoPlayer {
    return ExoPlayer.Builder(context)
        .build()
}

private fun playVideo(player: ExoPlayer, url: String, context: Context) {
    val mediaItem = MediaItem.fromUri(url)
    val dataSourceFactory = DefaultHttpDataSource.Factory()
        .setUserAgent("MummyFlix/1.0")
        .setConnectTimeoutMs(8000)
        .setReadTimeoutMs(8000)
    val hlsMediaSource = HlsMediaSource.Factory(dataSourceFactory)
        .createMediaSource(mediaItem)

    player.setMediaSource(hlsMediaSource)
    player.prepare()
    player.playWhenReady = true
}

@Composable
fun RadioModeOverlay(channelName: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    center = androidx.compose.ui.graphics.Center(0.5f, 0.5f),
                    radius = 100.dp,
                    colors = listOf(
                        colorResource(com.mummyflix.R.color.bg_radial_1),
                        colorResource(com.mummyflix.R.color.bg_radial_2),
                        colorResource(com.mummyflix.R.color.bg_radial_3)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 10.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Rgb(0x04, 0x1f, 0x42).copy(alpha = 0.85f),
                                Color.Rgb(0x04, 0x1f, 0x42).copy(alpha = 0.85f)
                            )
                        )
                    )
                    .border(width = 2.dp, color = colorResource(com.mummyflix.R.color.border_cyan_light))
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(6.dp))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "♫ RADIO",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(com.mummyflix.R.color.secondary),
                        letterSpacing = 3.sp
                    )
                    Text(
                        text = channelName.take(20),
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    // Radio waves animation
                    Row(
                        modifier = Modifier.padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        repeat(5) { index ->
                            AnimatedWaveBar(index = index)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedWaveBar(index: Int) {
    val delays = listOf(0f, 0.2f, 0.4f, 0.1f, 0.3f)
    val animatedHeight = remember {
        mutableStateOf(5f)
    }

    LaunchedEffect(Unit) {
        while (true) {
            val delay = delays.getOrNull(index) ?: 0f
            if (delay > 0) {
                kotlinx.coroutines.delay((delay * 1000).toLong())
            }
            animateFloatAsState(
                targetValue = if (animatedHeight.value == 5f) 22f else 5f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
            ).value.also { animatedHeight.value = it }
            kotlinx.coroutines.delay(800)
        }
    }

    Box(
        modifier = Modifier
            .width(6.dp)
            .height(animatedHeight.value.dp)
            .background(
                if (animatedHeight.value > 13f)
                    colorResource(com.mummyflix.R.color.secondary)
                else
                    colorResource(com.mummyflix.R.color.wave_blue)
            )
    )
}

@Composable
fun NoSignalOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 32.dp, vertical = 8.dp)
                .background(colorResource(com.mummyflix.R.color.wave_blue))
                .border(width = 2.dp, color = colorResource(com.mummyflix.R.color.border_cyan_light))
        ) {
            Text(
                text = stringResource(com.mummyflix.R.string.no_signal),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(com.mummyflix.R.color.text_dark_blue_3),
                letterSpacing = 1.sp
            )
        }
    }
}