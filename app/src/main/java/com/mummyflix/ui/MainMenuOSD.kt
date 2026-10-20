package com.mummyflix.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mummyflix.R
import com.mummyflix.viewmodel.MummyFlixViewModel

@Composable
fun MainMenuOSD(viewModel: MummyFlixViewModel) {
    val menuItems = viewModel.menuItems
    val menuIndex = viewModel.menuIndex.value

    Column(modifier = Modifier.fillMaxSize()) {
        OsdHeader(
            title = if (viewModel.appSettings.value.menuLang == "Hindi") {
                stringResource(R.string.mukhya_menu)
            } else {
                stringResource(R.string.main_menu)
            }
        )

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
                menuItems.forEachIndexed { index, item ->
                    val active = index == menuIndex
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
                            .clickable { viewModel.selectMenuItem(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}. $item",
                            fontSize = 13.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                            color = if (active) Color.Black else colorResource(R.color.text_cyan),
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }

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

                // Signal / transponder readout over the video preview.
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.72f))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.signal_intensity),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.border_cyan_light)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        SignalBar(stringResource(R.string.signal_intensity), 92, Modifier.weight(1f))
                        SignalBar(stringResource(R.string.signal_quality), 78, Modifier.weight(1f))
                    }
                    Text(
                        text = "${viewModel.appSettings.value.transponder}V / 29500 / DiSEqC:OFF   22K:OFF",
                        fontSize = 10.sp,
                        color = colorResource(R.color.text_cyan)
                    )
                    Text(
                        text = "LNB ${viewModel.appSettings.value.lnbFreq}   ${viewModel.appSettings.value.tvSystem}",
                        fontSize = 10.sp,
                        color = colorResource(R.color.text_cyan)
                    )
                }
            }
        }

        OsdFooter {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                KeyBadge("▲", onClick = { viewModel.moveSelection(-1) })
                KeyBadge("▼", onClick = { viewModel.moveSelection(1) })
                FooterHint(stringResource(R.string.move))
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
