package com.mummyflix.repository

import com.mummyflix.model.Channel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

class ChannelRepository {

    companion object {
        private const val PLAYLIST_URL = "https://iptv-org.github.io/iptv/countries/in.m3u"
        private val RADIO_PATTERN =
            Regex("(?i)music|9x|mastiii|b4u|mtv|vh1|radio|fm|hits|sangeet|zing|tune")
        private val DEFAULT_RADIO_NAMES = listOf(
            "Radio Mirchi", "Red FM", "Radio City", "Big FM", "Fever FM",
            "Radio One", "Radio Nasha", "Radio Mango", "Radio Dhamaal", "Ishq FM"
        )
    }

    suspend fun loadPlaylist(): Result<List<Channel>> = withContext(Dispatchers.IO) {
        try {
            val text = URL(PLAYLIST_URL).readText()
            val channels = mutableListOf<Channel>()
            var currentName = ""

            text.lineSequence().forEach { line ->
                val trimmed = line.trim()
                when {
                    trimmed.startsWith("#EXTINF:") -> {
                        currentName = trimmed.substringAfter(',', "Unknown Channel").trim()
                    }
                    trimmed.isNotEmpty() && !trimmed.startsWith("#") -> {
                        channels += Channel(
                            name = currentName.ifBlank { "Unknown Channel" },
                            url = trimmed
                        )
                        currentName = ""
                    }
                }
            }
            Result.success(channels)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getDefaultRadioNames(): List<String> = DEFAULT_RADIO_NAMES

    fun filterRadioChannels(channels: List<Channel>): List<Channel> =
        channels.filter { RADIO_PATTERN.containsMatchIn(it.name) }

    /** Builds the radio list: a stable set of named stations plus any music channels. */
    fun buildRadioChannels(tvChannels: List<Channel>): List<Channel> {
        val matched = filterRadioChannels(tvChannels)
        val named = DEFAULT_RADIO_NAMES.mapIndexed { index, name ->
            Channel(
                name = name,
                url = matched.getOrNull(index % matched.size.coerceAtLeast(1))?.url
                    ?: tvChannels.getOrNull(index)?.url
                    ?: ""
            )
        }
        return (named + matched).distinctBy {
            if (it.url.isBlank()) "name:${it.name}" else it.url
        }
    }
}
