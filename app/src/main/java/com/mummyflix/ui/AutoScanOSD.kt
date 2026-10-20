package com.mummyflix.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mummyflix.R
import com.mummyflix.model.ScanProgress
import com.mummyflix.viewmodel.MummyFlixViewModel

@Composable
fun AutoScanOSD(viewModel: MummyFlixViewModel) {
    val scan: ScanProgress = viewModel.scanProgress.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        colorResource(R.color.bg_dark_blue_1),
                        colorResource(R.color.bg_dark_blue_2),
                        colorResource(R.color.bg_dark_blue_3)
                    )
                )
            )
            .border(3.dp, colorResource(R.color.border_cyan))
    ) {
        OsdHeader(title = stringResource(R.string.auto_scan))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ScanParam("INSAT 4B", "", Modifier.weight(1f))
                ScanParam("TP", scan.tpNum.toString(), Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ScanParam("LNB Freq", viewModel.appSettings.value.lnbFreq, Modifier.weight(1f))
                ScanParam("Symbol Rate", scan.symbolRate, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ScanParam("Trans Freq", scan.transFreq, Modifier.weight(1f))
                ScanParam("Polarity", scan.polarity, Modifier.weight(1f))
            }

            // Progress
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
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
                    )
                }
                Text(
                    text = "${scan.progress}%",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.width(56.dp).padding(start = 10.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ScanColumn(
                    title = stringResource(R.string.tv_channel_list),
                    scanned = scan.tvScanned,
                    prefix = "CHANNEL",
                    modifier = Modifier.weight(1f)
                )
                ScanColumn(
                    title = stringResource(R.string.radio_channel_list),
                    scanned = scan.radioScanned,
                    prefix = "RADIO",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        OsdFooter {
            FooterHint("Scanning transponders — press MENU to finish early")
        }
    }
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
                RoundedCornerShape(4.dp)
            )
            .border(1.dp, colorResource(R.color.bg_blue_5), RoundedCornerShape(4.dp))
            .padding(horizontal = 12.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.text_cyan_light)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.text_cyan_light)
        )
    }
}

@Composable
private fun ScanColumn(
    title: String,
    scanned: Int,
    prefix: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "$title ($scanned)",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(R.color.bg_radial_2))
                .border(1.dp, colorResource(R.color.bg_blue_10))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            val first = (scanned - 5).coerceAtLeast(1)
            (first..scanned).takeLast(6).forEach { number ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "%03d".format(number),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.width(40.dp)
                    )
                    Text(
                        text = "$prefix $number",
                        fontSize = 11.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}
