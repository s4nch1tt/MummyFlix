package com.mummyflix.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.FontStyle
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mummyflix.model.*
import com.mummyflix.viewmodel.MummyFlixViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FullscreenMode(viewModel: MummyFlixViewModel) {
    // In fullscreen mode, only video is shown - no OSD frame
    // The video player fills the entire screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Video player would be here - full screen
        // Watermark is shown on video
        if (viewModel.isVideoPlaying.value && !viewModel.showNoSignal.value && !viewModel.isRadioMode.value) {
            WatermarkOverlay()
        }
    }
}

@Composable
fun WatermarkOverlay() {
    Box(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(bottom = 30.dp, end = 20.dp)
            .graphicsLayer {
                scaleX = 0.82f
                scaleY = 0.82f
                transformOrigin = androidx.compose.ui.graphics.TransformOrigin.BottomRight
            }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(-2.dp)
        ) {
            // Dish Icon
            Box(
                modifier = Modifier
                    .size(34.dp, 18.dp)
                    .background(Color.Transparent)
            ) {
                // Simple vector representation
                androidx.compose.foundation.Canvas(
                    modifier = Modifier.size(34.dp, 18.dp)
                ) {
                    // Main curve
                    drawPath(
                        path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(12f, 26f)
                            cubicTo(10f, 14f, 26f, 4f, 46f, 8f)
                        },
                        color = colorResource(R.color.watermark_orange),
                        style = androidx.compose.ui.graphics.Stroke(width = 4f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                    )
                    // Inner curve
                    drawPath(
                        path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(20f, 22f)
                            cubicTo(20f, 14f, 30f, 9f, 42f, 12f)
                        },
                        color = Color.White,
                        style = androidx.compose.ui.graphics.Stroke(width = 2.5f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                    )
                    // Center dot
                    drawCircle(
                        color = colorResource(R.color.watermark_yellow),
                        center = androidx.compose.ui.geometry.Offset(32f, 17f),
                        radius = 3.5f
                    )
                }
            }
            // SR PLUS Text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text(
                    text = "SR",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    color = Color.White.copy(alpha = 0.9f),
                    letterSpacing = 1.5.sp,
                    shadow = Shadow(offset = androidx.compose.ui.geometry.Offset(1f, 1f), blurRadius = 3f, color = colorResource(R.color.watermark_orange).copy(alpha = 0.9f))
                )
                Text(
                    text = "PLUS",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = colorResource(R.color.watermark_orange),
                    letterSpacing = 2.5.sp,
                    background = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }
    }
}

@Composable
fun ChannelBanner(bannerState: BannerState) {
    Box(
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(bottom = 30.dp, start = 35.dp)
            .fillMaxWidth(0.82f)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        colorResource(R.color.bg_dark_blue_1),
                        colorResource(R.color.bg_dark_blue_5)
                    )
                )
            )
            .border(width = 2.dp, color = colorResource(R.color.border_cyan))
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(0.dp))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Banner Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                colorResource(R.color.border_cyan_light),
                                colorResource(R.color.border_blue),
                                colorResource(R.color.text_dark_blue_2),
                                colorResource(R.color.bg_blue_12)
                            )
                        )
                    )
                    .border(bottom = BorderStroke(2.dp, colorResource(R.color.border_cyan_lighter))),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .background(colorResource(R.color.bg_blue_12))
                            .clip(CircleShape)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("➜", fontSize = 10.sp, color = colorResource(R.color.border_cyan_light), fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = bannerState.channelType,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.text_dark_blue),
                        letterSpacing = 1.0.sp
                    )
                }
            }

            // Banner Body
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = bannerState.channelName,
                        fontSize = 18.sp,
                        color = Color.White,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(R.string.left_channel),
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun NumericInputOSD(buffer: String) {
    Box(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(top = 30.dp, end = 25.dp)
            .background(colorResource(R.color.text_dark_blue_2))
            .border(width = 2.dp, color = colorResource(R.color.secondary))
            .clip(RoundedCornerShape(4.dp))
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Text(
            text = buffer.padStart(3, '0'),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.secondary),
            letterSpacing = 3.sp
        )
    }
}