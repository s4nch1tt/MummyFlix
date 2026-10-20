package com.mummyflix.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mummyflix.R
import com.mummyflix.model.Channel
import com.mummyflix.model.SnakeGameState
import com.mummyflix.model.SubMenuType
import com.mummyflix.viewmodel.MummyFlixViewModel

@Composable
fun SubMenuOSD(viewModel: MummyFlixViewModel) {
    val subMenu = viewModel.activeSubMenu.value ?: return
    val options = viewModel.currentSubMenuOptions()
    val subMenuIndex = viewModel.subMenuIndex.value

    Column(modifier = Modifier.fillMaxSize()) {
        OsdHeader(title = subMenu.title)

        Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
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
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                if (options.isEmpty()) {
                    ScreenTitle(subMenu.title)
                    Hint("Use the panel on the right. Press MENU or OK to return.")
                } else {
                    options.forEachIndexed { index, option ->
                        val active = index == subMenuIndex
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .background(
                                    if (active) {
                                        Brush.horizontalGradient(
                                            listOf(
                                                colorResource(R.color.secondary),
                                                colorResource(R.color.secondary_variant)
                                            )
                                        )
                                    } else {
                                        Brush.horizontalGradient(
                                            listOf(
                                                colorResource(R.color.bg_blue_1).copy(alpha = 0.6f),
                                                colorResource(R.color.bg_blue_2).copy(alpha = 0.6f)
                                            )
                                        )
                                    },
                                    RoundedCornerShape(3.dp)
                                )
                                .border(
                                    1.dp,
                                    if (active) Color.White else colorResource(R.color.bg_blue_3),
                                    RoundedCornerShape(3.dp)
                                )
                                .clickable { viewModel.selectSubMenuOption(index) },
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = option,
                                fontSize = 12.sp,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                color = if (active) Color.Black else colorResource(R.color.text_cyan),
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(8.dp)
            ) {
                // Transparent window: the live video stays visible behind.
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(3.dp, colorResource(R.color.border_cyan_light))
                )

                SubMenuDetail(
                    viewModel = viewModel,
                    subMenu = subMenu,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }

        OsdFooter {
            if (subMenu != SubMenuType.GAME) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    KeyBadge("▲", onClick = { viewModel.moveSelection(-1) })
                    KeyBadge("▼", onClick = { viewModel.moveSelection(1) })
                    FooterHint(stringResource(R.string.move))
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    KeyBadge("◄", onClick = { viewModel.handleLeftRight(-1) })
                    KeyBadge("►", onClick = { viewModel.handleLeftRight(1) })
                    FooterHint(stringResource(R.string.change))
                }
            } else {
                FooterHint("ARROWS: move snake   OK: restart")
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                KeyBadge("OK", onClick = viewModel::handleOkKey)
                FooterHint(stringResource(R.string.select))
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                KeyBadge("MENU", onClick = viewModel::handleMenuKey)
                FooterHint(stringResource(R.string.back))
            }
            InfoCircle(onClick = viewModel::showBanner)
        }
    }
}

@Composable
private fun SubMenuDetail(
    viewModel: MummyFlixViewModel,
    subMenu: SubMenuType,
    modifier: Modifier = Modifier
) {
    val channel = viewModel.activeList.value.getOrNull(viewModel.currentIndex.value)
    val settings = viewModel.appSettings.value

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.78f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        when (subMenu) {
            SubMenuType.PROGRAM_SETUP -> {
                ScreenTitle("Scan / Satellite")
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    SignalBar(stringResource(R.string.signal_intensity), 92, Modifier.weight(1f))
                    SignalBar(stringResource(R.string.signal_quality), 78, Modifier.weight(1f))
                }
                Hint("LNB ${settings.lnbFreq}  •  TP ${settings.transponder}  •  ${settings.tvSystem}")
            }

            SubMenuType.PROGRAM_EDIT -> {
                ScreenTitle("Edit: ${channel?.displayName ?: "—"}")
                Hint("Favorite: ${if (channel?.fav == true) "YES" else "NO"}   Locked: ${if (channel?.locked == true) "YES" else "NO"}")
                Hint(stringResource(R.string.press_ok_to_toggle))
            }

            SubMenuType.PROGRAM_INFO -> {
                ScreenTitle(channel?.displayName ?: "No channel")
                InfoRow("Number", (viewModel.currentIndex.value + 1).toString())
                InfoRow("Type", if (viewModel.isRadioMode.value) "Radio" else "TV")
                InfoRow("Frequency", "${settings.transponder} MHz")
                InfoRow("Symbol Rate", "28490")
                InfoRow("Favorite", if (channel?.fav == true) "YES" else "NO")
                InfoRow("Locked", if (channel?.locked == true) "YES" else "NO")
                if (channel != null && channel.url.isNotBlank()) {
                    Text(
                        text = channel.url,
                        fontSize = 9.sp,
                        color = colorResource(R.color.border_cyan_light),
                        maxLines = 1
                    )
                }
            }

            SubMenuType.LANGUAGE -> {
                ScreenTitle("Languages")
                InfoRow("Menu Language", settings.menuLang)
                InfoRow("Audio Language", settings.audioLang)
                InfoRow("Subtitle", "OFF")
                Hint(stringResource(R.string.tap_or_press_to_switch))
            }

            SubMenuType.SYSTEM_SETTING -> {
                ScreenTitle("System")
                InfoRow("TV System", settings.tvSystem)
                InfoRow("Screen Ratio", settings.aspectRatio)
                InfoRow("OSD Timeout", settings.osdTimeout)
                Hint(stringResource(R.string.use_ok_to_adjust))
            }

            SubMenuType.GAME -> SnakeBoard(viewModel.snakeGameState.value)

            SubMenuType.SYSTEM_INFO -> {
                ScreenTitle("System Information")
                InfoRow("Satellite", "INSAT 4B")
                InfoRow("H/W Version", "GX6605S V1.2")
                InfoRow("S/W Version", "3.0.4-IND")
                InfoRow("TV Channels", viewModel.allChannels.value.size.toString())
                InfoRow("Radio Channels", viewModel.radioChannels.value.size.toString())
                InfoRow("Last Update", "2026-10-19")
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = colorResource(R.color.text_cyan))
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun SnakeBoard(state: SnakeGameState) {
    Column(
        modifier = Modifier.fillMaxWidth(),
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
                        .background(Color.Black.copy(alpha = 0.7f)),
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

        Text(
            text = stringResource(R.string.use_arrows_to_move),
            fontSize = 10.sp,
            color = colorResource(R.color.border_cyan_light),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
