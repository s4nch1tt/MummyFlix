package com.mummyflix.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mummyflix.R
import com.mummyflix.model.BannerState
import com.mummyflix.model.Channel
import com.mummyflix.model.KeypadState
import com.mummyflix.model.SnakeGameState
import com.mummyflix.model.SubMenuType
import com.mummyflix.viewmodel.MummyFlixViewModel

// ---------------------------------------------------------------------------
// Screen titles
// ---------------------------------------------------------------------------

@Composable
fun mainMenuTitle(menuLang: String): String =
    if (menuLang == "Hindi") stringResource(R.string.mukhya_menu)
    else stringResource(R.string.main_menu)

// ---------------------------------------------------------------------------
// TV / Radio list
// ---------------------------------------------------------------------------

@Composable
fun ChannelListBody(viewModel: MummyFlixViewModel, geo: OsdGeometry) {
    val channels = viewModel.activeList.value
    val currentIndex = viewModel.currentIndex.value
    val settings = viewModel.appSettings.value
    val isLoading = viewModel.isLoading.value

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .offset(geo.leftPanelX, geo.leftPanelY)
                .width(geo.leftPanelWidth)
                .height(geo.leftPanelHeight)
                .padding(end = 7.dp)
        ) {
            if (channels.isEmpty()) {
                OsdEmptyText(
                    if (isLoading) stringResource(R.string.loading_channels)
                    else stringResource(R.string.no_channels_scanned)
                )
            } else {
                ChannelListPanel(
                    channels = channels,
                    currentIndex = currentIndex,
                    rowHeight = geo.rowHeight,
                    isPlaying = viewModel.isVideoPlaying.value,
                    modifier = Modifier.fillMaxSize(),
                    onSelect = viewModel::playChannel
                )
            }
        }

        PreviewChrome(
            geo = geo,
            menuMode = false,
            lnb = settings.lnbFreq,
            transponder = "12020"
        )
    }
}

// ---------------------------------------------------------------------------
// Main menu
// ---------------------------------------------------------------------------

@Composable
fun MainMenuBody(viewModel: MummyFlixViewModel, geo: OsdGeometry) {
    val settings = viewModel.appSettings.value

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .offset(geo.leftPanelX, geo.leftPanelY)
                .width(geo.leftPanelWidth)
                .height(geo.leftPanelHeight)
                .padding(end = 7.dp)
        ) {
            MenuListPanel(
                items = viewModel.menuItems,
                selectedIndex = viewModel.menuIndex.value,
                modifier = Modifier.fillMaxSize(),
                showNumbers = false,
                onClick = viewModel::selectMenuItem
            )
        }

        PreviewChrome(
            geo = geo,
            menuMode = true,
            lnb = settings.lnbFreq,
            transponder = settings.transponder
        )
    }
}

// ---------------------------------------------------------------------------
// Sub-menu
// ---------------------------------------------------------------------------

@Composable
fun SubMenuBody(viewModel: MummyFlixViewModel, geo: OsdGeometry) {
    val subMenu = viewModel.activeSubMenu.value ?: return
    val settings = viewModel.appSettings.value

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .offset(geo.leftPanelX, geo.leftPanelY)
                .width(geo.leftPanelWidth)
                .height(geo.leftPanelHeight)
                .padding(end = 7.dp)
        ) {
            MenuListPanel(
                items = viewModel.menuItems,
                selectedIndex = viewModel.menuIndex.value,
                modifier = Modifier.fillMaxSize(),
                showNumbers = false,
                onClick = viewModel::selectMenuItem
            )
        }

        PreviewChrome(
            geo = geo,
            menuMode = true,
            lnb = settings.lnbFreq,
            transponder = settings.transponder
        )

        OptionPanelShell(title = subMenu.panelTitle, geo = geo) {
            SubMenuContent(viewModel = viewModel, subMenu = subMenu)
        }
    }
}

