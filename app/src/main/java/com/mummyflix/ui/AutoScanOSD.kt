package com.mummyflix.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mummyflix.R
import com.mummyflix.model.Channel
import com.mummyflix.model.ScanProgress
import com.mummyflix.viewmodel.MummyFlixViewModel

/** Simulated satellite scan body, matching the reference OSD exactly. */
@Composable
fun AutoScanBody(viewModel: MummyFlixViewModel, geo: OsdGeometry) {
    val scan: ScanProgress = viewModel.scanProgress.value
    val settings = viewModel.appSettings.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = geo.headerHeight + 8.dp,
                bottom = geo.footerHeight + 6.dp
            ),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ScanParam("INSAT 4B", "", Modifier.weight(1f))
            ScanParam("TP", scan.tpNum.toString(), Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ScanParam("LNB Freq", settings.lnbFreq, Modifier.weight(1f))
            ScanParam("Symbol Rate", scan.symbolRate, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ScanParam("Trans Freq", scan.transFreq, Modifier.weight(1f))
            ScanParam("Polarity", scan.polarity, Modifier.weight(1f))
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(16.dp)
                    .background(colorResource(R.color.scan_bar_bg), RoundedCornerShape(3.dp))
                    .border(1.dp, colorResource(R.color.scan_bar_border), RoundedCornerShape(3.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(scan.progress.coerceIn(0, 100) / 100f)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    colorResource(R.color.gradient_yellow_start),
                                    colorResource(R.color.gradient_yellow_end)
                                )
                            ),
                            RoundedCornerShape(3.dp)
                        )
                        .border(1.dp, Color.White, RoundedCornerShape(3.dp))
                )
            }
            Text(
                text = "${scan.progress}%",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.width(58.dp).padding(start = 10.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ScanColumn(
                title = stringResource(R.string.tv_channel_list),
                master = viewModel.masterTvChannels.value,
                scanned = scan.tvScanned,
                modifier = Modifier.weight(1f)
            )
            ScanColumn(
                title = stringResource(R.string.radio_channel_list),
                master = viewModel.masterRadioChannels.value,
                scanned = scan.radioScanned,
                modifier = Modifier.weight(1f)
            )
        }
    }

    Spacer(Modifier.height(0.dp))
}

@Composable
private fun ScanParam(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    listOf(
                        colorResource(R.color.bg_blue_1),
                        colorResource(R.color.bg_blue_4)
                    )
                ),
                RoundedCornerShape(5.dp)
            )
            .border(1.dp, colorResource(R.color.bg_blue_5), RoundedCornerShape(5.dp))
            .padding(horizontal = 14.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.text_cyan_light)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.text_cyan_light)
        )
    }
}

@Composable
private fun ScanColumn(
    title: String,
    master: List<Channel>,
    scanned: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (title.startsWith("Radio")) "▤" else "▭",
                fontSize = 13.sp,
                color = Color.White,
                modifier = Modifier.padding(end = 6.dp)
            )
            Text(
                text = "$title ($scanned)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(R.color.bg_radial_2), RoundedCornerShape(2.dp))
                .border(1.dp, colorResource(R.color.bg_blue_10), RoundedCornerShape(2.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            val visible = master.take(scanned.coerceAtMost(master.size)).takeLast(6)
            val startNumber = (scanned - visible.size + 1).coerceAtLeast(1)
            visible.forEachIndexed { offset, channel ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "%03d".format(startNumber + offset),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.width(46.dp)
                    )
                    Text(
                        text = channel.displayName
                            .replace(Regex("\\(.*\\)"), "")
                            .trim()
                            .uppercase()
                            .take(18),
                        fontSize = 12.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
