package com.mummyflix.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mummyflix.BuildConfig
import com.mummyflix.R
import com.mummyflix.model.SnakeGameState
import com.mummyflix.model.SubMenuType
import com.mummyflix.viewmodel.MummyFlixViewModel

/**
 * Sub-menu body: the main menu stays on the left (so UP/DOWN keeps working)
 * while the selected panel opens as a blue option modal on the right.
 */
@Composable
fun SubMenuBody(viewModel: MummyFlixViewModel, geo: OsdGeometry) {
    val subMenu = viewModel.activeSubMenu.value ?: return
    val options = viewModel.currentSubMenuOptions()
    val selected = viewModel.subMenuIndex.value
    val channel = viewModel.activeList.value.getOrNull(viewModel.currentIndex.value)
    val settings = viewModel.appSettings.value

    Box(modifier = Modifier.fillMaxSize()) {
        MenuListPanel(
            items = viewModel.menuItems,
            selectedIndex = viewModel.menuIndex.value,
            modifier = Modifier
                .offset(geo.leftPanelX, geo.leftPanelY)
                .width(geo.leftPanelWidth)
                .height(geo.leftPanelHeight),
            onClick = viewModel::selectMenuItem
        )

        OptionPanelShell(title = subMenu.title, geo = geo) {
            when (subMenu) {
                SubMenuType.PROGRAM_SETUP -> {
                    OptionList(options, selected, viewModel::selectSubMenuOption)
                    Spacer(Modifier.weight(1f))
                    SignalBlock()
                    PanelHint("LNB ${settings.lnbFreq}  •  TP ${settings.transponder}  •  ${settings.tvSystem}")
                }

                SubMenuType.PROGRAM_EDIT -> {
                    Text(
                        text = channel?.displayName ?: "—",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    OptionList(options, selected, viewModel::selectSubMenuOption)
                    Spacer(Modifier.weight(1f))
                    PanelHint(stringResource(R.string.press_ok_to_toggle))
                }

                SubMenuType.PROGRAM_INFO -> {
                    InfoRow("Channel", channel?.displayName ?: "—")
                    InfoRow("Number", "%03d".format(viewModel.currentIndex.value + 1))
                    InfoRow("Type", if (viewModel.isRadioMode.value) "Radio" else "TV")
                    InfoRow("Downlink Freq", "${settings.transponder} MHz (H)")
                    InfoRow("Symbol Rate", "28490 Ksps")
                    InfoRow("Video/Audio PID", "5064 / 6064")
                    InfoRow("Favorite", if (channel?.fav == true) "ON ♥" else "OFF")
                    InfoRow("Lock", if (channel?.locked == true) "LOCKED" else "OFF")
                    if (channel != null && channel.url.isNotBlank()) {
                        Text(
                            text = channel.url,
                            fontSize = 9.sp,
                            color = colorResource(R.color.border_cyan_light),
                            maxLines = 1,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    SignalBlock()
                }

                SubMenuType.LANGUAGE -> {
                    OptionList(options, selected, viewModel::selectSubMenuOption)
                    Spacer(Modifier.weight(1f))
                    PanelHint(stringResource(R.string.tap_or_press_to_switch))
                }

                SubMenuType.SYSTEM_SETTING -> {
                    OptionList(options, selected, viewModel::selectSubMenuOption)
                    Spacer(Modifier.weight(1f))
                    PanelHint(stringResource(R.string.use_ok_to_adjust))
                }

                SubMenuType.GAME -> {
                    SnakeBoard(state = viewModel.snakeGameState.value, modifier = Modifier.weight(1f))
                    Text(
                        text = stringResource(R.string.use_arrows_to_move),
                        fontSize = 10.sp,
                        color = colorResource(R.color.border_cyan_light),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    )
                }

                SubMenuType.SYSTEM_INFO -> {
                    BrandPlate(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.25f))
                            .border(1.dp, colorResource(R.color.bg_blue_3))
                            .padding(8.dp),
                        showVersion = true
                    )
                    Spacer(Modifier.height(6.dp))
                    InfoRow("Satellite", "INSAT 4B")
                    InfoRow("H/W Version", "GX6605S V1.2")
                    InfoRow("S/W Version", "MUMMYFLIX ${BuildConfig.VERSION_NAME}")
                    InfoRow("TV Channels", viewModel.allChannels.value.size.toString())
                    InfoRow("Radio Channels", viewModel.radioChannels.value.size.toString())
                    InfoRow("Last Update", "2026-10-21")
                    Spacer(Modifier.weight(1f))
                    PanelHint("Press OK or BACK to close")
                }
            }
        }

        PreviewChrome(
            geo = geo,
            signal = viewModel.isVideoPlaying.value,
            lnb = settings.lnbFreq,
            transponder = settings.transponder
        )
    }
}

@Composable
private fun OptionList(
    options: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, option ->
            OptionRow(
                text = "${index + 1}. $option",
                active = index == selected,
                onClick = { onSelect(index) }
            )
        }
    }
}

@Composable
private fun SignalBlock() {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SignalBar(stringResource(R.string.signal_intensity), 92, Modifier.weight(1f))
        SignalBar(stringResource(R.string.signal_quality), 78, Modifier.weight(1f))
    }
}

@Composable
private fun SnakeBoard(state: SnakeGameState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.box_snake_game),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.secondary)
            )
            Text(
                text = "${stringResource(R.string.score)}: ${state.score}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.secondary)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(MummyFlixViewModel.GRID_WIDTH.toFloat() / MummyFlixViewModel.GRID_HEIGHT)
                .background(colorResource(R.color.game_bg))
                .border(2.dp, colorResource(R.color.game_border))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cellW = size.width / MummyFlixViewModel.GRID_WIDTH
                val cellH = size.height / MummyFlixViewModel.GRID_HEIGHT
                state.snake.forEachIndexed { index, point ->
                    drawRect(
                        color = if (index == 0) Color.White else Color(0xFFF5E625),
                        topLeft = Offset(point.x * cellW, point.y * cellH),
                        size = Size(cellW - 1f, cellH - 1f)
                    )
                }
                drawRect(
                    color = Color(0xFFFF4D6D),
                    topLeft = Offset(state.food.x * cellW, state.food.y * cellH),
                    size = Size(cellW - 1f, cellH - 1f)
                )
            }

            if (state.isGameOver) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.72f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "GAME OVER\nPress OK to restart",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.secondary),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
