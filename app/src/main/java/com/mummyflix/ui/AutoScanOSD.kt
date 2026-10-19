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
fun AutoScanOSD(viewModel: MummyFlixViewModel) {
    val scanProgress = viewModel.scanProgress.value

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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
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
                            text = stringResource(R.string.auto_scan),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.text_dark_blue),
                            letterSpacing = 1.0.sp
                        )
                    }
                }

                // Top Grid - Scan Parameters
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ScanParamBox(label = "INSAT 4B", value = "")
                        ScanParamBox(label = "TP", value = scanProgress.tpNum.toString())
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ScanParamBox(label = "LNB Freq", value = scanProgress.transFreq)
                        ScanParamBox(label = "Symbol Rate", value = scanProgress.symbolRate)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ScanParamBox(label = "Trans Freq", value = scanProgress.transFreq)
                        ScanParamBox(label = "Polarity", value = scanProgress.polarity)
                    }
                }

                // Progress Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(16.dp)
                            .background(colorResource(R.color.scan_bar_bg))
                            .border(width = 1.5.dp, color = colorResource(R.color.scan_bar_border))
                            .clip(RoundedCornerShape(3.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width("${scanProgress.progress}%")
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            colorResource(R.color.gradient_yellow_start),
                                            colorResource(R.color.gradient_yellow_end)
                                        )
                                    )
                                )
                                .clip(RoundedCornerShape(3.dp))
                                .border(end = BorderStroke(2.dp, Color.White))
                        )
                    }
                    Text(
                        text = "${scanProgress.progress}%",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.width(48.dp).padding(start = 12.dp)
                    )
                }

                // Channel Lists Grid
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // TV Channel List Column
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp, 14.dp)
                                    .border(width = 2.dp, color = Color.White)
                            ) {
                                // TV icon - simple representation
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Transparent)
                                )
                            }
                            Text(
                                text = stringResource(R.string.tv_channel_list),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(colorResource(R.color.bg_radial_2))
                                .border(width = 1.5.dp, color = colorResource(R.color.bg_blue_10))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Simulated scanned channels
                                repeat(6) { i ->
                                    val chNum = scanProgress.tvScanned - 5 + i
                                    if (chNum > 0 && chNum <= scanProgress.tvScanned) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "%03d".format(chNum),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.width(50.dp)
                                            )
                                            Text(
                                                text = "CHANNEL ${chNum}",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Radio Channel List Column
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp, 14.dp)
                                    .border(width = 2.dp, color = Color.White)
                                    .background(
                                        Brush.radialGradient(
                                            center = androidx.compose.ui.graphics.Center(0.7f, 0.5f),
                                            radius = 50.percent,
                                            colors = listOf(Color.White, Color.Transparent)
                                        )
                                    )
                            )
                            Text(
                                text = stringResource(R.string.radio_channel_list),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(colorResource(R.color.bg_radial_2))
                                .border(width = 1.5.dp, color = colorResource(R.color.bg_blue_10))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                repeat(6) { i ->
                                    val chNum = scanProgress.radioScanned - 5 + i
                                    if (chNum > 0 && chNum <= scanProgress.radioScanned) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "%03d".format(chNum),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.width(50.dp)
                                            )
                                            Text(
                                                text = "RADIO ${chNum}",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScanParamBox(label: String, value: String) {
    Box(
        modifier = Modifier
            .weight(1f)
            .padding(horizontal = 7.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        colorResource(R.color.bg_blue_1),
                        colorResource(R.color.bg_blue_4)
                    )
                )
            )
            .border(width = 1.5.dp, color = colorResource(R.color.bg_blue_5))
            .clip(RoundedCornerShape(5.dp))
            .padding(horizontal = 14.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.text_cyan_light),
                letterSpacing = 0.5.sp
            )
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.text_cyan_light),
                letterSpacing = 0.5.sp
            )
        }
    }
}