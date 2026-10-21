package com.mummyflix

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.graphics.Color
import com.mummyflix.model.ViewState
import com.mummyflix.ui.AutoScanOSD
import com.mummyflix.ui.ChannelBanner
import com.mummyflix.ui.ChannelListOSD
import com.mummyflix.ui.FullscreenMode
import com.mummyflix.ui.MainMenuOSD
import com.mummyflix.ui.NumericInputOSD
import com.mummyflix.ui.SubMenuOSD
import com.mummyflix.ui.VideoPlayerLayer
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
                MummyFlixScreen(viewModel = viewModel)
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
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> { viewModel.handleOkKey(); true }
            KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_MENU, KeyEvent.KEYCODE_ESCAPE -> {
                viewModel.handleMenuKey(); true
            }
            KeyEvent.KEYCODE_INFO, KeyEvent.KEYCODE_I -> { viewModel.showBanner(); true }
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, KeyEvent.KEYCODE_MEDIA_PLAY -> {
                viewModel.handleOkKey(); true
            }
            in KeyEvent.KEYCODE_0..KeyEvent.KEYCODE_9 -> {
                viewModel.handleNumericKey((keyCode - KeyEvent.KEYCODE_0).toString())
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode in KeyEvent.KEYCODE_0..KeyEvent.KEYCODE_9) {
            viewModel.submitNumericInput()
            return true
        }
        return super.onKeyUp(keyCode, event)
    }
}

@Composable
fun MummyFlixScreen(viewModel: MummyFlixViewModel) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        VideoPlayerLayer(viewModel = viewModel)

        when (viewModel.viewState.value) {
            ViewState.LIST -> ChannelListOSD(viewModel)
            ViewState.MENU -> MainMenuOSD(viewModel)
            ViewState.SUBMENU -> SubMenuOSD(viewModel)
            ViewState.AUTOSCAN -> AutoScanOSD(viewModel)
            ViewState.FULLSCREEN -> FullscreenMode(viewModel)
        }

        if (viewModel.bannerState.value.isVisible) {
            ChannelBanner(viewModel.bannerState.value)
        }

        if (viewModel.numericInput.value.isVisible) {
            NumericInputOSD(viewModel.numericInput.value.buffer)
        }
    }
}