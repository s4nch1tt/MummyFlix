package com.mummyflix.ui

import androidx.compose.animation.animateFloatAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.drawRect
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.px
import androidx.compose.ui.unit.sp
import com.mummyflix.model.*
import com.mummyflix.viewmodel.MummyFlixViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChannelListOSD(
    viewModel: MummyFlixViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val listScrollY = remember { mutableStateOf(0f) }
    val maxScrollY = remember { mutableStateOf(0f) }
    val viewHeight = remember { mutableStateOf(0f) }
    val itemHeight = remember { mutableStateOf(30f) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // OSD Frame - full screen with border
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            colorResource(R.color.bg_dark_blue_1),
                            colorResource(R.color.bg_dark_blue_2),
                            colorResource(R.color.bg_dark_blue_3)
                        )
                    )
                )
                .border(width = 3.dp, color = colorResource(R.color.border_cyan)),
            contentAlignment = Alignment.TopStart
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.5.percent)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    colorResource(R.color.border_cyan_light),
                                    colorResource(R.color.border_blue),
                                    colorResource(R.color.bg_blue_12),
                                    colorResource(R.color.bg_blue_13)
                                )
                            )
                        )
                        .border(bottom = BorderStroke(2.dp, colorResource(R.color.border_cyan_lighter))),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .background(colorResource(R.color.bg_blue_12))
                                .clip(RoundedCornerShape(50.percent))
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("➜", fontSize = 12.sp, color = colorResource(R.color.border_cyan_light), fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = if (viewModel.isRadioMode.value) stringResource(R.string.radio_channel_list) else stringResource(R.string.tv_channel_list),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.text_dark_blue),
                            letterSpacing = 1.0.sp
                        )
                    }
                }

                // Body - Left Panel + Right Panel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    // Left Panel - Channel List with Custom Scrollbar
                    LeftChannelPanel(
                        viewModel = viewModel,
                        listScrollY = listScrollY,
                        maxScrollY = maxScrollY,
                        viewHeight = viewHeight,
                        itemHeight = itemHeight
                    )

                    // Right Panel - Preview Details
                    RightPreviewPanel(viewModel = viewModel)
                }

                // Footer
                FooterKeyHints(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun LeftChannelPanel(
    viewModel: MummyFlixViewModel,
    listScrollY: MutableState<Float>,
    maxScrollY: MutableState<Float>,
    viewHeight: MutableState<Float>,
    itemHeight: MutableState<Float>
) {
    val activeList = viewModel.activeList.value
    val currentIndex = viewModel.currentIndex.value
    val isRadioMode = viewModel.isRadioMode.value

    Box(
        modifier = Modifier
            .width(40.5.percent)
            .fillMaxHeight()
            .padding(right = 7.dp),
        contentAlignment = Alignment.TopStart
    ) {
        // Channel List Track
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
        ) {
            activeList.forEachIndexed { index, channel ->
                val isActive = index == currentIndex
                val isPlaying = index == viewModel.playingIndex.value
                val showFav = channel.fav
                val showLocked = channel.locked

                ChannelListItem(
                    index = index,
                    channel = channel,
                    isActive = isActive,
                    isPlaying = isPlaying,
                    isRadioMode = isRadioMode,
                    onClick = { viewModel.moveSelection(index - currentIndex); viewModel.playChannel(index) }
                )
            }
        }

        // Custom Scrollbar
        if (maxScrollY.value > 0 && viewHeight.value > 0) {
            val thumbHeight = maxOf(18f, (viewHeight.value / (maxScrollY.value + viewHeight.value)) * viewHeight.value)
            val maxThumbTop = viewHeight.value - thumbHeight
            val thumbTop = (listScrollY.value / maxScrollY.value) * maxThumbTop

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .width(5.dp)
                    .height(viewHeight.value.dp)
                    .background(colorResource(R.color.bg_blue_6))
                    .clip(RoundedCornerShape(3.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(thumbHeight.dp)
                        .background(colorResource(R.color.border_cyan))
                        .clip(RoundedCornerShape(3.dp))
                        .offset(y = thumbTop.dp)
                )
            }
        }
    }
}

