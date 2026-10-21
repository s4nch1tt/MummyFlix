package com.mummyflix.model

import kotlinx.serialization.Serializable

@Serializable
data class Channel(
    val name: String,
    val url: String,
    var fav: Boolean = false,
    var locked: Boolean = false
) {
    val displayName: String
        get() = name.ifBlank { "Unknown Channel" }
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

enum class SubMenuType(val title: String, val options: List<String>) {
    PROGRAM_SETUP(
        "Program Setup",
        listOf("Auto Scan", "Add New Program", "LNB Frequency", "Transponder Frequency", "Back to Main Menu")
    ),
    PROGRAM_EDIT(
        "Program Edit",
        listOf("Toggle Favorite", "Toggle Lock", "Delete Current Channel", "Back to Main Menu")
    ),
    PROGRAM_INFO(
        "Program Info",
        listOf()
    ),
    LANGUAGE(
        "Language Setting",
        listOf("Menu Language", "Audio Language", "Subtitle", "Back to Main Menu")
    ),
    SYSTEM_SETTING(
        "System Setting",
        listOf("TV System", "Screen Ratio", "OSD Timeout", "Restore Factory Default", "Back to Main Menu")
    ),
    GAME(
        "Box Snake Game",
        listOf()
    ),
    SYSTEM_INFO(
        "System Information",
        listOf()
    )
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
    val isRunning: Boolean = false,
    val isGameOver: Boolean = false
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

/**
 * The footer is a real, focusable part of the remote experience: pressing
 * RIGHT on the channel list moves the highlight into this bar instead of
 * doing nothing (or bouncing the user out of the app).
 */
enum class FooterCommand {
    FOCUS_LIST,
    PAGE_FORWARD,
    ENTER,
    OPEN_MENU,
    EXIT_APP,
    SHOW_INFO,
    CONFIRM,
    BACK_LEVEL,
    DONE_SCAN,
    CHANGE_VALUE
}

data class FooterGroup(
    val keys: List<String>,
    val label: String,
    val command: FooterCommand
)

/**
 * On-screen numeric pad. Fire TV remotes have no digit keys, so the receiver
 * provides a D-pad driven keypad for direct channel entry.
 */
data class KeypadState(
    val isVisible: Boolean = false,
    val buffer: String = "",
    val row: Int = 0,
    val col: Int = 0
)
