package com.mummyflix.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mummyflix.R
import com.mummyflix.model.BannerState
import com.mummyflix.viewmodel.MummyFlixViewModel

/** Fullscreen playback: the player already fills the screen, so only overlays remain. */
@Composable
fun FullscreenMode(viewModel: MummyFlixViewModel) {
    if (viewModel.isVideoPlaying.value && !viewModel.showNoSignal.value && !viewModel.isRadioMode.value) {
        MummyFlixBranding()
    }
}

/** Channel banner, anchored to the bottom-left of the root Box. */
@Composable
fun BoxScope.ChannelBanner(bannerState: BannerState) {
    Column(
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(bottom = 44.dp, start = 24.dp)
            .fillMaxWidth(0.6f)
            .background(
                Brush.verticalGradient(
                    listOf(
                        colorResource(R.color.bg_dark_blue_1).copy(alpha = 0.96f),
                        colorResource(R.color.bg_dark_blue_5).copy(alpha = 0.96f)
                    )
                )
            )
            .border(2.dp, colorResource(R.color.border_cyan))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            colorResource(R.color.border_cyan_light),
                            colorResource(R.color.border_blue),
                            colorResource(R.color.bg_blue_12)
                        )
                    )
                )
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = bannerState.channelType,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.text_dark_blue),
                letterSpacing = 1.sp
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = bannerState.channelName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1
            )
            Text(
                text = stringResource(R.string.left_channel),
                fontSize = 11.sp,
                color = colorResource(R.color.border_cyan_light)
            )
        }
    }
}

/** Numeric channel entry, anchored to the top-right of the root Box. */
@Composable
fun BoxScope.NumericInputOSD(buffer: String) {
    Box(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(top = 26.dp, end = 26.dp)
            .background(colorResource(R.color.text_dark_blue_2), RoundedCornerShape(4.dp))
            .border(2.dp, colorResource(R.color.secondary), RoundedCornerShape(4.dp))
            .padding(horizontal = 18.dp, vertical = 6.dp)
    ) {
        Text(
            text = buffer.padStart(3, '0'),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.secondary),
            letterSpacing = 4.sp
        )
    }
}