@Composable
private fun SubMenuContent(viewModel: MummyFlixViewModel, subMenu: SubMenuType) {
    val selected = viewModel.subMenuIndex.value
    val options = viewModel.currentSubMenuOptions()
    val channel = viewModel.activeList.value.getOrNull(viewModel.currentIndex.value)

    when (subMenu) {
        SubMenuType.PROGRAM_SETUP -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    options.forEachIndexed { index, option ->
                        OptionRow(
                            text = "${index + 1}. $option",
                            active = index == selected,
                            onClick = { viewModel.selectSubMenuOption(index) }
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    SignalBar(stringResource(R.string.signal_intensity), 92)
                    SignalBar(stringResource(R.string.signal_quality), 78)
                }
            }
        }

        SubMenuType.PROGRAM_EDIT -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    InfoRow(
                        label = stringResource(R.string.channel),
                        value = channel?.displayName?.take(16) ?: "—"
                    )
                    val labels = listOf(
                        "1. ${stringResource(R.string.favorite)}: ${if (channel?.fav == true) "ON ♥" else "OFF"}",
                        "2. ${stringResource(R.string.lock_channel)}: ${if (channel?.locked == true) "LOCKED 🔒" else "UNLOCKED"}",
                        "3. ${stringResource(R.string.delete_current_channel)}",
                        "4. ${stringResource(R.string.back_to_main_menu)}"
                    )
                    labels.forEachIndexed { index, label ->
                        OptionRow(
                            text = label,
                            active = index == selected,
                            onClick = { viewModel.selectSubMenuOption(index) }
                        )
                    }
                }
                Hint(stringResource(R.string.press_ok_to_toggle))
            }
        }

        SubMenuType.PROGRAM_INFO -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    InfoRow(
                        label = stringResource(R.string.channel),
                        value = "${viewModel.currentIndex.value + 1}. ${channel?.displayName?.take(15) ?: "—"}"
                    )
                    InfoRow(stringResource(R.string.downlink_freq), "10990 MHz (H)")
                    InfoRow(stringResource(R.string.symbol_rate), "28490 Ksps")
                    InfoRow(stringResource(R.string.video_audio_pid), "5064 / 6064")
                }
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    SignalBar(stringResource(R.string.signal_intensity), 90)
                    SignalBar(stringResource(R.string.signal_quality), 76)
                }
            }
        }

        SubMenuType.LANGUAGE -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    options.forEachIndexed { index, option ->
                        OptionRow(
                            text = "${index + 1}. $option",
                            active = index == selected,
                            onClick = { viewModel.selectSubMenuOption(index) }
                        )
                    }
                }
                Hint(stringResource(R.string.tap_or_press_to_switch))
            }
        }

        SubMenuType.SYSTEM_SETTING -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    options.forEachIndexed { index, option ->
                        OptionRow(
                            text = "${index + 1}. $option",
                            active = index == selected,
                            onClick = { viewModel.selectSubMenuOption(index) }
                        )
                    }
                }
                Hint(stringResource(R.string.use_ok_to_adjust))
            }
        }

        SubMenuType.GAME -> {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.score) + ": ${viewModel.snakeGameState.value.score}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.text_cyan)
                    )
                    if (viewModel.snakeGameState.value.isGameOver) {
                        Text(
                            text = "GAME OVER",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = colorResource(R.color.border_red)
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                SnakeBoard(
                    state = viewModel.snakeGameState.value,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
                Spacer(Modifier.height(6.dp))
                Hint(
                    text = stringResource(R.string.use_arrows_to_move),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        SubMenuType.SYSTEM_INFO -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    InfoRow(stringResource(R.string.satellite), stringResource(R.string.insat_4b))
                    InfoRow(stringResource(R.string.hw_version), stringResource(R.string.gx6605s_v12))
                    InfoRow(stringResource(R.string.sw_version), stringResource(R.string.v304_ind))
                    InfoRow(
                        label = stringResource(R.string.scanned_tv_radio),
                        value = "${viewModel.allChannels.value.size} / ${viewModel.radioChannels.value.size}"
                    )
                    InfoRow(stringResource(R.string.last_update), stringResource(R.string.date_2026))
                }
                OptionRow(
                    text = stringResource(R.string.ok_return),
                    active = true,
                    onClick = viewModel::handleMenuKey
                )
            }
        }
    }
}

@Composable
private fun SnakeBoard(state: SnakeGameState, modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .background(colorResource(R.color.game_bg), RoundedCornerShape(3.dp))
            .border(2.dp, colorResource(R.color.game_border), RoundedCornerShape(3.dp))
            .padding(4.dp)
    ) {
        val columns = 26f
        val rows = 16f
        val cellW = size.width / columns
        val cellH = size.height / rows

        state.snake.forEach { point ->
            drawRect(
                color = Color(0xFFF5E625),
                topLeft = Offset(point.x * cellW, point.y * cellH),
                size = Size((cellW - 1f).coerceAtLeast(1f), (cellH - 1f).coerceAtLeast(1f))
            )
        }
        drawRect(
            color = Color(0xFFFF4D6D),
            topLeft = Offset(state.food.x * cellW, state.food.y * cellH),
            size = Size((cellW - 1f).coerceAtLeast(1f), (cellH - 1f).coerceAtLeast(1f))
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRect(
                    color = Color(0x4D8ECAE6),
                    topLeft = Offset(0f, size.height - 1.dp.toPx()),
                    size = Size(size.width, 1.dp.toPx())
                )
            }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = colorResource(R.color.text_cyan)
        )
        Text(
            text = value,
            fontSize = 16.sp,
            color = colorResource(R.color.text_cyan),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.End
        )
    }
}