@Composable
fun ChannelListItem(
    index: Int,
    channel: Channel,
    isActive: Boolean,
    isPlaying: Boolean,
    isRadioMode: Boolean,
    onClick: () -> Unit
) {
    val numStr = "%03d".format(index + 1)
    val badge = buildString {
        if (channel.fav) append("♥ ")
        if (channel.locked) append("🔒 ")
    }
    val cleanName = (badge + channel.name).take(18)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .padding(bottom = 2.dp)
            .background(
                if (isActive)
                    Brush.verticalGradient(
                        colors = listOf(
                            colorResource(R.color.secondary),
                            colorResource(R.color.secondary_variant)
                        )
                    )
                else
                    Brush.verticalGradient(
                        colors = listOf(
                            colorResource(R.color.bg_blue_1),
                            colorResource(R.color.bg_blue_2)
                        )
                    )
            )
            .border(
                width = 1.5.dp,
                color = if (isActive) Color.White else colorResource(R.color.bg_blue_3)
            )
            .clip(RoundedCornerShape(4.dp))
            .padding(horizontal = 10.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .pointerInput(Unit) {
                detectTapGestures(onPress = { /* handle press */ })
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = numStr,
                fontSize = 14.sp,
                color = if (isActive) Color.Black else colorResource(R.color.text_cyan),
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                letterSpacing = 0.5.sp
            )
            Text(
                text = cleanName,
                fontSize = 14.sp,
                color = if (isActive) Color.Black else colorResource(R.color.text_cyan),
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                letterSpacing = 0.5.sp,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun RightPreviewPanel(viewModel: MummyFlixViewModel) {
    val currentChannel = viewModel.activeList.value.getOrNull(viewModel.currentIndex.value)

    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Preview bottom bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp)
                .background(colorResource(R.color.border_cyan_light))
                .border(width = 2.dp, color = Color.White)
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .size(width = 14.dp, height = 10.dp)
                    .background(
                        Brush.radialGradient(
                            center = androidx.compose.ui.graphics.Center(0.5f, 0.5f),
                            radius = 50.percent,
                            colors = listOf(colorResource(R.color.wave_green), Color.Rgb(0x1b, 0x43, 0x32))
                        )
                    )
                    .clip(RoundedCornerShape(50.percent))
                    .border(width = 1.dp, color = Color.Rgb(0x08, 0x1c, 0x15))
            )
        }

        // Transponder Info
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, horizontal = 10.dp)
                .background(colorResource(R.color.bg_blue_3).copy(alpha = 0.9f))
                .border(top = BorderStroke(2.dp, colorResource(R.color.border_cyan_light))),
            contentAlignment = Alignment.CenterStart
        ) {
            Column(
                modifier = Modifier.padding(4.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Row {
                    Text("12020/V/30000", fontSize = 14.sp, color = colorResource(R.color.text_cyan), letterSpacing = 0.5.sp, modifier = Modifier.weight(1.3f))
                    Text("DiSEqC:Off", fontSize = 14.sp, color = colorResource(R.color.text_cyan), letterSpacing = 0.5.sp, modifier = Modifier.weight(1f))
                }
                Row {
                    Text("5064/6064", fontSize = 14.sp, color = colorResource(R.color.text_cyan), letterSpacing = 0.5.sp, modifier = Modifier.weight(1.3f))
                    Text("22K:Off", fontSize = 14.sp, color = colorResource(R.color.text_cyan), letterSpacing = 0.5.sp, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun FooterKeyHints(viewModel: MummyFlixViewModel) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.5.percent)
            .minHeight(36.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        colorResource(R.color.border_cyan),
                        colorResource(R.color.border_cyan_dark)
                    )
                )
            )
            .border(top = BorderStroke(2.dp, colorResource(R.color.border_cyan_lighter)))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Move keys
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                KeyBadge(label = "▲", onClick = { viewModel.moveSelection(-1) })
                KeyBadge(label = "▼", onClick = { viewModel.moveSelection(1) })
                Text(
                    text = stringResource(R.string.select),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.text_dark_blue)
                )
            }

            // Page keys
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                KeyBadge(label = "◄", onClick = { viewModel.handleLeftRight(-1) })
                KeyBadge(label = "►", onClick = { viewModel.handleLeftRight(1) })
                Text(
                    text = stringResource(R.string.page),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.text_dark_blue)
                )
            }

            // OK key
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                KeyBadge(label = "OK")
                Text(
                    text = stringResource(R.string.enter),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.text_dark_blue)
                )
            }

            // Menu key
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                KeyBadge(label = "MENU")
                Text(
                    text = stringResource(R.string.exit),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.text_dark_blue)
                )
            }

            // Info circle
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(colorResource(R.color.text_dark_blue_2))
                    .clip(RoundedCornerShape(50.percent))
                    .clickable { viewModel.showBanner() },
                contentAlignment = Alignment.Center
            ) {
                Text("i", fontSize = 16.sp, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic, color = colorResource(R.color.border_cyan_light), fontFamily = FontFamily.Serif)
            }
        }
    }
}

@Composable
fun KeyBadge(
    label: String,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 3.dp)
            .background(colorResource(R.color.text_dark_blue_2))
            .border(width = 1.dp, color = colorResource(R.color.text_dark_blue_3))
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .wrapContentWidth()
            .wrapContentHeight(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = colorResource(R.color.border_cyan_light),
            fontWeight = FontWeight.Bold
        )
    }
}