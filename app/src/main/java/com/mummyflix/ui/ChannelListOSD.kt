package com.mummyflix.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.mummyflix.R
import com.mummyflix.model.Channel
import com.mummyflix.viewmodel.MummyFlixViewModel

/** TV / Radio channel list body: left list, right live preview. */
@Composable
fun ChannelListBody(viewModel: MummyFlixViewModel, geo: OsdGeometry) {
    val channels: List<Channel> = viewModel.activeList.value
    val currentIndex = viewModel.currentIndex.value
    val settings = viewModel.appSettings.value
    val panelModifier = Modifier
        .offset(geo.leftPanelX, geo.leftPanelY)
        .width(geo.leftPanelWidth)
        .height(geo.leftPanelHeight)

    Box(modifier = Modifier.fillMaxSize()) {
        if (channels.isEmpty()) {
            Box(modifier = panelModifier) {
                OsdEmptyText(
                    text = when {
                        viewModel.isLoading.value -> stringResource(R.string.loading_channels)
                        viewModel.loadError.value != null ->
                            "${stringResource(R.string.playlist_error)}\n\nPress OK to retry"
                        else -> stringResource(R.string.no_channels_scanned)
                    }
                )
            }
        } else {
            ChannelListPanel(
                channels = channels,
                currentIndex = currentIndex,
                rowHeight = geo.rowHeight,
                isPlaying = viewModel.isVideoPlaying.value,
                modifier = panelModifier,
                onSelect = viewModel::playChannel
            )
        }

        PreviewChrome(
            geo = geo,
            signal = viewModel.isVideoPlaying.value,
            lnb = settings.lnbFreq,
            transponder = settings.transponder
        )
    }
}
