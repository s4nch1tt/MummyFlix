package com.mummyflix

import android.app.Application
import com.mummyflix.preferences.MummyFlixPreferences
import com.mummyflix.repository.ChannelRepository
import com.mummyflix.viewmodel.MummyFlixViewModel

class MummyFlixApplication : Application() {

    lateinit var channelRepository: ChannelRepository
        private set

    lateinit var preferences: MummyFlixPreferences
        private set

    lateinit var viewModel: MummyFlixViewModel
        private set

    override fun onCreate() {
        super.onCreate()
        channelRepository = ChannelRepository()
        preferences = MummyFlixPreferences.getInstance(this)
        viewModel = MummyFlixViewModel(channelRepository, preferences)
    }
}
