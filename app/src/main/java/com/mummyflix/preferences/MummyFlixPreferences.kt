package com.mummyflix.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesKeys
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesDataStore
import androidx.datastore.rxjava3.RxDataStore
import io.reactivex.rxjava3.core.Flowable
import kotlinx.coroutines.tasks.await

object MummyFlixPreferences {
    private const val DATA_STORE_NAME = "mummyflix_preferences"

    private val MENU_LANG = PreferencesKeys.stringKey("menu_lang")
    private val AUDIO_LANG = PreferencesKeys.stringKey("audio_lang")
    private val TV_SYSTEM = PreferencesKeys.stringKey("tv_system")
    private val ASPECT_RATIO = PreferencesKeys.stringKey("aspect_ratio")
    private val OSD_TIMEOUT = PreferencesKeys.stringKey("osd_timeout")
    private val LNB_FREQ = PreferencesKeys.stringKey("lnb_freq")
    private val TRANSPONDER = PreferencesKeys.stringKey("transponder")
    private val FAV_CHANNELS = PreferencesKeys.stringSetKey("fav_channels")
    private val LOCKED_CHANNELS = PreferencesKeys.stringSetKey("locked_channels")
    private val LAST_CHANNEL_INDEX = PreferencesKeys.intKey("last_channel_index")
    private val IS_RADIO_MODE = PreferencesKeys.booleanKey("is_radio_mode")

    private lateinit var dataStore: RxDataStore<Preferences>

    fun initialize(context: Context) {
        dataStore = context.preferencesDataStore(name = DATA_STORE_NAME)
    }

    private fun getDataStore(): RxDataStore<Preferences> {
        return dataStore
    }

    // Menu Language
    val menuLang: Flowable<String> = getDataStore()
        .data()
        .map { it[MENU_LANG] ?: "English" }
        .distinctUntilChanged()

    suspend fun setMenuLang(lang: String) {
        getDataStore().updateDataAsync { it[MENU_LANG] = lang }.await()
    }

    // Audio Language
    val audioLang: Flowable<String> = getDataStore()
        .data()
        .map { it[AUDIO_LANG] ?: "Hindi" }
        .distinctUntilChanged()

    suspend fun setAudioLang(lang: String) {
        getDataStore().updateDataAsync { it[AUDIO_LANG] = lang }.await()
    }

    // TV System
    val tvSystem: Flowable<String> = getDataStore()
        .data()
        .map { it[TV_SYSTEM] ?: "PAL" }
        .distinctUntilChanged()

    suspend fun setTvSystem(system: String) {
        getDataStore().updateDataAsync { it[TV_SYSTEM] = system }.await()
    }

    // Aspect Ratio
    val aspectRatio: Flowable<String> = getDataStore()
        .data()
        .map { it[ASPECT_RATIO] ?: "Full (16:9)" }
        .distinctUntilChanged()

    suspend fun setAspectRatio(ratio: String) {
        getDataStore().updateDataAsync { it[ASPECT_RATIO] = ratio }.await()
    }

    // OSD Timeout
    val osdTimeout: Flowable<String> = getDataStore()
        .data()
        .map { it[OSD_TIMEOUT] ?: "5 Sec" }
        .distinctUntilChanged()

    suspend fun setOsdTimeout(timeout: String) {
        getDataStore().updateDataAsync { it[OSD_TIMEOUT] = timeout }.await()
    }

    // LNB Frequency
    val lnbFreq: Flowable<String> = getDataStore()
        .data()
        .map { it[LNB_FREQ] ?: "09750" }
        .distinctUntilChanged()

    suspend fun setLnbFreq(freq: String) {
        getDataStore().updateDataAsync { it[LNB_FREQ] = freq }.await()
    }

    // Transponder
    val transponder: Flowable<String> = getDataStore()
        .data()
        .map { it[TRANSPONDER] ?: "10990" }
        .distinctUntilChanged()

    suspend fun setTransponder(transponder: String) {
        getDataStore().updateDataAsync { it[TRANSPONDER] = transponder }.await()
    }

    // Favorite Channels
    val favChannels: Flowable<Set<String>> = getDataStore()
        .data()
        .map { it[FAV_CHANNELS] ?: emptySet() }
        .distinctUntilChanged()

    suspend fun toggleFavChannel(channelUrl: String) {
        getDataStore().updateDataAsync { prefs ->
            val current = prefs[FAV_CHANNELS]?.toMutableSet() ?: mutableSetOf()
            if (current.contains(channelUrl)) {
                current.remove(channelUrl)
            } else {
                current.add(channelUrl)
            }
            prefs[FAV_CHANNELS] = current
        }.await()
    }

    // Locked Channels
    val lockedChannels: Flowable<Set<String>> = getDataStore()
        .data()
        .map { it[LOCKED_CHANNELS] ?: emptySet() }
        .distinctUntilChanged()

    suspend fun toggleLockedChannel(channelUrl: String) {
        getDataStore().updateDataAsync { prefs ->
            val current = prefs[LOCKED_CHANNELS]?.toMutableSet() ?: mutableSetOf()
            if (current.contains(channelUrl)) {
                current.remove(channelUrl)
            } else {
                current.add(channelUrl)
            }
            prefs[LOCKED_CHANNELS] = current
        }.await()
    }

    // Last Channel Index
    val lastChannelIndex: Flowable<Int> = getDataStore()
        .data()
        .map { it[LAST_CHANNEL_INDEX] ?: 0 }
        .distinctUntilChanged()

    suspend fun setLastChannelIndex(index: Int) {
        getDataStore().updateDataAsync { it[LAST_CHANNEL_INDEX] = index }.await()
    }

    // Radio Mode
    val isRadioMode: Flowable<Boolean> = getDataStore()
        .data()
        .map { it[IS_RADIO_MODE] ?: false }
        .distinctUntilChanged()

    suspend fun setRadioMode(isRadio: Boolean) {
        getDataStore().updateDataAsync { it[IS_RADIO_MODE] = isRadio }.await()
    }
}