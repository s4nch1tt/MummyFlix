package com.mummyflix

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mummyflix.model.ViewState
import com.mummyflix.ui.AutoScanBody
import com.mummyflix.ui.ChannelBanner
import com.mummyflix.ui.ChannelKeypad
import com.mummyflix.ui.ChannelListBody
import com.mummyflix.ui.MainMenuBody
import com.mummyflix.ui.NumericInputOSD
import com.mummyflix.ui.OsdBackdrop
import com.mummyflix.ui.OsdFooterBar
import com.mummyflix.ui.OsdFrameBorder
import com.mummyflix.ui.OsdGeometry
import com.mummyflix.ui.OsdHeaderBar
import com.mummyflix.ui.SubMenuBody
import com.mummyflix.ui.VideoPlayerLayer
import com.mummyflix.ui.mainMenuTitle
import com.mummyflix.ui.theme.MummyFlixTheme
import com.mummyflix.viewmodel.MummyFlixViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MummyFlixViewModel
        get() = (application as MummyFlixApplication).viewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUi()
        setContent {
            MummyFlixTheme {
                MummyFlixScreen(viewModel = viewModel, onExit = { finish() })
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUi()
    }

    private fun hideSystemUi() {
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            )
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
    }

    /**
     * Amazon Fire TV Stick HD (3rd gen) remote map:
     *  - D-pad + Select : list / menu / footer navigation
     *  - Back           : one level up; exits the app from the channel list
     *  - Menu (☰)       : opens the receiver menu
     *  - Play/Pause     : toggle fullscreen
     *  - Rewind / FF    : previous / next page
     *  - Channel +/-    : previous / next channel
     *  - Info / Guide   : channel banner
     *  - Volume / Mute  : passed to the TV (never consumed)
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_CHANNEL_UP,
            KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                viewModel.moveSelection(-1); true
            }

            KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_CHANNEL_DOWN,
            KeyEvent.KEYCODE_MEDIA_NEXT -> {
                viewModel.moveSelection(1); true
            }

            KeyEvent.KEYCODE_DPAD_LEFT -> { viewModel.handleLeftRight(-1); true }
            KeyEvent.KEYCODE_DPAD_RIGHT -> { viewModel.handleLeftRight(1); true }

            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER,
            KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                viewModel.handleOkKey(); true
            }

            KeyEvent.KEYCODE_BACK -> { viewModel.handleBackKey(); true }
            KeyEvent.KEYCODE_MENU, KeyEvent.KEYCODE_ESCAPE -> {
                viewModel.handleMenuKey(); true
            }

            KeyEvent.KEYCODE_INFO, KeyEvent.KEYCODE_GUIDE, KeyEvent.KEYCODE_I -> {
                viewModel.showBanner(); true
            }

            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, KeyEvent.KEYCODE_MEDIA_PLAY -> {
                viewModel.toggleFullscreen(); true
            }

            KeyEvent.KEYCODE_MEDIA_REWIND, KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD -> {
                viewModel.pageChannels(-1); true
            }

            KeyEvent.KEYCODE_MEDIA_FAST_FORWARD, KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD -> {
                viewModel.pageChannels(1); true
            }

            in KeyEvent.KEYCODE_0..KeyEvent.KEYCODE_9 -> {
                viewModel.handleNumericKey((keyCode - KeyEvent.KEYCODE_0).toString())
                true
            }

            else -> super.onKeyDown(keyCode, event)
        }
    }
}

@Composable
fun MummyFlixScreen(viewModel: MummyFlixViewModel, onExit: () -> Unit) {
    val exitRequested by viewModel.exitRequested
    LaunchedEffect(exitRequested) {
        if (exitRequested) onExit()
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        val geo = remember(maxWidth, maxHeight) { OsdGeometry(maxWidth, maxHeight) }
        val state = viewModel.viewState.value
        val fullscreen = state == ViewState.FULLSCREEN
        val isRadio = viewModel.isRadioMode.value
        val settings = viewModel.appSettings.value

        if (!fullscreen) {
            OsdBackdrop()
        }

        val playerModifier = when (state) {
            ViewState.FULLSCREEN -> Modifier.fillMaxSize()
            ViewState.AUTOSCAN -> Modifier.size(0.dp)
            else -> Modifier
                .offset(geo.previewLeft, geo.previewTop)
                .size(geo.previewWidth, geo.previewHeight)
        }

        VideoPlayerLayer(
            viewModel = viewModel,
            modifier = playerModifier,
            fullscreen = fullscreen
        )

        if (!fullscreen) {
            val title = when (state) {
                ViewState.LIST -> stringResource(
                    if (isRadio) R.string.radio_channel_list else R.string.tv_channel_list
                )

                ViewState.MENU -> mainMenuTitle(settings.menuLang)
                ViewState.SUBMENU -> viewModel.activeSubMenu.value?.title ?: ""
                ViewState.AUTOSCAN -> stringResource(R.string.auto_scan)
                ViewState.FULLSCREEN -> ""
            }

            OsdHeaderBar(
                title = title,
                height = geo.headerHeight,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            when (state) {
                ViewState.LIST -> ChannelListBody(viewModel, geo)
                ViewState.MENU -> MainMenuBody(viewModel, geo)
                ViewState.SUBMENU -> SubMenuBody(viewModel, geo)
                ViewState.AUTOSCAN -> AutoScanBody(viewModel, geo)
                ViewState.FULLSCREEN -> Unit
            }

            val groups = viewModel.footerGroups()
            OsdFooterBar(
                groups = groups,
                focusedIndex = viewModel.footerIndex.value,
                height = geo.footerHeight,
                modifier = Modifier.align(Alignment.BottomCenter),
                onActivate = { index ->
                    if (index == groups.size) viewModel.showBanner()
                    else viewModel.activateFooter(index)
                }
            )

            OsdFrameBorder()
        }

        if (viewModel.bannerState.value.isVisible) {
            ChannelBanner(viewModel.bannerState.value)
        }

        if (viewModel.numericInput.value.isVisible) {
            NumericInputOSD(viewModel.numericInput.value.buffer)
        }

        if (viewModel.keypad.value.isVisible) {
            ChannelKeypad(viewModel.keypad.value)
        }
    }
}