// ---------------------------------------------------------------------------
// Auto scan
// ---------------------------------------------------------------------------

@Composable
fun AutoScanBody(viewModel: MummyFlixViewModel, geo: OsdGeometry) {
    val scan = viewModel.scanProgress.value
    val settings = viewModel.appSettings.value
    val tvScanned = viewModel.masterTvChannels.value.take(scan.tvScanned).takeLast(6)
    val radioScanned = viewModel.masterRadioChannels.value.take(scan.radioScanned).takeLast(6)

    Column(
        modifier = Modifier
            .offset(14.dp, geo.headerHeight + 4.dp)
            .width(geo.width - 28.dp)
            .height(geo.bodyHeight - 8.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ScanParam(stringResource(R.string.insat_4b), "", Modifier.weight(1f))
                ScanParam("TP", scan.tpNum.toString(), Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ScanParam(stringResource(R.string.lnb_freq), settings.lnbFreq, Modifier.weight(1f))
                ScanParam(stringResource(R.string.symbol_rate), scan.symbolRate, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ScanParam(stringResource(R.string.trans_freq), scan.transFreq, Modifier.weight(1f))
                ScanParam("Polarity", scan.polarity, Modifier.weight(1f))
            }
        }

        ScanProgressRow(scan.progress)

        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ScanColumn(
                title = stringResource(R.string.tv_channel_list),
                channels = tvScanned,
                startNumber = (scan.tvScanned - tvScanned.size).coerceAtLeast(0),
                isRadio = false,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            ScanColumn(
                title = stringResource(R.string.radio_channel_list),
                channels = radioScanned,
                startNumber = (scan.radioScanned - radioScanned.size).coerceAtLeast(0),
                isRadio = true,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }
    }
}

@Composable
private fun ScanParam(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .height(38.dp)
            .background(
                Brush.verticalGradient(
                    listOf(
                        colorResource(R.color.bg_blue_1),
                        colorResource(R.color.bg_blue_4)
                    )
                ),
                RoundedCornerShape(5.dp)
            )
            .border(1.5.dp, colorResource(R.color.bg_blue_5), RoundedCornerShape(5.dp))
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE6F6FF),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1
        )
    }
}

@Composable
private fun ScanProgressRow(progress: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(16.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(colorResource(R.color.scan_bar_bg))
                .border(1.5.dp, colorResource(R.color.scan_bar_border), RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress.coerceIn(0, 100) / 100f)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                colorResource(R.color.gradient_yellow_start),
                                colorResource(R.color.gradient_yellow_end)
                            )
                        )
                    )
                    .drawBehind {
                        if (progress in 1..99) {
                            drawRect(
                                color = Color.White,
                                topLeft = Offset(size.width - 2.dp.toPx(), 0f),
                                size = Size(2.dp.toPx(), size.height)
                            )
                        }
                    }
            )
        }
        Text(
            text = "$progress%",
            modifier = Modifier.width(48.dp),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun ScanColumn(
    title: String,
    channels: List<Channel>,
    startNumber: Int,
    isRadio: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isRadio) RadioIcon() else TvIcon()
            Spacer(Modifier.width(8.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(colorResource(R.color.bg_blue_9))
                .border(1.5.dp, colorResource(R.color.bg_blue_10))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (channels.isEmpty()) {
                Text(
                    text = "Scanning…",
                    fontSize = 15.sp,
                    color = colorResource(R.color.text_cyan_light)
                )
            } else {
                channels.forEachIndexed { index, channel ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "%03d".format(startNumber + index + 1),
                            modifier = Modifier.width(50.dp),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = channel.displayName.uppercase().take(15),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TvIcon() {
    Canvas(modifier = Modifier.size(20.dp, 18.dp)) {
        val stroke = 2.dp.toPx()
        drawRect(
            color = Color.White,
            topLeft = Offset(1.dp.toPx(), 5.dp.toPx()),
            size = Size(size.width - 2.dp.toPx(), size.height - 6.dp.toPx()),
            style = Stroke(stroke)
        )
        drawLine(
            color = Color.White,
            start = Offset(size.width * 0.30f, 5.dp.toPx()),
            end = Offset(size.width * 0.42f, 1.dp.toPx()),
            strokeWidth = stroke
        )
        drawLine(
            color = Color.White,
            start = Offset(size.width * 0.62f, 5.dp.toPx()),
            end = Offset(size.width * 0.52f, 1.dp.toPx()),
            strokeWidth = stroke
        )
    }
}

@Composable
private fun RadioIcon() {
    Canvas(modifier = Modifier.size(20.dp, 18.dp)) {
        val stroke = 2.dp.toPx()
        drawRect(
            color = Color.White,
            topLeft = Offset(1.dp.toPx(), 3.dp.toPx()),
            size = Size(size.width - 2.dp.toPx(), size.height - 4.dp.toPx()),
            style = Stroke(stroke)
        )
        drawCircle(
            color = Color.White,
            radius = 2.5.dp.toPx(),
            center = Offset(size.width * 0.70f, size.height * 0.55f)
        )
    }
}

// ---------------------------------------------------------------------------
// Fullscreen banner, numeric input, keypad
// ---------------------------------------------------------------------------

@Composable
fun BoxScope.ChannelBanner(bannerState: BannerState) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = maxWidth * 0.06f, bottom = maxHeight * 0.05f)
                .width(maxWidth * 0.82f)
                .shadow(8.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            colorResource(R.color.bg_dark_blue_1),
                            colorResource(R.color.bg_dark_blue_5)
                        )
                    )
                )
                .border(2.dp, colorResource(R.color.border_cyan))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colorStops = arrayOf(
                                0f to colorResource(R.color.border_cyan_light),
                                0.55f to colorResource(R.color.border_blue),
                                0.57f to colorResource(R.color.bg_blue_11),
                                1f to colorResource(R.color.bg_blue_12)
                            )
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(colorResource(R.color.bg_blue_12), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "➜",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.border_cyan_light)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = bannerState.channelType,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.text_dark_blue),
                    letterSpacing = 1.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = bannerState.channelName.ifBlank { stringResource(R.string.loading_channels) },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.left_channel),
                    fontSize = 14.sp,
                    color = colorResource(R.color.border_cyan_light)
                )
            }
        }
    }
}

