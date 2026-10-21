package com.mummyflix.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mummyflix.model.*
import com.mummyflix.preferences.MummyFlixPreferences
import com.mummyflix.repository.ChannelRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MummyFlixViewModel(
    private val repository: ChannelRepository,
    private val preferences: MummyFlixPreferences
) : ViewModel() {

    // ------------------------------------------------------------------ data

    private val _masterTvChannels = mutableStateOf<List<Channel>>(emptyList())
    val masterTvChannels: State<List<Channel>> = _masterTvChannels

    private val _masterRadioChannels = mutableStateOf<List<Channel>>(emptyList())
    val masterRadioChannels: State<List<Channel>> = _masterRadioChannels

    private val _allChannels = mutableStateOf<List<Channel>>(emptyList())
    val allChannels: State<List<Channel>> = _allChannels

    private val _radioChannels = mutableStateOf<List<Channel>>(emptyList())
    val radioChannels: State<List<Channel>> = _radioChannels

    private val _activeList = mutableStateOf<List<Channel>>(emptyList())
    val activeList: State<List<Channel>> = _activeList

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    private val _loadError = mutableStateOf<String?>(null)
    val loadError: State<String?> = _loadError

    // ------------------------------------------------------------- UI state

    private val _viewState = mutableStateOf(ViewState.LIST)
    val viewState: State<ViewState> = _viewState

    private val _isRadioMode = mutableStateOf(false)
    val isRadioMode: State<Boolean> = _isRadioMode

    private val _currentIndex = mutableStateOf(0)
    val currentIndex: State<Int> = _currentIndex

    private val _playingIndex = mutableStateOf(-1)
    val playingIndex: State<Int> = _playingIndex

    private val _menuIndex = mutableStateOf(0)
    val menuIndex: State<Int> = _menuIndex

    private val _subMenuIndex = mutableStateOf(0)
    val subMenuIndex: State<Int> = _subMenuIndex

    private val _activeSubMenu = mutableStateOf<SubMenuType?>(null)
    val activeSubMenu: State<SubMenuType?> = _activeSubMenu

    private val _scanProgress = mutableStateOf(ScanProgress())
    val scanProgress: State<ScanProgress> = _scanProgress

    private val _snakeGameState = mutableStateOf(SnakeGameState())
    val snakeGameState: State<SnakeGameState> = _snakeGameState

    private val _numericInput = mutableStateOf(NumericInputState())
    val numericInput: State<NumericInputState> = _numericInput

    private val _bannerState = mutableStateOf(BannerState())
    val bannerState: State<BannerState> = _bannerState

    private val _isVideoPlaying = mutableStateOf(false)
    val isVideoPlaying: State<Boolean> = _isVideoPlaying

    private val _showNoSignal = mutableStateOf(false)
    val showNoSignal: State<Boolean> = _showNoSignal

    private val _videoUrl = mutableStateOf("")
    val videoUrl: State<String> = _videoUrl

    private val _appSettings = mutableStateOf(AppSettings())
    val appSettings: State<AppSettings> = _appSettings

    /** null means the content (channel list / menu) owns the highlight. */
    private val _footerIndex = mutableStateOf<Int?>(null)
    val footerIndex: State<Int?> = _footerIndex

    private val _exitRequested = mutableStateOf(false)
    val exitRequested: State<Boolean> = _exitRequested

    private val _keypad = mutableStateOf(KeypadState())
    val keypad: State<KeypadState> = _keypad

    private var bannerJob: Job? = null
    private var numericJob: Job? = null
    private var snakeJob: Job? = null
    private var scanJob: Job? = null

    init {
        loadPreferences()
        loadPlaylist()
    }

    // ------------------------------------------------------------ bootstrap

    private fun loadPreferences() {
        viewModelScope.launch {
            val settings = AppSettings(
                menuLang = preferences.menuLang.first(),
                audioLang = preferences.audioLang.first(),
                tvSystem = preferences.tvSystem.first(),
                aspectRatio = preferences.aspectRatio.first(),
                osdTimeout = preferences.osdTimeout.first(),
                lnbFreq = preferences.lnbFreq.first(),
                transponder = preferences.transponder.first()
            )
            _appSettings.value = settings
            _isRadioMode.value = preferences.isRadioMode.first()
            _currentIndex.value = preferences.lastChannelIndex.first()
        }
    }

    private fun loadPlaylist() {
        viewModelScope.launch {
            _isLoading.value = true
            _loadError.value = null
            repository.loadPlaylist()
                .onSuccess { channels ->
                    val favs = preferences.favChannels.first()
                    val locked = preferences.lockedChannels.first()
                    val decorated = channels.map {
                        it.copy(
                            fav = favs.contains(it.url),
                            locked = locked.contains(it.url)
                        )
                    }
                    val radio = repository.buildRadioChannels(decorated)

                    _masterTvChannels.value = decorated
                    _masterRadioChannels.value = radio
                    _allChannels.value = decorated
                    _radioChannels.value = radio

                    _activeList.value = if (_isRadioMode.value) radio else decorated
                    _isLoading.value = false
                    _loadError.value = null

                    if (_activeList.value.isNotEmpty()) {
                        val start = _currentIndex.value.coerceIn(0, _activeList.value.lastIndex)
                        playChannel(start)
                    } else {
                        _showNoSignal.value = true
                    }
                }
                .onFailure { error ->
                    _isLoading.value = false
                    _loadError.value = error.message ?: "Playlist unavailable"
                    _showNoSignal.value = true
                }
        }
    }

    fun retryLoading() {
        if (_isLoading.value) return
        loadPlaylist()
    }

    // ------------------------------------------------------------- playback

    fun playChannel(index: Int) {
        val list = _activeList.value
        if (index !in list.indices) return

        _playingIndex.value = index
        _currentIndex.value = index
        _videoUrl.value = list[index].url
        _isVideoPlaying.value = false
        _showNoSignal.value = list[index].url.isBlank()

        viewModelScope.launch(Dispatchers.IO) {
            preferences.setLastChannelIndex(index)
        }
    }

    fun onVideoPlaying() {
        _isVideoPlaying.value = true
        _showNoSignal.value = false
    }

    fun onVideoError() {
        _isVideoPlaying.value = false
        _showNoSignal.value = true
    }

    fun toggleFullscreen() {
        when (_viewState.value) {
            ViewState.FULLSCREEN -> exitFullscreen()
            ViewState.LIST -> if (_activeList.value.isNotEmpty()) enterFullscreen()
            else -> Unit
        }
    }

    /** Page through the list ten channels at a time (◄ / ► and FF / RW keys). */
    fun pageChannels(dir: Int) {
        val list = _activeList.value
        if (list.isEmpty()) return
        var next = _currentIndex.value + dir * PAGE_SIZE
        if (next > list.lastIndex) next = 0
        if (next < 0) next = (list.lastIndex / PAGE_SIZE) * PAGE_SIZE
        playChannel(next)
    }

    // ------------------------------------------------------------ navigation

    fun moveSelection(delta: Int) {
        if (_keypad.value.isVisible) {
            moveKeypadSelection(delta, 0)
            return
        }

        when (_viewState.value) {
            ViewState.LIST -> {
                if (_footerIndex.value != null) {
                    _footerIndex.value = null
                    return
                }
                val list = _activeList.value
                if (list.isEmpty()) return
                val next = (_currentIndex.value + delta + list.size) % list.size
                playChannel(next)
            }

            ViewState.MENU -> {
                if (_footerIndex.value != null) {
                    _footerIndex.value = null
                    return
                }
                _menuIndex.value = (_menuIndex.value + delta + MENU_ITEMS.size) % MENU_ITEMS.size
            }

            ViewState.SUBMENU -> when (_activeSubMenu.value) {
                SubMenuType.GAME -> changeSnakeDirection(if (delta < 0) Point(0, -1) else Point(0, 1))
                else -> {
                    val count = currentSubMenuOptions().size
                    if (count > 0) {
                        _subMenuIndex.value = (_subMenuIndex.value + delta + count) % count
                    }
                }
            }

            ViewState.FULLSCREEN -> {
                val list = _activeList.value
                if (list.isEmpty()) return
                val next = (_currentIndex.value + delta + list.size) % list.size
                playChannel(next)
                showBanner()
            }

            ViewState.AUTOSCAN -> Unit
        }
    }

    fun handleLeftRight(dir: Int) {
        if (_keypad.value.isVisible) {
            moveKeypadSelection(0, dir)
            return
        }

        when (_viewState.value) {
            ViewState.LIST -> {
                if (_footerIndex.value != null) {
                    moveFooterSelection(dir)
                } else if (dir > 0) {
                    // RIGHT on a channel name moves the highlight into the
                    // footer (confirm / menu line) instead of leaving the app.
                    _footerIndex.value = FOOTER_LIST_ENTRY
                } else {
                    pageChannels(-1)
                }
            }

            ViewState.MENU -> {
                if (_footerIndex.value != null) {
                    moveFooterSelection(dir)
                } else if (dir > 0) {
                    _footerIndex.value = FOOTER_MENU_ENTRY
                }
            }

            ViewState.SUBMENU -> when (_activeSubMenu.value) {
                SubMenuType.GAME -> changeSnakeDirection(if (dir < 0) Point(-1, 0) else Point(1, 0))
                SubMenuType.LANGUAGE, SubMenuType.SYSTEM_SETTING -> changeSelectedOption(dir)
                else -> Unit
            }

            ViewState.AUTOSCAN -> Unit
            else -> Unit
        }
    }

    fun handleOkKey() {
        if (_keypad.value.isVisible) {
            activateKeypadCell()
            return
        }
        if (_footerIndex.value != null) {
            activateFooter(_footerIndex.value!!)
            return
        }

        when (_viewState.value) {
            ViewState.LIST -> {
                if (_activeList.value.isEmpty() || _loadError.value != null) {
                    retryLoading()
                } else if (_playingIndex.value != _currentIndex.value) {
                    playChannel(_currentIndex.value)
                } else {
                    enterFullscreen()
                }
            }

            ViewState.MENU -> openSelectedMenuOption(MENU_ITEMS[_menuIndex.value])
            ViewState.SUBMENU -> handleSubMenuAction()
            ViewState.FULLSCREEN -> exitFullscreen()
            ViewState.AUTOSCAN -> finalizeAutoScan()
        }
    }

    /** MENU key: always opens the receiver menu. */
    fun handleMenuKey() {
        if (_keypad.value.isVisible) {
            closeKeypad()
            return
        }
        _footerIndex.value = null
        when (_viewState.value) {
            ViewState.AUTOSCAN -> finalizeAutoScan()
            ViewState.SUBMENU -> {
                stopSnakeGame()
                _viewState.value = ViewState.MENU
                _activeSubMenu.value = null
            }

            ViewState.LIST -> {
                _viewState.value = ViewState.MENU
                _menuIndex.value = 2
            }

            ViewState.MENU -> _viewState.value = ViewState.LIST
            ViewState.FULLSCREEN -> {
                _viewState.value = ViewState.MENU
                _bannerState.value = _bannerState.value.copy(isVisible = false)
            }
        }
    }

    /**
     * BACK key: contextual one level up, and from the top-level channel list
     * it actually leaves the app (the old build mapped BACK to "open menu",
     * which made it impossible to quit with the remote).
     */
    fun handleBackKey() {
        if (_keypad.value.isVisible) {
            closeKeypad()
            return
        }
        if (_footerIndex.value != null) {
            _footerIndex.value = null
            return
        }
        when (_viewState.value) {
            ViewState.LIST -> requestExit()
            ViewState.MENU -> {
                _activeSubMenu.value = null
                _viewState.value = ViewState.LIST
            }

            ViewState.SUBMENU -> {
                stopSnakeGame()
                _activeSubMenu.value = null
                _viewState.value = ViewState.MENU
            }

            ViewState.AUTOSCAN -> finalizeAutoScan()
            ViewState.FULLSCREEN -> exitFullscreen()
        }
    }

    fun requestExit() {
        _exitRequested.value = true
    }

    // ------------------------------------------------------- footer / keypad

    fun footerGroups(): List<FooterGroup> = when (_viewState.value) {
        ViewState.LIST -> listOf(
            FooterGroup(listOf("▲", "▼"), "Select", FooterCommand.FOCUS_LIST),
            FooterGroup(listOf("◄", "►"), "Page", FooterCommand.PAGE_FORWARD),
            FooterGroup(listOf("OK"), "Enter", FooterCommand.ENTER),
            FooterGroup(listOf("MENU"), "Menu", FooterCommand.OPEN_MENU),
            FooterGroup(listOf("BACK"), "Exit", FooterCommand.EXIT_APP)
        )

        ViewState.MENU -> listOf(
            FooterGroup(listOf("▲", "▼"), "Move", FooterCommand.FOCUS_LIST),
            FooterGroup(listOf("OK"), "Confirm", FooterCommand.CONFIRM),
            FooterGroup(listOf("MENU"), "Back", FooterCommand.BACK_LEVEL),
            FooterGroup(listOf("BACK"), "Exit", FooterCommand.EXIT_APP)
        )

        ViewState.SUBMENU -> listOf(
            FooterGroup(listOf("▲", "▼"), "Move", FooterCommand.FOCUS_LIST),
            FooterGroup(listOf("◄", "►"), "Change", FooterCommand.CHANGE_VALUE),
            FooterGroup(listOf("OK"), "Select", FooterCommand.CONFIRM),
            FooterGroup(listOf("MENU"), "Back", FooterCommand.BACK_LEVEL)
        )

        ViewState.AUTOSCAN -> listOf(
            FooterGroup(listOf("OK"), "Done", FooterCommand.DONE_SCAN),
            FooterGroup(listOf("MENU"), "Cancel", FooterCommand.DONE_SCAN)
        )

        ViewState.FULLSCREEN -> emptyList()
    }

    fun activateFooter(index: Int) {
        val groups = footerGroups()
        if (groups.isEmpty()) return
        if (index == groups.size) {
            showBanner()
            return
        }
        val command = groups.getOrNull(index)?.command ?: return
        when (command) {
            FooterCommand.FOCUS_LIST -> _footerIndex.value = null
            FooterCommand.PAGE_FORWARD -> {
                pageChannels(1)
                _footerIndex.value = null
            }

            FooterCommand.ENTER -> {
                _footerIndex.value = null
                if (_activeList.value.isEmpty()) retryLoading()
                else if (_playingIndex.value != _currentIndex.value) playChannel(_currentIndex.value)
                else enterFullscreen()
            }

            FooterCommand.OPEN_MENU -> handleMenuKey()
            FooterCommand.EXIT_APP -> requestExit()
            FooterCommand.SHOW_INFO -> showBanner()
            FooterCommand.CONFIRM -> {
                _footerIndex.value = null
                handleOkKey()
            }

            FooterCommand.BACK_LEVEL -> {
                _footerIndex.value = null
                handleMenuKey()
            }

            FooterCommand.DONE_SCAN -> finalizeAutoScan()
            FooterCommand.CHANGE_VALUE -> handleOkKey()
        }
    }

    private fun moveFooterSelection(delta: Int) {
        val count = footerGroups().size + 1 // + info circle
        val current = _footerIndex.value ?: 0
        _footerIndex.value = ((current + delta) % count + count) % count
    }

    fun openKeypad() {
        _footerIndex.value = null
        _keypad.value = KeypadState(isVisible = true)
    }

    fun closeKeypad() {
        _keypad.value = KeypadState()
    }

    private fun moveKeypadSelection(rowDelta: Int, colDelta: Int) {
        val state = _keypad.value
        val row = ((state.row + rowDelta) % 4 + 4) % 4
        val col = ((state.col + colDelta) % 3 + 3) % 3
        _keypad.value = state.copy(row = row, col = col)
    }

    private fun activateKeypadCell() {
        val state = _keypad.value
        when {
            state.row == 3 && state.col == 0 -> {
                _keypad.value = state.copy(buffer = state.buffer.dropLast(1))
            }

            state.row == 3 && state.col == 1 -> appendKeypadDigit("0")
            state.row == 3 && state.col == 2 -> submitKeypad()
            else -> appendKeypadDigit((state.row * 3 + state.col + 1).toString())
        }
    }

    private fun appendKeypadDigit(digit: String) {
        val state = _keypad.value
        val buffer = if (state.buffer.length >= 3) digit else state.buffer + digit
        _keypad.value = state.copy(buffer = buffer)
    }

    private fun submitKeypad() {
        val buffer = _keypad.value.buffer
        closeKeypad()
        val target = buffer.toIntOrNull()?.minus(1) ?: return
        if (target in _activeList.value.indices) {
            _viewState.value = ViewState.LIST
            playChannel(target)
        }
    }

    fun handleNumericKey(digit: String) {
        if (_viewState.value != ViewState.LIST && _viewState.value != ViewState.FULLSCREEN) return

        val current = _numericInput.value.buffer
        val next = if (current.length >= 3) digit else current + digit
        _numericInput.value = NumericInputState(next, true)

        numericJob?.cancel()
        numericJob = viewModelScope.launch {
            delay(1400)
            submitNumericInput()
        }
    }

    fun submitNumericInput() {
        numericJob?.cancel()
        val buffer = _numericInput.value.buffer
        _numericInput.value = NumericInputState("", false)
        val target = buffer.toIntOrNull()?.minus(1) ?: return
        if (target in _activeList.value.indices) {
            playChannel(target)
            if (_viewState.value == ViewState.FULLSCREEN) showBanner()
        }
    }

    /** Called by the touch/click UI; D-pad navigation uses [moveSelection]. */
    fun selectMenuItem(index: Int) {
        _menuIndex.value = index.coerceIn(0, MENU_ITEMS.lastIndex)
    }

    fun selectSubMenuOption(index: Int) {
        val size = currentSubMenuOptions().size
        if (size > 0) _subMenuIndex.value = index.coerceIn(0, size - 1)
    }

    private fun openSelectedMenuOption(option: String) {
        when (option) {
            "TV List" -> switchList(radio = false)
            "Radio List" -> switchList(radio = true)
            "Go to Channel" -> openKeypad()
            "Exit App" -> requestExit()
            "Cancel" -> enterFullscreen()
            else -> {
                val subMenu = SubMenuType.entries.firstOrNull { it.title == option }
                if (subMenu != null) {
                    _activeSubMenu.value = subMenu
                    _subMenuIndex.value = 0
                    _viewState.value = ViewState.SUBMENU
                    if (subMenu == SubMenuType.GAME) startSnakeGame() else stopSnakeGame()
                }
            }
        }
    }

    private fun switchList(radio: Boolean) {
        stopSnakeGame()
        _isRadioMode.value = radio
        _activeList.value = if (radio) _radioChannels.value else _allChannels.value
        _currentIndex.value = 0
        _viewState.value = ViewState.LIST
        _footerIndex.value = null
        if (_activeList.value.isNotEmpty()) playChannel(0)
        viewModelScope.launch { preferences.setRadioMode(radio) }
    }

    // --------------------------------------------------------------- submenu

    /** Option labels with live setting values substituted in. */
    fun currentSubMenuOptions(): List<String> {
        val subMenu = _activeSubMenu.value ?: return emptyList()
        val settings = _appSettings.value
        return subMenu.options.map { option ->
            when (option) {
                "LNB Frequency" -> "$option: ${settings.lnbFreq}"
                "Transponder Frequency" -> "$option: ${settings.transponder}"
                "Menu Language" -> "$option: < ${settings.menuLang} >"
                "Audio Language" -> "$option: < ${settings.audioLang} >"
                "Subtitle" -> "$option: < OFF >"
                "TV System" -> "$option: < ${settings.tvSystem} >"
                "Screen Ratio" -> "$option: < ${settings.aspectRatio} >"
                "OSD Timeout" -> "$option: < ${settings.osdTimeout} >"
                else -> option
            }
        }
    }

    /** LEFT/RIGHT on a settings row cycles the value in that direction. */
    private fun changeSelectedOption(@Suppress("UNUSED_PARAMETER") dir: Int) {
        val subMenu = _activeSubMenu.value ?: return
        val index = _subMenuIndex.value
        when (subMenu) {
            SubMenuType.LANGUAGE -> when (index) {
                0 -> updateSetting(menuLang = if (_appSettings.value.menuLang == "English") "Hindi" else "English")
                1 -> updateSetting(audioLang = if (_appSettings.value.audioLang == "Hindi") "English" else "Hindi")
                else -> Unit
            }

            SubMenuType.SYSTEM_SETTING -> when (index) {
                0 -> updateSetting(tvSystem = if (_appSettings.value.tvSystem == "PAL") "NTSC" else "PAL")
                1 -> updateSetting(
                    aspectRatio = if (_appSettings.value.aspectRatio.startsWith("Full")) "Fit (4:3)"
                    else "Full (16:9)"
                )

                2 -> updateSetting(
                    osdTimeout = when (_appSettings.value.osdTimeout) {
                        "3 Sec" -> "5 Sec"
                        "5 Sec" -> "10 Sec"
                        else -> "3 Sec"
                    }
                )

                else -> Unit
            }

            else -> Unit
        }
        if (subMenu == SubMenuType.PROGRAM_SETUP) {
            if (index == 2) cycleLnbFrequency()
            if (index == 3) cycleTransponder()
        }
    }

    private fun handleSubMenuAction() {
        val subMenu = _activeSubMenu.value ?: return
        val index = _subMenuIndex.value

        when (subMenu) {
            SubMenuType.PROGRAM_SETUP -> when (index) {
                0 -> startAutoScan()
                1 -> switchList(radio = false)
                2 -> cycleLnbFrequency()
                3 -> cycleTransponder()
                4 -> handleMenuKey()
                else -> Unit
            }

            SubMenuType.PROGRAM_EDIT -> when (index) {
                0 -> toggleFavorite()
                1 -> toggleLock()
                2 -> deleteCurrentChannel()
                3 -> handleMenuKey()
                else -> Unit
            }

            SubMenuType.LANGUAGE -> when (index) {
                0 -> updateSetting(menuLang = if (_appSettings.value.menuLang == "English") "Hindi" else "English")
                1 -> updateSetting(audioLang = if (_appSettings.value.audioLang == "Hindi") "English" else "Hindi")
                3 -> handleMenuKey()
                else -> Unit
            }

            SubMenuType.SYSTEM_SETTING -> when (index) {
                0 -> updateSetting(tvSystem = if (_appSettings.value.tvSystem == "PAL") "NTSC" else "PAL")
                1 -> updateSetting(
                    aspectRatio = if (_appSettings.value.aspectRatio.startsWith("Full")) "Fit (4:3)"
                    else "Full (16:9)"
                )

                2 -> updateSetting(
                    osdTimeout = when (_appSettings.value.osdTimeout) {
                        "3 Sec" -> "5 Sec"
                        "5 Sec" -> "10 Sec"
                        else -> "3 Sec"
                    }
                )

                3 -> restoreDefaults()
                4 -> handleMenuKey()
                else -> Unit
            }

            SubMenuType.GAME -> startSnakeGame()
            SubMenuType.PROGRAM_INFO, SubMenuType.SYSTEM_INFO -> handleMenuKey()
        }
    }

    private fun updateSetting(
        menuLang: String? = null,
        audioLang: String? = null,
        tvSystem: String? = null,
        aspectRatio: String? = null,
        osdTimeout: String? = null
    ) {
        val current = _appSettings.value
        _appSettings.value = current.copy(
            menuLang = menuLang ?: current.menuLang,
            audioLang = audioLang ?: current.audioLang,
            tvSystem = tvSystem ?: current.tvSystem,
            aspectRatio = aspectRatio ?: current.aspectRatio,
            osdTimeout = osdTimeout ?: current.osdTimeout
        )
        viewModelScope.launch {
            menuLang?.let { preferences.setMenuLang(it) }
            audioLang?.let { preferences.setAudioLang(it) }
            tvSystem?.let { preferences.setTvSystem(it) }
            aspectRatio?.let { preferences.setAspectRatio(it) }
            osdTimeout?.let { preferences.setOsdTimeout(it) }
        }
    }

    private fun cycleLnbFrequency() {
        val values = listOf("09750", "10600", "10750", "11300")
        val next = values[(values.indexOf(_appSettings.value.lnbFreq) + 1).coerceAtLeast(0) % values.size]
        _appSettings.value = _appSettings.value.copy(lnbFreq = next)
        viewModelScope.launch { preferences.setLnbFreq(next) }
    }

    private fun cycleTransponder() {
        val values = listOf("10990", "11090", "11170", "11470", "11510")
        val next = values[(values.indexOf(_appSettings.value.transponder) + 1).coerceAtLeast(0) % values.size]
        _appSettings.value = _appSettings.value.copy(transponder = next)
        viewModelScope.launch { preferences.setTransponder(next) }
    }

    private fun restoreDefaults() {
        _appSettings.value = AppSettings()
        viewModelScope.launch {
            preferences.resetToDefaults()
            preferences.setRadioMode(false)
        }
        _isRadioMode.value = false
        _activeList.value = _allChannels.value
        _currentIndex.value = 0
    }

    private fun toggleFavorite() {
        val channel = _activeList.value.getOrNull(_currentIndex.value) ?: return
        val updated = channel.copy(fav = !channel.fav)
        replaceChannel(channel, updated)
        viewModelScope.launch { preferences.toggleFavChannel(channel.url) }
    }

    private fun toggleLock() {
        val channel = _activeList.value.getOrNull(_currentIndex.value) ?: return
        val updated = channel.copy(locked = !channel.locked)
        replaceChannel(channel, updated)
        viewModelScope.launch { preferences.toggleLockedChannel(channel.url) }
    }

    private fun replaceChannel(old: Channel, updated: Channel) {
        fun List<Channel>.replaced() = map { if (it.url == old.url) updated else it }
        _activeList.value = _activeList.value.replaced()
        _allChannels.value = _allChannels.value.replaced()
        _masterTvChannels.value = _masterTvChannels.value.replaced()
        _radioChannels.value = _radioChannels.value.replaced()
        _masterRadioChannels.value = _masterRadioChannels.value.replaced()
    }

    private fun deleteCurrentChannel() {
        val list = _activeList.value
        val index = _currentIndex.value
        if (index !in list.indices) return

        val target = list[index]
        val filtered = list.filterNot { it.url == target.url }
        _activeList.value = filtered
        _allChannels.value = _allChannels.value.filterNot { it.url == target.url }
        _masterTvChannels.value = _masterTvChannels.value.filterNot { it.url == target.url }
        _radioChannels.value = _radioChannels.value.filterNot { it.url == target.url }
        _masterRadioChannels.value = _masterRadioChannels.value.filterNot { it.url == target.url }

        if (filtered.isEmpty()) {
            _playingIndex.value = -1
            _videoUrl.value = ""
            _showNoSignal.value = true
            _subMenuIndex.value = 0
        } else {
            playChannel(index.coerceIn(0, filtered.lastIndex))
        }
    }

    // --------------------------------------------------------------- banner

    fun showBanner() {
        val list = _activeList.value
        val index = _currentIndex.value
        if (index !in list.indices) return

        _bannerState.value = BannerState(
            isVisible = true,
            channelName = "${index + 1}. ${list[index].displayName}",
            channelType = if (_isRadioMode.value) "Radio Channel" else "TV Channel"
        )

        val timeout = when (_appSettings.value.osdTimeout) {
            "3 Sec" -> 3_000L
            "10 Sec" -> 10_000L
            else -> 5_000L
        }
        bannerJob?.cancel()
        bannerJob = viewModelScope.launch {
            delay(timeout)
            _bannerState.value = _bannerState.value.copy(isVisible = false)
        }
    }

    fun enterFullscreen() {
        _footerIndex.value = null
        _viewState.value = ViewState.FULLSCREEN
        showBanner()
    }

    fun exitFullscreen() {
        _viewState.value = ViewState.LIST
        _bannerState.value = _bannerState.value.copy(isVisible = false)
    }

    // ------------------------------------------------------------- auto scan

    private fun startAutoScan() {
        scanJob?.cancel()
        _footerIndex.value = null
        _viewState.value = ViewState.AUTOSCAN
        _scanProgress.value = ScanProgress()

        val transponders = listOf("10990", "11090", "11170", "11470", "11510", "11550", "12020")
        val symbolRates = listOf("28490", "29500", "30000")
        val tvTotal = _masterTvChannels.value.size
        val radioTotal = _masterRadioChannels.value.size

        scanJob = viewModelScope.launch {
            for (progress in 0..100) {
                _scanProgress.value = ScanProgress(
                    progress = progress,
                    tpNum = (progress / 4).coerceAtLeast(1),
                    transFreq = transponders[(progress / 4) % transponders.size],
                    symbolRate = symbolRates[(progress / 4) % symbolRates.size],
                    polarity = if ((progress / 4) % 2 == 0) "V" else "H",
                    tvScanned = tvTotal * progress / 100,
                    radioScanned = radioTotal * progress / 100
                )
                delay(60)
            }
            delay(400)
            finalizeAutoScan()
        }
    }

    private fun finalizeAutoScan() {
        scanJob?.cancel()
        scanJob = null
        _allChannels.value = _masterTvChannels.value
        _radioChannels.value = _masterRadioChannels.value
        _activeList.value = if (_isRadioMode.value) _radioChannels.value else _allChannels.value
        _currentIndex.value = 0
        _viewState.value = ViewState.LIST
        _footerIndex.value = null
        if (_activeList.value.isNotEmpty()) playChannel(0) else _showNoSignal.value = true
    }

    // ----------------------------------------------------------- snake game

    fun startSnakeGame() {
        if (snakeJob?.isActive == true) return
        _snakeGameState.value = SnakeGameState(isRunning = true)

        snakeJob = viewModelScope.launch {
            while (isActive) {
                delay(180)
                val state = _snakeGameState.value
                if (!state.isRunning) break

                val head = state.snake.first()
                val next = Point(
                    (head.x + state.direction.x + GRID_WIDTH) % GRID_WIDTH,
                    (head.y + state.direction.y + GRID_HEIGHT) % GRID_HEIGHT
                )

                val hitsBody = state.snake.dropLast(1).contains(next)
                if (hitsBody) {
                    _snakeGameState.value = state.copy(isRunning = false, isGameOver = true)
                    break
                }

                val eating = next == state.food
                val body = if (eating) state.snake else state.snake.dropLast(1)
                val newSnake = listOf(next) + body

                _snakeGameState.value = state.copy(
                    snake = newSnake,
                    food = if (eating) spawnFood(newSnake) else state.food,
                    score = if (eating) state.score + 10 else state.score,
                    isRunning = true,
                    isGameOver = false
                )
            }
        }
    }

    fun stopSnakeGame() {
        snakeJob?.cancel()
        snakeJob = null
        _snakeGameState.value = _snakeGameState.value.copy(isRunning = false)
    }

    private fun changeSnakeDirection(direction: Point) {
        val state = _snakeGameState.value
        if (!state.isRunning) {
            startSnakeGame()
            return
        }
        // Ignore 180° reversals.
        if (state.direction.x == -direction.x && state.direction.y == -direction.y) return
        _snakeGameState.value = state.copy(direction = direction)
    }

    private fun spawnFood(snake: List<Point>): Point {
        repeat(200) {
            val candidate = Point(
                (0 until GRID_WIDTH).random(),
                (0 until GRID_HEIGHT).random()
            )
            if (!snake.contains(candidate)) return candidate
        }
        return Point(0, 0)
    }

    // ---------------------------------------------------------------- helpers

    val menuItems: List<String> = MENU_ITEMS
    val itemsPerPage: Int = PAGE_SIZE

    companion object {
        const val PAGE_SIZE = 10
        const val GRID_WIDTH = 26
        const val GRID_HEIGHT = 16
        private const val FOOTER_LIST_ENTRY = 2
        private const val FOOTER_MENU_ENTRY = 1

        private val MENU_ITEMS = listOf(
            "TV List",
            "Radio List",
            "Go to Channel",
            "Program Setup",
            "Program Edit",
            "Program Info",
            "Language",
            "System Setting",
            "Game",
            "System Info",
            "Cancel",
            "Exit App"
        )
    }
}
