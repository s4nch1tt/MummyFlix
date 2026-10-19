package com.mummyflix

import android.app.Application
import com.mummyflix.preferences.MummyFlixPreferences
import com.mummyflix.repository.ChannelRepository
import com.mummyflix.viewmodel.MummyFlixViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class MummyFlixApplication : Application() {

    val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    lateinit var channelRepository: ChannelRepository
    lateinit var preferences: MummyFlixPreferences
    lateinit var viewModel: MummyFlixViewModel

    override fun onCreate() {
        super.onCreate()
        channelRepository = ChannelRepository()
        preferences = MummyFlixPreferences.apply { initialize(this@MummyFlixApplication) }
        viewModel = MummyFlixViewModel(channelRepository, preferences)
    }
}