@Composable
fun BoxScope.NumericInputOSD(buffer: String) {
    Box(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(top = 32.dp, end = 40.dp)
            .background(colorResource(R.color.bg_blue_11), RoundedCornerShape(4.dp))
            .border(2.dp, colorResource(R.color.secondary), RoundedCornerShape(4.dp))
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Text(
            text = buffer.padStart(3, '0'),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.secondary),
            letterSpacing = 3.sp
        )
    }
}

@Composable
fun ChannelKeypad(
    state: KeypadState,
    onSelect: (Int, Int) -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.66f))
            .clickable(onClick = onClose),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(280.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            colorResource(R.color.bg_dark_blue_4),
                            colorResource(R.color.bg_dark_blue_5)
                        )
                    ),
                    RoundedCornerShape(6.dp)
                )
                .border(2.dp, colorResource(R.color.border_cyan_light), RoundedCornerShape(6.dp))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.goto_channel_title),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.secondary),
                letterSpacing = 2.sp
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorResource(R.color.bg_blue_6), RoundedCornerShape(3.dp))
                    .border(2.dp, colorResource(R.color.secondary), RoundedCornerShape(3.dp))
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.buffer.padStart(3, '0'),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.secondary),
                    letterSpacing = 5.sp
                )
            }

            val labels = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "DEL", "0", "GO")
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                for (row in 0 until 4) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (col in 0 until 3) {
                            val index = row * 3 + col
                            val active = state.row == row && state.col == col
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .background(
                                        if (active) colorResource(R.color.secondary)
                                        else colorResource(R.color.bg_blue_11),
                                        RoundedCornerShape(3.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (active) Color.White else colorResource(R.color.bg_blue_3),
                                        RoundedCornerShape(3.dp)
                                    )
                                    .clickable { onSelect(row, col) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = labels[index],
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (active) Color.Black
                                    else colorResource(R.color.border_cyan_light)
                                )
                            }
                        }
                    }
                }
            }

            Text(
                text = "D-pad to move • OK to select • BACK to close",
                fontSize = 10.sp,
                color = colorResource(R.color.border_cyan_light),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Small shared widgets
// ---------------------------------------------------------------------------

@Composable
fun SignalBar(label: String, percent: Int, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = "$label: ${percent.coerceIn(0, 100)}%",
            fontSize = 16.sp,
            color = colorResource(R.color.text_cyan)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp)
                .height(14.dp)
                .background(colorResource(R.color.bg_blue_6))
                .border(1.dp, colorResource(R.color.border_cyan))
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
                        )
                    )
            )
        }
    }
}

@Composable
fun Hint(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        fontSize = 13.sp,
        color = colorResource(R.color.border_cyan_light),
        lineHeight = 17.sp
    )
}
