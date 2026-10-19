package com.mummyflix.ui

import androidx.compose.animation.animateFloatAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FontStyle
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.percent
import androidx.compose.ui.unit.sp
import com.mummyflix.model.*
import com.mummyflix.viewmodel.MummyFlixViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SubMenuOSD(viewModel: MummyFlixViewModel) {
    val subMenu = viewModel.activeSubMenu.value
    val subMenuIndex = viewModel.subMenuIndex.value

    if (subMenu == null) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.TopStart
    ) {
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
                            text = subMenu.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.text_dark_blue),
                            letterSpacing = 1.0.sp
                        )
                    }
                }

                // Body - Left Panel (Menu) + Right Panel (Option Panel)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    // Left Panel - Submenu Items
                    Column(
                        modifier = Modifier
                            .width(40.5.percent)
                            .fillMaxHeight()
                            .padding(right = 7.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        subMenu.options.forEachIndexed { index, item ->
                            val isActive = index == subMenuIndex
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
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
                                    .clickable { viewModel.subMenuIndex.value = index },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = item,
                                    fontSize = 16.sp,
                                    color = if (isActive) Color.Black else colorResource(R.color.text_cyan),
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                    letterSpacing = 0.5.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    // Right Panel - Option Panel
                    OptionPanelContent(viewModel = viewModel, subMenu = subMenu)
                }

                // Footer
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
                        // Move keys (for non-Game submenus)
                        if (subMenu != SubMenuType.GAME) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                KeyBadge(label = "▲", onClick = { viewModel.moveSelection(-1) })
                                KeyBadge(label = "▼", onClick = { viewModel.moveSelection(1) })
                                Text(
                                    text = stringResource(R.string.move),
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
                                    text = stringResource(R.string.change),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorResource(R.color.text_dark_blue)
                                )
                            }
                        }

                        // OK key
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            KeyBadge(label = "OK")
                            Text(
                                text = stringResource(R.string.select),
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
                                text = stringResource(R.string.back),
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
        }
    }
}

@Composable
fun OptionPanelContent(viewModel: MummyFlixViewModel, subMenu: SubMenuType) {
    val currentChannel = viewModel.activeList.value.getOrNull(viewModel.currentIndex.value)
    val subMenuIndex = viewModel.subMenuIndex.value

    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        colorResource(R.color.bg_dark_blue_4),
                        colorResource(R.color.bg_dark_blue_5)
                    )
                )
            )
            .border(width = 3.dp, color = colorResource(R.color.border_cyan_light)),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        // Top Section - Options
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text(
                text = "[ ${subMenu.title} ]",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.secondary)
            )

            subMenu.options.forEachIndexed { index, item ->
                val isHighlighted = index == subMenuIndex
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp, horizontal = 5.dp)
                        .background(if (isHighlighted) colorResource(R.color.secondary) else Color.Transparent)
                        .border(width = 1.dp, color = if (isHighlighted) Color.White else Color.Transparent)
                        .clip(RoundedCornerShape(3.dp))
                        .clickable { viewModel.subMenuIndex.value = index; viewModel.handleOkKey() }
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = item,
                        fontSize = 14.sp,
                        color = if (isHighlighted) Color.Black else colorResource(R.color.text_cyan),
                        fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        // Bottom Section - Dynamic Content
        when (subMenu) {
            SubMenuType.PROGRAM_SETUP -> ProgramSetupBottomPanel()
            SubMenuType.PROGRAM_EDIT -> ProgramEditBottomPanel(channel = currentChannel)
            SubMenuType.PROGRAM_INFO -> ProgramInfoBottomPanel(channel = currentChannel)
            SubMenuType.LANGUAGE -> LanguageBottomPanel()
            SubMenuType.SYSTEM_SETTING -> SystemSettingBottomPanel()
            SubMenuType.GAME -> GameBottomPanel(viewModel = viewModel)
            SubMenuType.SYSTEM_INFO -> SystemInfoBottomPanel(viewModel = viewModel)
        }
    }
}

@Composable
fun ProgramSetupBottomPanel() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row {
            Text("Signal Intensity: 92%", fontSize = 14.sp, color = colorResource(R.color.text_cyan))
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .background(colorResource(R.color.bg_blue_6))
                .border(width = 1.dp, color = colorResource(R.color.border_cyan))
                .clip(RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .width(92.percent)
                    .height(14.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                colorResource(R.color.gradient_green_start),
                                colorResource(R.color.gradient_green_end)
                            )
                        )
                    )
                    .clip(RoundedCornerShape(3.dp))
            )
        }
        Row {
            Text("Signal Quality: 78%", fontSize = 14.sp, color = colorResource(R.color.text_cyan))
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .background(colorResource(R.color.bg_blue_6))
                .border(width = 1.dp, color = colorResource(R.color.border_cyan))
                .clip(RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .width(78.percent)
                    .height(14.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                colorResource(R.color.gradient_green_start),
                                colorResource(R.color.gradient_green_end)
                            )
                        )
                    )
                    .clip(RoundedCornerShape(3.dp))
            )
        }
    }
}

