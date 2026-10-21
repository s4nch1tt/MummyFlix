package com.mummyflix.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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

/** Shared OSD chrome so every screen stays visually consistent. */
@Composable
fun OsdHeader(title: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        colorResource(R.color.border_cyan_light),
                        colorResource(R.color.border_blue),
                        colorResource(R.color.bg_blue_12),
                        colorResource(R.color.bg_blue_13)
                    )
                )
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(colorResource(R.color.bg_blue_12), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "➜",
                    fontSize = 12.sp,
                    color = colorResource(R.color.border_cyan_light),
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.text_dark_blue),
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun OsdFooter(
    modifier: Modifier = Modifier,
    focusArea: Int = 0,
    isGame: Boolean = false,
    content: @Composable RowScope.(focusArea: Int) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(42.dp)
            .background(
                Brush.verticalGradient(
                    listOf(
                        colorResource(R.color.border_cyan),
                        colorResource(R.color.border_cyan_dark)
                    )
                )
            )
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        content = { content(focusArea) }
    )
}

@Composable
fun KeyBadge(
    label: String,
    isFocused: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .background(
                if (isFocused) colorResource(R.color.secondary) else Color.White.copy(alpha = 0.92f),
                RoundedCornerShape(3.dp)
            )
            .border(
                1.dp,
                if (isFocused) Color.White else colorResource(R.color.text_dark_blue_2),
                RoundedCornerShape(3.dp)
            )
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 7.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isFocused) Color.Black else colorResource(R.color.text_dark_blue)
        )
    }
}

@Composable
fun FooterHint(label: String, isFocused: Boolean = false, modifier: Modifier = Modifier) {
    Text(
        text = label,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = if (isFocused) Color.Black else colorResource(R.color.text_dark_blue),
        modifier = modifier
    )
}

@Composable
fun InfoCircle(onClick: () -> Unit, isFocused: Boolean = false) {
    Box(
        modifier = Modifier
            .size(26.dp)
            .background(
                if (isFocused) colorResource(R.color.secondary) else colorResource(R.color.text_dark_blue_2),
                CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "i",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (isFocused) Color.Black else colorResource(R.color.border_cyan_light)
        )
    }
}

/** Thin labelled progress bar used for signal strength / quality. */
@Composable
fun SignalBar(label: String, percent: Int, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 12.sp, color = colorResource(R.color.text_cyan))
            Text(
                text = "$percent%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.text_cyan)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp)
                .height(10.dp)
                .background(colorResource(R.color.bg_blue_6), RoundedCornerShape(2.dp))
                .border(1.dp, colorResource(R.color.border_cyan), RoundedCornerShape(2.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(percent.coerceIn(0, 100) / 100f)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                colorResource(R.color.gradient_green_start),
                                colorResource(R.color.gradient_green_end)
                            )
                        ),
                        RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}

@Composable
fun PanelBackground(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    listOf(
                        colorResource(R.color.bg_dark_blue_4),
                        colorResource(R.color.bg_dark_blue_5)
                    )
                )
            )
            .border(2.dp, colorResource(R.color.border_cyan_light))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        content = content
    )
}

@Composable
fun ScreenTitle(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = colorResource(R.color.secondary)
    )
}

@Composable
fun Hint(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        color = colorResource(R.color.border_cyan_light),
        lineHeight = 15.sp
    )
}

@Composable
fun MummyFlixBranding() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
        Column(
            modifier = Modifier.padding(bottom = 34.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "MUMMY",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = Color.White.copy(alpha = 0.9f),
                letterSpacing = 1.5.sp
            )
            Text(
                text = "FLIX",
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = colorResource(R.color.watermark_orange),
                letterSpacing = 2.5.sp,
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(2.dp))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            )
        }
    }
}