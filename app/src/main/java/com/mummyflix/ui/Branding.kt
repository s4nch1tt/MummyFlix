package com.mummyflix.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mummyflix.BuildConfig
import com.mummyflix.R

/** The mummy-in-a-TV mark, used everywhere MummyFlix signs its name. */
@Composable
fun MummyFlixMark(size: Dp, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.mummyflix_logo),
        contentDescription = stringResource(R.string.app_name),
        modifier = modifier.size(size)
    )
}

/** Compact wordmark for the OSD header. */
@Composable
fun BrandHeaderLockup(modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        MummyFlixMark(24.dp)
        Spacer(Modifier.width(7.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = stringResource(R.string.brand_wordmark),
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.5.sp,
                color = Color.White
            )
            Text(
                text = stringResource(R.string.brand_tagline),
                fontSize = 6.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.6.sp,
                color = colorResource(R.color.brand_gold)
            )
        }
    }
}

/**
 * Broadcast-style corner bug drawn over live video: the logo, an italic
 * wordmark and a gold network strap. Deliberately subtle (72% opacity) so it
 * reads as a channel watermark rather than a sticker.
 */
@Composable
fun PlayerWatermark(fullscreen: Boolean, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val compact = maxWidth < 380.dp
        val markSize = when {
            fullscreen -> 58.dp
            compact -> 30.dp
            else -> 38.dp
        }
        val titleSize = when {
            fullscreen -> 24.sp
            compact -> 13.sp
            else -> 16.sp
        }
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .alpha(if (fullscreen) 0.82f else 0.66f)
                .padding(
                    end = if (fullscreen) 30.dp else 12.dp,
                    bottom = if (fullscreen) 26.dp else 10.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MummyFlixMark(
                size = markSize,
                modifier = Modifier.shadow(6.dp, RoundedCornerShape(10.dp))
            )
            Spacer(Modifier.width(if (fullscreen) 10.dp else 6.dp))
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = stringResource(R.string.brand_wordmark),
                    fontSize = titleSize,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Black,
                    letterSpacing = if (fullscreen) 2.sp else 1.sp,
                    color = Color.White,
                    textAlign = TextAlign.Start
                )
                Spacer(Modifier.height(if (fullscreen) 3.dp else 1.dp))
                Text(
                    text = stringResource(R.string.brand_tagline),
                    fontSize = if (fullscreen) 9.sp else 6.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = if (fullscreen) 2.6.sp else 1.2.sp,
                    color = colorResource(R.color.brand_gold),
                    modifier = Modifier
                        .background(
                            colorResource(R.color.brand_navy).copy(alpha = 0.72f),
                            RoundedCornerShape(2.dp)
                        )
                        .padding(horizontal = if (fullscreen) 5.dp else 3.dp, vertical = 1.dp)
                )
            }
        }
    }
}

/** Brand plate used by System Info and the loading screen. */
@Composable
fun BrandPlate(
    modifier: Modifier = Modifier,
    showVersion: Boolean = true,
    horizontal: Boolean = true
) {
    if (horizontal) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MummyFlixMark(54.dp)
            BrandText()
        }
    } else {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MummyFlixMark(96.dp)
            BrandText(centered = true, showVersion = showVersion)
        }
    }
}

@Composable
private fun BrandText(centered: Boolean = false, showVersion: Boolean = true) {
    val align = if (centered) Alignment.CenterHorizontally else Alignment.Start
    Column(horizontalAlignment = align) {
        Text(
            text = stringResource(R.string.brand_wordmark),
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            fontStyle = FontStyle.Italic,
            letterSpacing = 3.sp,
            color = Color.White
        )
        Text(
            text = stringResource(R.string.brand_tagline),
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.4.sp,
            color = colorResource(R.color.brand_gold)
        )
        if (showVersion) {
            Spacer(Modifier.height(3.dp))
            Text(
                text = stringResource(R.string.brand_version, BuildConfig.VERSION_NAME),
                fontSize = 8.sp,
                letterSpacing = 1.sp,
                color = colorResource(R.color.text_cyan_light).copy(alpha = 0.75f)
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = stringResource(R.string.brand_signature),
            fontSize = 8.sp,
            fontStyle = FontStyle.Italic,
            color = colorResource(R.color.text_cyan_light).copy(alpha = 0.55f)
        )
    }
}

/** Sign-on panel shown while the playlist is being fetched. */
@Composable
fun BrandSplash(modifier: Modifier = Modifier, message: String) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            colorResource(R.color.brand_navy_light).copy(alpha = 0.96f),
                            colorResource(R.color.brand_navy).copy(alpha = 0.96f)
                        )
                    ),
                    RoundedCornerShape(8.dp)
                )
                .border(2.dp, colorResource(R.color.brand_cyan), RoundedCornerShape(8.dp))
                .padding(horizontal = 30.dp, vertical = 18.dp)
        ) {
            BrandPlate(showVersion = true, horizontal = false)
        }
        Spacer(Modifier.height(14.dp))
        Text(
            text = message,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = colorResource(R.color.text_cyan_light)
        )
    }
}
