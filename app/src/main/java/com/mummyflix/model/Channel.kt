package com.mummyflix.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.Serializable

@Serializable
data class Channel(
    val name: String,
    val url: String,
    var fav: Boolean = false,
    var locked: Boolean = false
) {
    var isPlaying: Boolean = false
        private set
}

@Serializable
data class AppSettings(
    var menuLang: String = "English",
    var audioLang: String = "Hindi",
    var tvSystem: String = "PAL",
    var aspectRatio: String = "Full (16:9)",
    var osdTimeout: String = "5 Sec",
    var lnbFreq: String = "09750",
    var transponder: String = "10990"
)

enum class ViewState {
    LIST, MENU, SUBMENU, AUTOSCAN, FULLSCREEN
}

enum class SubMenuType(
    val title: String,
    val options: List<String>
) {
    PROGRAM_SETUP("Program Setup", listOf(
        "1. Auto Scan",
        "2. Add New Program",
        "3. LNB Freq: 09750",
        "4. Trans Freq: 10990",
        "5. Back to Main Menu"
    )),
    PROGRAM_EDIT("Program Edit", listOf()),
    PROGRAM_INFO("Program Info", listOf()),
    LANGUAGE("Language Setting", listOf(
        "Menu Language: < English >",
        "Audio Language: < Hindi >",
        "Subtitle: < OFF >",
        "Back to Main Menu"
    )),
    SYSTEM_SETTING("System Setting", listOf(
        "TV System: < PAL >",
        "Screen Ratio: < Full (16:9) >",
        "OSD Timeout: < 5 Sec >",
        "Restore Factory Default",
        "Back to Main Menu"
    )),
    GAME("Box Snake Game", listOf()),
    SYSTEM_INFO("System Information", listOf())
}

data class ScanProgress(
    var progress: Int = 0,
    var tpNum: Int = 1,
    var transFreq: String = "10990",
    var symbolRate: String = "28490",
    var polarity: String = "H",
    var tvScanned: Int = 0,
    var radioScanned: Int = 0
)

data class SnakeGameState(
    val snake: List<Point> = listOf(Point(5, 5), Point(4, 5), Point(3, 5)),
    val direction: Point = Point(1, 0),
    val food: Point = Point(10, 6),
    val score: Int = 0,
    val isRunning: Boolean = false
)

data class Point(val x: Int, val y: Int)

data class NumericInputState(
    var buffer: String = "",
    var isVisible: Boolean = false
)

data class BannerState(
    var isVisible: Boolean = false,
    var channelName: String = "",
    var channelType: String = "TV Channel"
)