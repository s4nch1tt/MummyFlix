package com.mummyflix.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mummyflix.model.*
import com.mummyflix.preferences.MummyFlixPreferences
import com.mummyflix.repository.ChannelRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MummyFlixViewModel(
    private val repository: ChannelRepository,
    private val preferences: MummyFlixPreferences
) : ViewModel() {

    // Channel data
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

    // UI State
    private val _viewState = mutableStateOf<ViewState>(ViewState.LIST)
    val viewState: State<ViewState> = _viewState

    private val _isRadioMode = mutableStateOf<Boolean>(false)
    val isRadioMode: State<Boolean> = _isRadioMode

    private val _currentIndex = mutableStateOf<Int>(0)
    val currentIndex: State<Int> = _currentIndex

    private val _playingIndex = mutableStateOf<Int>(-1)
    val playingIndex: State<Int> = _playingIndex

    private val _menuIndex = mutableStateOf<Int>(0)
    val menuIndex: State<Int> = _menuIndex

    private val _subMenuIndex = mutableStateOf<Int>(0)
    val subMenuIndex: State<Int> = _subMenuIndex

    private val _activeSubMenu = mutableStateOf<SubMenuType?>(null)
    val activeSubMenu: State<SubMenuType?> = _activeSubMenu

    private val _listScrollY = mutableStateOf<Float>(0f)
    val listScrollY: State<Float> = _listScrollY

    private val _scanProgress = mutableStateOf<ScanProgress>(ScanProgress())
    val scanProgress: State<ScanProgress> = _scanProgress

    private val _snakeGameState = mutableStateOf<SnakeGameState>(SnakeGameState())
    val snakeGameState: State<SnakeGameState> = _snakeGameState

    private val _numericInput = mutableStateOf<NumericInputState>(NumericInputState())
    val numericInput: State<NumericInputState> = _numericInput

    private val _bannerState = mutableStateOf<BannerState>(BannerState())
    val bannerState: State<BannerState> = _bannerState

    private val _isVideoPlaying = mutableStateOf<Boolean>(false)
    val isVideoPlaying: State<Boolean> = _isVideoPlaying

    private val _showNoSignal = mutableStateOf<Boolean>(false)
    val showNoSignal: State<Boolean> = _showNoSignal

    private val _videoUrl = mutableStateOf<String>("")
    val videoUrl: State<String> = _videoUrl

    // Settings from preferences
    private val _appSettings = mutableStateOf<AppSettings>(AppSettings())
    val appSettings: State<AppSettings> = _appSettings

    // Load initial data
    init {
        loadPreferences()
        loadPlaylist()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            preferences.menuLang.firstOrNull().let { if (it != null) _appSettings.value = _appSettings.value.copy(menuLang = it) }
            preferences.audioLang.firstOrNull().let { if (it != null) _appSettings.value = _appSettings.value.copy(audioLang = it) }
            preferences.tvSystem.firstOrNull().let { if (it != null) _appSettings.value = _appSettings.value.copy(tvSystem = it) }
            preferences.aspectRatio.firstOrNull().let { if (it != null) _appSettings.value = _appSettings.value.copy(aspectRatio = it) }
            preferences.osdTimeout.firstOrNull().let { if (it != null) _appSettings.value = _appSettings.value.copy(osdTimeout = it) }
            preferences.lnbFreq.firstOrNull().let { if (it != null) _appSettings.value = _appSettings.value.copy(lnbFreq = it) }
            preferences.transponder.firstOrNull().let { if (it != null) _appSettings.value = _appSettings.value.copy(transponder = it) }
            preferences.isRadioMode.firstOrNull().let { if (it != null) _isRadioMode.value = it }
            preferences.lastChannelIndex.firstOrNull().let { if (it != null) _currentIndex.value = it }
        }
    }

    private fun loadPlaylist() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.loadPlaylist()
            result.onSuccess { channels ->
                _masterTvChannels.value = channels
                val matchedRadio = repository.filterRadioChannels(channels)
                val defaultRadioNames = repository.getDefaultRadioNames()

                val masterRadio = defaultRadioNames.mapIndexed { idx, name ->
                    Channel(
                        name = name,
                        url = matchedRadio.getOrNull(idx % matchedRadio.size)?.url
                            ?: channels.getOrNull(idx)?.url
                            ?: "",
                        fav = false,
                        locked = false
                    )
                } + matchedRadio.map { it.copy() }

                _masterRadioChannels.value = masterRadio
                _allChannels.value = channels.toMutableList()
                _radioChannels.value = masterRadio.toMutableList()
                _activeList.value = if (_isRadioMode.value) masterRadio else channels

                if (_activeList.value.isNotEmpty()) {
                    _currentIndex.value = 0
                    playChannel(0)
                } else {
                    _showNoSignal.value = true
                }
            }.onFailure { e ->
                _showNoSignal.value = true
            }
        }
    }

    // Channel playback
    fun playChannel(index: Int) {
        val list = _activeList.value
        if (index < 0 || index >= list.size) return

        _playingIndex.value = index
        _currentIndex.value = index
        _videoUrl.value = list[index].url
        _isVideoPlaying.value = false
        _showNoSignal.value = false
        preferences.setLastChannelIndex(index)
    }

    fun onVideoPlaying() {
        _isVideoPlaying.value = true
        _showNoSignal.value = false
    }

    fun onVideoError() {
        _isVideoPlaying.value = false
        _showNoSignal.value = true
    }

    // Navigation
    fun moveSelection(delta: Int) {
        when (_viewState.value) {
            ViewState.LIST -> {
                val list = _activeList.value
                if (list.isEmpty()) return
                val nextIdx = (_currentIndex.value + delta + list.size) % list.size
                updateActiveChannel(nextIdx, shouldScroll = true)
                playChannel(nextIdx)
            }
            ViewState.MENU -> {
                val items = MENU_ITEMS
                _menuIndex.value = (_menuIndex.value + delta + items.size) % items.size
            }
            ViewState.SUBMENU -> {
                _activeSubMenu.value?.let { subMenu ->
                    when (subMenu) {
                        SubMenuType.GAME -> {
                            _snakeGameState.value = _snakeGameState.value.copy(direction = Point(0, delta))
                        }
                        else -> {
                            val maxOpt = (subMenu.options.size - 1).coerceAtMost(4)
                            _subMenuIndex.value = (_subMenuIndex.value + delta).coerceIn(0, maxOpt)
                        }
                    }
                }
            }
            ViewState.FULLSCREEN -> {
                val list = _activeList.value
                if (list.isEmpty()) return
                _currentIndex.value = (_currentIndex.value + delta + list.size) % list.size
                playChannel(_currentIndex.value)
                showBanner()
            }
        }
    }

    fun handleLeftRight(dir: Int) {
        when (_viewState.value) {
            ViewState.LIST -> {
                val list = _activeList.value
                if (list.isEmpty()) return
                var nextIdx = _currentIndex.value + dir * 10
                if (nextIdx >= list.size) nextIdx = 0
                if (nextIdx < 0) nextIdx = maxOf(0, list.size - 10)
                updateActiveChannel(nextIdx, shouldScroll = true)
                playChannel(nextIdx)
            }
            ViewState.SUBMENU -> {
                _activeSubMenu.value?.let { subMenu ->
                    when (subMenu) {
                        SubMenuType.GAME -> {
                            _snakeGameState.value = _snakeGameState.value.copy(direction = Point(dir, 0))
                        }
                        else -> handleOkKey()
                    }
                }
            }
        }
    }

    fun handleOkKey() {
        when (_viewState.value) {
            ViewState.LIST -> {
                if (_playingIndex.value != _currentIndex.value) {
                    playChannel(_currentIndex.value)
                } else {
                    enterFullscreen()
                }
            }
            ViewState.MENU -> {
                openSelectedMenuOption(MENU_ITEMS[_menuIndex.value])
            }
            ViewState.SUBMENU -> {
                handleSubMenuAction()
            }
            ViewState.FULLSCREEN -> {
                exitFullscreen()
            }
        }
    }

    fun handleMenuKey() {
        when (_viewState.value) {
            ViewState.AUTOSCAN -> finalizeAutoScan()
            ViewState.SUBMENU -> {
                _viewState.value = ViewState.MENU
                _activeSubMenu.value = null
            }
            ViewState.LIST -> {
                _viewState.value = ViewState.MENU
                _menuIndex.value = 2 // Program Setup
            }
            ViewState.MENU -> {
                _viewState.value = ViewState.LIST
            }
            ViewState.FULLSCREEN -> {
                _viewState.value = ViewState.MENU
                _bannerState.value = _bannerState.value.copy(isVisible = false)
            }
        }
    }

    fun handleNumericKey(digit: String) {
        if (_viewState.value !in setOf(ViewState.LIST, ViewState.FULLSCREEN)) return

        val buffer = _numericInput.value.buffer + digit
        val newBuffer = if (buffer.length >= 3) digit else buffer
        _numericInput.value = NumericInputState(newBuffer, true)

        // Auto-submit after 1.4 seconds
        // In a real app, you'd use a coroutine with delay
    }

    fun submitNumericInput() {
        val buffer = _numericInput.value.buffer
        _numericInput.value = NumericInputState("", false)
        val targetCh = buffer.toIntOrNull()?.minus(1)
        targetCh?.let {
            val list = _activeList.value
            if (it in list.indices) {
                updateActiveChannel(it, true)
                playChannel(it)
                if (_viewState.value == ViewState.FULLSCREEN) showBanner()
            }
        }
    }

    private fun updateActiveChannel(newIndex: Int, shouldScroll: Boolean) {
        _currentIndex.value = newIndex
        // Scroll handling would be done in UI
    }

    private fun openSelectedMenuOption(optionName: String) {
        when (optionName) {
            "TV List" -> {
                _isRadioMode.value = false
                _activeList.value = _allChannels.value
                _currentIndex.value = minOf(_currentIndex.value, maxOf(0, _allChannels.value.size - 1))
                _viewState.value = ViewState.LIST
                playChannel(_currentIndex.value)
                preferences.setRadioMode(false)
            }
            "Radio List" -> {
                _isRadioMode.value = true
                _activeList.value = _radioChannels.value
                _currentIndex.value = 0
                _listScrollY.value = 0f
                _viewState.value = ViewState.LIST
                playChannel(0)
                preferences.setRadioMode(true)
            }
            "Cancel" -> enterFullscreen()
            else -> {
                val subMenu = SubMenuType.values().find { it.title == optionName }
                subMenu?.let {
                    _activeSubMenu.value = it
                    _subMenuIndex.value = 0
                    _viewState.value = ViewState.SUBMENU
                }
            }
        }
    }

    private fun handleSubMenuAction() {
        val ch = _activeList.value.getOrNull(_currentIndex.value)
        _activeSubMenu.value?.let { subMenu ->
            when (subMenu) {
                SubMenuType.PROGRAM_SETUP -> {
                    when (_subMenuIndex.value) {
                        0 -> { _viewState.value = ViewState.AUTOSCAN; startAutoScan() }
                        4 -> handleMenuKey()
                    }
                }
                SubMenuType.PROGRAM_EDIT -> {
                    ch?.let { channel ->
                        when (_subMenuIndex.value) {
                            1 -> channel.fav = !channel.fav
                            2 -> channel.locked = !channel.locked
                            3 -> {
                                if (_activeList.value.size > 1) {
                                    val list = _activeList.value.toMutableList()
                                    list.removeAt(_currentIndex.value)
                                    _activeList.value = list
                                    _currentIndex.value = maxOf(0, _currentIndex.value - 1)
                                    _masterTvChannels.value.toMutableList().removeAll { it.url == channel.url }
                                    _masterRadioChannels.value.toMutableList().removeAll { it.url == channel.url }
                                }
                            }
                            4 -> { handleMenuKey(); return }
                        }
                        // Re-render submenu
                    }
                }
                SubMenuType.LANGUAGE -> {
                    when (_subMenuIndex.value) {
                        0 -> {
                            val newLang = if (_appSettings.value.menuLang == "English") "Hindi" else "English"
                            _appSettings.value = _appSettings.value.copy(menuLang = newLang)
                            preferences.setMenuLang(newLang)
                        }
                        1 -> {
                            val newLang = if (_appSettings.value.audioLang == "Hindi") "English" else "Hindi"
                            _appSettings.value = _appSettings.value.copy(audioLang = newLang)
                            preferences.setAudioLang(newLang)
                        }
                        3 -> { handleMenuKey(); return }
                    }
                }
                SubMenuType.SYSTEM_SETTING -> {
                    when (_subMenuIndex.value) {
                        0 -> {
                            val newSystem = if (_appSettings.value.tvSystem == "PAL") "NTSC" else "PAL"
                            _appSettings.value = _appSettings.value.copy(tvSystem = newSystem)
                            preferences.setTvSystem(newSystem)
                        }
                        1 -> {
                            val isFull = _appSettings.value.aspectRatio.startsWith("Full")
                            val newRatio = if (isFull) "Fit (4:3)" else "Full (16:9)"
                            _appSettings.value = _appSettings.value.copy(aspectRatio = newRatio)
                            preferences.setAspectRatio(newRatio)
                        }
                        3 -> {
                            _appSettings.value = AppSettings()
                            preferences.setTvSystem("PAL")
                            preferences.setAspectRatio("Full (16:9)")
                        }
                        4 -> { handleMenuKey(); return }
                    }
                }
                else -> handleMenuKey()
            }
        }
    }

    fun enterFullscreen() {
        _viewState.value = ViewState.FULLSCREEN
        showBanner()
    }

    fun exitFullscreen() {
        _viewState.value = ViewState.LIST
        _bannerState.value = _bannerState.value.copy(isVisible = false)
    }

    fun showBanner() {
        val list = _activeList.value
        val idx = _currentIndex.value
        if (idx in list.indices) {
            _bannerState.value = BannerState(
                isVisible = true,
                channelName = "${idx + 1}. ${list[idx].name}",
                channelType = if (_isRadioMode.value) "Radio Channel" else "TV Channel"
            )
        }
    }

    // Auto Scan
    private fun startAutoScan() {
        _scanProgress.value = ScanProgress()
        val transFreqs = listOf("10990", "11090", "11170", "11470", "11510", "11550", "12020")
        val symRates = listOf("28490", "29500", "30000")

        _allChannels.value = emptyList()
        _radioChannels.value = emptyList()

        viewModelScope.launch {
            for (progress in 1..100) {
                _scanProgress.value = _scanProgress.value.copy(
                    progress = progress,
                    tpNum = maxOf(1, (progress / 4).coerceAtLeast(1)),
                    transFreq = transFreqs[((progress / 4) % transFreqs.size)],
                    symbolRate = symRates[((progress / 4) % symRates.size)],
                    polarity = if ((progress / 4) % 2 == 0) "V" else "H",
                    tvScanned = (progress * _masterTvChannels.value.size / 100),
                    radioScanned = (progress * _masterRadioChannels.value.size / 100)
                )
                // Simulate scanning delay
                kotlinx.coroutines.delay(150)
            }
            finalizeAutoScan()
        }
    }

    private fun finalizeAutoScan() {
        if (_allChannels.value.isEmpty() && _masterTvChannels.value.isNotEmpty()) {
            _allChannels.value = listOf(_masterTvChannels.value[0])
        }
        if (_radioChannels.value.isEmpty() && _masterRadioChannels.value.isNotEmpty()) {
            _radioChannels.value = listOf(_masterRadioChannels.value[0])
        }
        _activeList.value = if (_isRadioMode.value) _radioChannels.value else _allChannels.value
        _currentIndex.value = 0
        _listScrollY.value = 0f
        _viewState.value = ViewState.LIST
        playChannel(0)
    }

    // Snake Game
    fun startSnakeGame() {
        _snakeGameState.value = SnakeGameState(
            snake = listOf(Point(5, 5), Point(4, 5), Point(3, 5)),
            direction = Point(1, 0),
            food = Point(10, 6),
            score = 0,
            isRunning = true
        )

        viewModelScope.launch {
            while (_snakeGameState.value.isRunning) {
                val state = _snakeGameState.value
                val head = state.snake[0]
                val newHead = Point(
                    (head.x + state.direction.x + 26) % 26,
                    (head.y + state.direction.y + 16) % 16
                )

                val newSnake = (newHead + state.snake).toList()
                var newFood = state.food
                var newScore = state.score

                if (newHead == state.food) {
                    newScore += 10
                    newFood = Point((Math.random() * 24 + 1).toInt(), (Math.random() * 14 + 1).toInt())
                } else {
                    _snakeGameState.value = _snakeGameState.value.copy(snake = newSnake.dropLast(1))
                }

                _snakeGameState.value = _snakeGameState.value.copy(
                    snake = if (newHead == state.food) newSnake else newSnake.dropLast(1),
                    food = newFood,
                    score = newScore
                )

                kotlinx.coroutines.delay(220)
            }
        }
    }

    fun stopSnakeGame() {
        _snakeGameState.value = _snakeGameState.value.copy(isRunning = false)
    }

    // Getters for UI
    val menuItems = MENU_ITEMS
    val itemsPerPage = 10

    companion object {
        private val MENU_ITEMS = listOf(
            "TV List",
            "Radio List",
            "Program Setup",
            "Program Edit",
            "Program Info",
            "Language",
            "System Setting",
            "Game",
            "System Info",
            "Cancel"
        )
    }
}