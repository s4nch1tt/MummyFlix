package com.mummyflix.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mummyflix.R
import com.mummyflix.model.Channel
import com.mummyflix.viewmodel.MummyFlixViewModel

@Composable
fun ChannelListOSD(viewModel: MummyFlixViewModel) {
    val channels = viewModel.activeList.value
    val currentIndex = viewModel.currentIndex.value
    val isRadio = viewModel.isRadioMode.value
    val listState = rememberLazyListState()

    LaunchedEffect(currentIndex, channels.size) {
        if (currentIndex in channels.indices) {
            listState.animateScrollToItem(currentIndex)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OsdHeader(
            title = stringResource(
                if (isRadio) R.string.radio_channel_list else R.string.tv_channel_list
            )
        )

        Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
            // ---- Channel list ------------------------------------------------
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.42f)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                colorResource(R.color.bg_dark_blue_1),
                                colorResource(R.color.bg_dark_blue_2),
                                colorResource(R.color.bg_dark_blue_3)
                            )
                        )
                    )
                    .border(2.dp, colorResource(R.color.border_cyan))
            ) {
                ChannelListHeader(isRadio = isRadio, count = channels.size)

                if (channels.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (viewModel.isLoading.value) stringResource(R.string.loading_channels)
                            else stringResource(R.string.no_channels_scanned),
                            fontSize = 13.sp,
                            color = colorResource(R.color.text_cyan)
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        itemsIndexed(channels, key = { index, _ -> index }) { index, channel ->
                            ChannelRow(
                                index = index,
                                channel = channel,
                                isActive = index == currentIndex,
                                isPlaying = index == viewModel.playingIndex.value,
                                onClick = { viewModel.playChannel(index) }
                            )
                        }
                    }
                }
            }

            // ---- Video preview window (transparent, player shows through) ----
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(3.dp, colorResource(R.color.border_cyan_light))
                )

                PreviewInfo(
                    channel = channels.getOrNull(currentIndex),
                    index = currentIndex,
                    isRadio = isRadio,
                    isPlaying = viewModel.isVideoPlaying.value,
                    modifier = Modifier.align(Alignment.BottomStart)
                )
            }
        }

        OsdFooter {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                KeyBadge("▲")
                KeyBadge("▼")
                FooterHint(stringResource(R.string.move))
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                KeyBadge("◄")
                KeyBadge("►")
                FooterHint(stringResource(R.string.page))
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                KeyBadge("OK", onClick = viewModel::handleOkKey)
                FooterHint(stringResource(R.string.confirm))
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                KeyBadge("MENU", onClick = viewModel::handleMenuKey)
                FooterHint(stringResource(R.string.return_action))
            }
            InfoCircle(onClick = viewModel::showBanner)
        }
    }
}

@Composable
private fun ChannelListHeader(isRadio: Boolean, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(R.color.bg_blue_12))
            .padding(horizontal = 8.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isRadio) "RADIO" else "TV",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.secondary),
            letterSpacing = 2.sp
        )
        Text(
            text = "$count CH",
            fontSize = 10.sp,
            color = colorResource(R.color.border_cyan_light)
        )
    }
}

@Composable
private fun ChannelRow(
    index: Int,
    channel: Channel,
    isActive: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 2.dp)
            .background(
                when {
                    isActive -> Brush.horizontalGradient(
                        listOf(
                            colorResource(R.color.secondary),
                            colorResource(R.color.secondary_variant)
                        )
                    )

                    else -> Brush.horizontalGradient(
                        listOf(
                            colorResource(R.color.bg_blue_1).copy(alpha = 0.55f),
                            colorResource(R.color.bg_blue_2).copy(alpha = 0.55f)
                        )
                    )
                },
                RoundedCornerShape(3.dp)
            )
            .border(
                1.dp,
                if (isActive) Color.White else colorResource(R.color.bg_blue_3),
                RoundedCornerShape(3.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "%03d".format(index + 1),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isActive) Color.Black else colorResource(R.color.text_cyan_light),
            modifier = Modifier.width(34.dp)
        )
        Text(
            text = channel.displayName,
            fontSize = 12.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) Color.Black else colorResource(R.color.text_cyan),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (channel.fav) {
            Text("★", fontSize = 11.sp, color = colorResource(R.color.secondary))
        }
        if (channel.locked) {
            Text("L", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = colorResource(R.color.error))
        }
        if (isPlaying) {
            Text(
                text = "▶",
                fontSize = 9.sp,
                color = if (isActive) Color.Black else colorResource(R.color.gradient_green_end),
                modifier = Modifier.padding(start = 3.dp)
            )
        }
    }
}

@Composable
private fun PreviewInfo(
    channel: Channel?,
    index: Int,
    isRadio: Boolean,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.85f)
                    )
                )
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (channel != null) "%03d  %s".format(index + 1, channel.displayName) else "—",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = if (isPlaying) "● LIVE" else "○ IDLE",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPlaying) colorResource(R.color.gradient_green_end)
                else colorResource(R.color.text_yellow_dark)
            )
        }
        Text(
            text = if (isRadio) stringResource(R.string.radio_channel) else stringResource(R.string.tv_channel),
            fontSize = 10.sp,
            color = colorResource(R.color.border_cyan_light)
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SignalBar(stringResource(R.string.signal_intensity), 92, Modifier.weight(1f))
            SignalBar(stringResource(R.string.signal_quality), 78, Modifier.weight(1f))
        }
    }
}
