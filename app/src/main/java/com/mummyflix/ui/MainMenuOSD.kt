package com.mummyflix.ui

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
fun MainMenuOSD(viewModel: MummyFlixViewModel) {
    val menuItems = viewModel.menuItems
    val menuIndex = viewModel.menuIndex.value

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
                            text = if (viewModel.appSettings.value.menuLang == "Hindi") stringResource(R.string.mukhya_menu) else stringResource(R.string.main_menu),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.text_dark_blue),
                            letterSpacing = 1.0.sp
                        )
                    }
                }

                // Body - Left Panel (Menu) + Right Panel (Signal Info)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    // Left Panel - Menu List (Flex Mode)
                    Column(
                        modifier = Modifier
                            .width(40.5.percent)
                            .fillMaxHeight()
                            .padding(right = 7.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        menuItems.forEachIndexed { index, item ->
                            val isActive = index == menuIndex
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
                                    .clickable { viewModel.menuIndex.value = index },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = item,
                                    fontSize = 16.sp,
                                    color = if (isActive) Color.Black else colorResource(R.color.text_cyan),
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                    letterSpacing = 0.5.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // Right Panel - Signal Info
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
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
                                    Text("11090F/V/29500S", fontSize = 14.sp, color = colorResource(R.color.text_cyan), letterSpacing = 0.5.sp, modifier = Modifier.weight(1.3f))
                                    Text("DiSEqC:OFF", fontSize = 14.sp, color = colorResource(R.color.text_cyan), letterSpacing = 0.5.sp, modifier = Modifier.weight(1f))
                                }
                                Row {
                                    Text("5010V/6010A/5010P", fontSize = 14.sp, color = colorResource(R.color.text_cyan), letterSpacing = 0.5.sp, modifier = Modifier.weight(1.3f))
                                    Text("22K:OFF", fontSize = 14.sp, color = colorResource(R.color.text_cyan), letterSpacing = 0.5.sp, modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                // Footer - Modified for Menu
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
                                text = stringResource(R.string.move),
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
                                text = stringResource(R.string.confirm),
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
                                text = stringResource(R.string.return_action),
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