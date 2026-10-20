package com.mummyflix.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/** Single DataStore instance for the whole process. */
private val Context.mummyFlixDataStore: DataStore<Preferences> by
    preferencesDataStore(name = "mummyflix_preferences")

/**
 * Typed wrapper around the app's DataStore.
 *
 * Every value is exposed as a cold [Flow]; setters are suspending and safe to
 * call from any coroutine dispatcher.
 */
class MummyFlixPreferences private constructor(context: Context) {

    private val dataStore = context.applicationContext.mummyFlixDataStore

    val menuLang: Flow<String> =
        dataStore.data.map { it[KEY_MENU_LANG] ?: DEFAULT_MENU_LANG }.distinctUntilChanged()

    val audioLang: Flow<String> =
        dataStore.data.map { it[KEY_AUDIO_LANG] ?: DEFAULT_AUDIO_LANG }.distinctUntilChanged()

    val tvSystem: Flow<String> =
        dataStore.data.map { it[KEY_TV_SYSTEM] ?: DEFAULT_TV_SYSTEM }.distinctUntilChanged()

    val aspectRatio: Flow<String> =
        dataStore.data.map { it[KEY_ASPECT_RATIO] ?: DEFAULT_ASPECT_RATIO }.distinctUntilChanged()

    val osdTimeout: Flow<String> =
        dataStore.data.map { it[KEY_OSD_TIMEOUT] ?: DEFAULT_OSD_TIMEOUT }.distinctUntilChanged()

    val lnbFreq: Flow<String> =
        dataStore.data.map { it[KEY_LNB_FREQ] ?: DEFAULT_LNB_FREQ }.distinctUntilChanged()

    val transponder: Flow<String> =
        dataStore.data.map { it[KEY_TRANSPONDER] ?: DEFAULT_TRANSPONDER }.distinctUntilChanged()

    val favChannels: Flow<Set<String>> =
        dataStore.data.map { it[KEY_FAV_CHANNELS] ?: emptySet() }.distinctUntilChanged()

    val lockedChannels: Flow<Set<String>> =
        dataStore.data.map { it[KEY_LOCKED_CHANNELS] ?: emptySet() }.distinctUntilChanged()

    val lastChannelIndex: Flow<Int> =
        dataStore.data.map { it[KEY_LAST_CHANNEL_INDEX] ?: 0 }.distinctUntilChanged()

    val isRadioMode: Flow<Boolean> =
        dataStore.data.map { it[KEY_IS_RADIO_MODE] ?: false }.distinctUntilChanged()

    suspend fun setMenuLang(value: String) {
        dataStore.edit { it[KEY_MENU_LANG] = value }
    }

    suspend fun setAudioLang(value: String) {
        dataStore.edit { it[KEY_AUDIO_LANG] = value }
    }

    suspend fun setTvSystem(value: String) {
        dataStore.edit { it[KEY_TV_SYSTEM] = value }
    }

    suspend fun setAspectRatio(value: String) {
        dataStore.edit { it[KEY_ASPECT_RATIO] = value }
    }

    suspend fun setOsdTimeout(value: String) {
        dataStore.edit { it[KEY_OSD_TIMEOUT] = value }
    }

    suspend fun setLnbFreq(value: String) {
        dataStore.edit { it[KEY_LNB_FREQ] = value }
    }

    suspend fun setTransponder(value: String) {
        dataStore.edit { it[KEY_TRANSPONDER] = value }
    }

    suspend fun toggleFavChannel(channelUrl: String) {
        dataStore.edit { prefs ->
            val current = prefs[KEY_FAV_CHANNELS]?.toMutableSet() ?: mutableSetOf()
            if (!current.add(channelUrl)) current.remove(channelUrl)
            prefs[KEY_FAV_CHANNELS] = current
        }
    }

    suspend fun toggleLockedChannel(channelUrl: String) {
        dataStore.edit { prefs ->
            val current = prefs[KEY_LOCKED_CHANNELS]?.toMutableSet() ?: mutableSetOf()
            if (!current.add(channelUrl)) current.remove(channelUrl)
            prefs[KEY_LOCKED_CHANNELS] = current
        }
    }

    suspend fun setLastChannelIndex(value: Int) {
        dataStore.edit { it[KEY_LAST_CHANNEL_INDEX] = value }
    }

    suspend fun setRadioMode(value: Boolean) {
        dataStore.edit { it[KEY_IS_RADIO_MODE] = value }
    }

    suspend fun resetToDefaults() {
        dataStore.edit { it.clear() }
    }

    companion object {
        const val DEFAULT_MENU_LANG = "English"
        const val DEFAULT_AUDIO_LANG = "Hindi"
        const val DEFAULT_TV_SYSTEM = "PAL"
        const val DEFAULT_ASPECT_RATIO = "Full (16:9)"
        const val DEFAULT_OSD_TIMEOUT = "5 Sec"
        const val DEFAULT_LNB_FREQ = "09750"
        const val DEFAULT_TRANSPONDER = "10990"

        private val KEY_MENU_LANG = stringPreferencesKey("menu_lang")
        private val KEY_AUDIO_LANG = stringPreferencesKey("audio_lang")
        private val KEY_TV_SYSTEM = stringPreferencesKey("tv_system")
        private val KEY_ASPECT_RATIO = stringPreferencesKey("aspect_ratio")
        private val KEY_OSD_TIMEOUT = stringPreferencesKey("osd_timeout")
        private val KEY_LNB_FREQ = stringPreferencesKey("lnb_freq")
        private val KEY_TRANSPONDER = stringPreferencesKey("transponder")
        private val KEY_FAV_CHANNELS = stringSetPreferencesKey("fav_channels")
        private val KEY_LOCKED_CHANNELS = stringSetPreferencesKey("locked_channels")
        private val KEY_LAST_CHANNEL_INDEX = intPreferencesKey("last_channel_index")
        private val KEY_IS_RADIO_MODE = booleanPreferencesKey("is_radio_mode")

        @Volatile
        private var instance: MummyFlixPreferences? = null

        fun getInstance(context: Context): MummyFlixPreferences =
            instance ?: synchronized(this) {
                instance ?: MummyFlixPreferences(context).also { instance = it }
            }
    }
}
