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
                MummyFlixScreen(
                    viewModel = viewModel,
                    onExit = { finish() }
                )
            }
        }
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

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> { viewModel.moveSelection(-1); true }
            KeyEvent.KEYCODE_DPAD_DOWN -> { viewModel.moveSelection(1); true }
            KeyEvent.KEYCODE_DPAD_LEFT -> { viewModel.handleLeftRight(-1); true }
            KeyEvent.KEYCODE_DPAD_RIGHT -> { viewModel.handleLeftRight(1); true }
            KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_ENTER,
            KeyEvent.KEYCODE_NUMPAD_ENTER,
            KeyEvent.KEYCODE_BUTTON_A -> { viewModel.handleOkKey(); true }

            // BACK is a real back button: it closes the menu, leaves fullscreen,
            // and exits the app from the root channel list. It never opens the menu.
            KeyEvent.KEYCODE_BACK,
            KeyEvent.KEYCODE_ESCAPE,
            KeyEvent.KEYCODE_BUTTON_B -> { viewModel.handleBackKey(); true }
            KeyEvent.KEYCODE_MENU,
            KeyEvent.KEYCODE_BUTTON_START,
            KeyEvent.KEYCODE_BUTTON_SELECT -> { viewModel.handleMenuKey(); true }

            KeyEvent.KEYCODE_INFO, KeyEvent.KEYCODE_I, KeyEvent.KEYCODE_GUIDE -> {
                viewModel.showBanner(); true
            }

            // Fire TV Stick 3rd gen play/pause controls the stream itself.
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
            KeyEvent.KEYCODE_MEDIA_PLAY,
            KeyEvent.KEYCODE_MEDIA_PAUSE -> { viewModel.togglePlayPause(); true }

            KeyEvent.KEYCODE_MEDIA_REWIND,
            KeyEvent.KEYCODE_MEDIA_PREVIOUS,
            KeyEvent.KEYCODE_CHANNEL_DOWN -> { viewModel.pageChannels(-1); true }
            KeyEvent.KEYCODE_MEDIA_FAST_FORWARD,
            KeyEvent.KEYCODE_MEDIA_NEXT,
            KeyEvent.KEYCODE_CHANNEL_UP -> { viewModel.pageChannels(1); true }

            in KeyEvent.KEYCODE_0..KeyEvent.KEYCODE_9 -> {
                viewModel.handleNumericKey((keyCode - KeyEvent.KEYCODE_0).toString())
                true
            }
            in KeyEvent.KEYCODE_NUMPAD_0..KeyEvent.KEYCODE_NUMPAD_9 -> {
                viewModel.handleNumericKey((keyCode - KeyEvent.KEYCODE_NUMPAD_0).toString())
                true
            }

            else -> super.onKeyDown(keyCode, event)
        }
    }

    /**
     * Some Fire OS builds deliver BACK through the activity callback instead of
     * [onKeyDown]. Route it through the same one-level-up / exit logic.
     */
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        viewModel.handleBackKey()
    }

    // Digits are committed by the 1.4 s timer in the ViewModel, so typing a
    // multi-digit channel is not cut short on remotes that have number keys.
}

@Composable
fun MummyFlixScreen(viewModel: MummyFlixViewModel, onExit: () -> Unit = {}) {
    val viewState = viewModel.viewState.value
    val fullscreen = viewState == ViewState.FULLSCREEN
    val exitRequested = viewModel.exitRequested.value

    LaunchedEffect(exitRequested) {
        if (exitRequested) onExit()
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val geo = OsdGeometry(maxWidth, maxHeight)

        if (!fullscreen) {
            OsdBackdrop()
        }

        // A single player composable at a single call site: entering fullscreen
        // only changes its bounds, so the stream never restarts. During Auto
        // Scan the player stays alive but takes no space.
        val playerModifier = when {
            fullscreen -> Modifier.fillMaxSize()
            viewState == ViewState.AUTOSCAN -> Modifier.size(0.dp)
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
            when (viewState) {
                ViewState.LIST -> ChannelListBody(viewModel, geo)
                ViewState.MENU -> MainMenuBody(viewModel, geo)
                ViewState.SUBMENU -> SubMenuBody(viewModel, geo)
                ViewState.AUTOSCAN -> AutoScanBody(viewModel, geo)
                ViewState.FULLSCREEN -> Unit
            }

            OsdHeaderBar(
                title = headerTitle(viewModel),
                height = geo.headerHeight
            )

            OsdFooterBar(
                groups = viewModel.footerGroups(),
                focusedIndex = viewModel.footerIndex.value,
                height = geo.footerHeight,
                modifier = Modifier.align(Alignment.BottomStart),
                onActivate = viewModel::activateFooter,
                onInfoClick = viewModel::showBanner
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
            ChannelKeypad(
                state = viewModel.keypad.value,
                onSelect = { row, col ->
                    viewModel.selectKeypadCell(row, col)
                    viewModel.activateKeypadCell()
                },
                onClose = viewModel::closeKeypad
            )
        }
    }
}

@Composable
private fun headerTitle(viewModel: MummyFlixViewModel): String {
    return when (viewModel.viewState.value) {
        ViewState.LIST -> if (viewModel.isRadioMode.value) {
            stringResource(R.string.radio_channel_list)
        } else {
            stringResource(R.string.tv_channel_list)
        }

        ViewState.MENU -> mainMenuTitle(viewModel.appSettings.value.menuLang)
        ViewState.SUBMENU -> viewModel.activeSubMenu.value?.title
            ?: mainMenuTitle(viewModel.appSettings.value.menuLang)

        ViewState.AUTOSCAN -> stringResource(R.string.auto_scan)
        ViewState.FULLSCREEN -> ""
    }
}