@Composable
fun ProgramEditBottomPanel(channel: Channel?) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Press OK to toggle Favorite, Lock or Delete channel.",
            fontSize = 12.sp,
            color = colorResource(R.color.border_cyan_light)
        )
    }
}

@Composable
fun ProgramInfoBottomPanel(channel: Channel?) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row {
            Text("Signal Intensity: 90%", fontSize = 14.sp, color = colorResource(R.color.text_cyan))
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .background(colorResource(R.color.bg_blue_6))
                .border(width = 1.dp, color = colorResource(R.color.border_cyan))
                .clip(RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .width(90.percent)
                    .height(14.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                colorResource(R.color.gradient_green_start),
                                colorResource(R.color.gradient_green_end)
                            )
                        )
                    )
                    .clip(RoundedCornerShape(3.dp))
            )
        }
        Row {
            Text("Signal Quality: 76%", fontSize = 14.sp, color = colorResource(R.color.text_cyan))
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .background(colorResource(R.color.bg_blue_6))
                .border(width = 1.dp, color = colorResource(R.color.border_cyan))
                .clip(RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .width(76.percent)
                    .height(14.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                colorResource(R.color.gradient_green_start),
                                colorResource(R.color.gradient_green_end)
                            )
                        )
                    )
                    .clip(RoundedCornerShape(3.dp))
            )
        }
    }
}

@Composable
fun LanguageBottomPanel() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Tap or press ◄► / OK to switch language.",
            fontSize = 12.sp,
            color = colorResource(R.color.border_cyan_light)
        )
    }
}

@Composable
fun SystemSettingBottomPanel() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Use OK or ◄► to adjust video & system settings.",
            fontSize = 12.sp,
            color = colorResource(R.color.border_cyan_light)
        )
    }
}

@Composable
fun GameBottomPanel(viewModel: MummyFlixViewModel) {
    val snakeState = viewModel.snakeGameState.value

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.box_snake_game),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.secondary)
            )
            Text(
                text = "Score: ${snakeState.score}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.secondary)
            )
        }

        // Snake Game Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.625f) // 260/160
                .background(colorResource(R.color.game_bg))
                .border(width = 2.dp, color = colorResource(R.color.game_border))
                .clip(RoundedCornerShape(0.dp))
        ) {
            // Snake segments
            snakeState.snake.forEach { point ->
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(colorResource(R.color.snake_yellow))
                        .offset(
                            x = (point.x * 10).dp,
                            y = (point.y * 10).dp
                        )
                )
            }
            // Food
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(colorResource(R.color.food_red))
                    .offset(
                        x = (snakeState.food.x * 10).dp,
                        y = (snakeState.food.y * 10).dp
                    )
            )
        }

        Text(
            text = stringResource(R.string.use_arrows_to_move),
            fontSize = 11.sp,
            color = colorResource(R.color.border_cyan_light),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun SystemInfoBottomPanel(viewModel: MummyFlixViewModel) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row {
            Text("Satellite:", fontSize = 14.sp, color = colorResource(R.color.text_cyan), modifier = Modifier.weight(1f))
            Text("INSAT 4B", fontSize = 14.sp, color = colorResource(R.color.text_white), fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        }
        Row {
            Text("H/W Version:", fontSize = 14.sp, color = colorResource(R.color.text_cyan), modifier = Modifier.weight(1f))
            Text("GX6605S V1.2", fontSize = 14.sp, color = colorResource(R.color.text_white), fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        }
        Row {
            Text("S/W Version:", fontSize = 14.sp, color = colorResource(R.color.text_cyan), modifier = Modifier.weight(1f))
            Text("V3.04-IND", fontSize = 14.sp, color = colorResource(R.color.text_white), fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        }
        Row {
            Text("Scanned TV / Radio:", fontSize = 14.sp, color = colorResource(R.color.text_cyan), modifier = Modifier.weight(1f))
            Text("${viewModel.allChannels.value.size} / ${viewModel.radioChannels.value.size}", fontSize = 14.sp, color = colorResource(R.color.text_white), fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        }
        Row {
            Text("Last Update:", fontSize = 14.sp, color = colorResource(R.color.text_cyan), modifier = Modifier.weight(1f))
            Text("2026-10-19", fontSize = 14.sp, color = colorResource(R.color.text_white), fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        }

        // OK / Return button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .background(colorResource(R.color.secondary))
                .clip(RoundedCornerShape(3.dp))
                .clickable { viewModel.handleMenuKey() }
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.ok_return),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}