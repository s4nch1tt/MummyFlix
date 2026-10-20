package com.mummyflix

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.mummyflix.model.ViewState
import com.mummyflix.ui.*
import com.mummyflix.ui.theme.MummyFlixTheme
import com.mummyflix.viewmodel.MummyFlixViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MummyFlixViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MummyFlixTheme {
                MummyFlixScreen(viewModel = viewModel)
            }
        }
    }

    private fun enableEdgeToEdge() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        window.setNavigationBarColor(android.graphics.Color.TRANSPARENT)
        window.setStatusBarColor(android.graphics.Color.TRANSPARENT)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> { viewModel.moveSelection(-1); true }
            KeyEvent.KEYCODE_DPAD_DOWN -> { viewModel.moveSelection(1); true }
            KeyEvent.KEYCODE_DPAD_LEFT -> { viewModel.handleLeftRight(-1); true }
            KeyEvent.KEYCODE_DPAD_RIGHT -> { viewModel.handleLeftRight(1); true }
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> { viewModel.handleOkKey(); true }
            KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_MENU, KeyEvent.KEYCODE_ESCAPE -> { viewModel.handleMenuKey(); true }
            KeyEvent.KEYCODE_INFO -> { viewModel.showBanner(); true }
            in KeyEvent.KEYCODE_0..KeyEvent.KEYCODE_9 -> {
                val digit = (keyCode - KeyEvent.KEYCODE_0).toString()
                viewModel.handleNumericKey(digit)
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
    Box(modifier = Modifier.fillMaxSize()) {
        // Background
        Box(modifier = Modifier.fillMaxSize().background(Color.Black))

        // Video Layer
        VideoPlayerLayer(viewModel = viewModel)

        // OSD Overlay based on view state
        when (viewModel.viewState.value) {
            ViewState.LIST -> ChannelListOSD(viewModel)
            ViewState.MENU -> MainMenuOSD(viewModel)
            ViewState.SUBMENU -> SubMenuOSD(viewModel)
            ViewState.AUTOSCAN -> AutoScanOSD(viewModel)
            ViewState.FULLSCREEN -> FullscreenMode(viewModel)
        }

        // Channel Banner (in fullscreen)
        if (viewModel.bannerState.value.isVisible) {
            ChannelBanner(viewModel.bannerState.value)
        }

        // Numeric Input OSD
        if (viewModel.numericInput.value.isVisible) {
            NumericInputOSD(viewModel.numericInput.value.buffer)
        }

        // Branding watermark - "Developed by Sanchit"
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            androidx.compose.material3.Text(
                text = "Developed by Sanchit",
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.4f),
                fontStyle = androidx.compose.ui.text.style.FontStyle.Italic
            )
        }
    }
}