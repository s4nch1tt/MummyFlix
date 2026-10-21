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
import com.mummyflix.viewmodel.MummyFlixViewModel

/** Main menu body: the menu occupies the left panel, preview stays live. */
@Composable
fun MainMenuBody(viewModel: MummyFlixViewModel, geo: OsdGeometry) {
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

        PreviewChrome(
            geo = geo,
            signal = viewModel.isVideoPlaying.value,
            lnb = settings.lnbFreq,
            transponder = settings.transponder
        )
    }
}

/** Title shown in the OSD header for the menu screen. */
@Composable
fun mainMenuTitle(language: String): String =
    if (language == "Hindi") stringResource(R.string.mukhya_menu) else stringResource(R.string.main_menu)
