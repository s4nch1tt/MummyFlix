package com.mummyflix.repository

import com.mummyflix.model.Channel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

class ChannelRepository {
    companion object {
        private const val PLAYLIST_URL = "https://iptv-org.github.io/iptv/countries/in.m3u"
        private const val ITEMS_PER_PAGE = 10
        private val DEFAULT_RADIO_NAMES = listOf(
            "Radio Mirchi", "Red FM", "Radio City", "Big FM", "Fever FM",
            "Radio One", "Radio Nasha", "Radio Mango", "Radio Dhamaal", "Ishq FM"
        )
    }

    suspend fun loadPlaylist(): Result<List<Channel>> = withContext(Dispatchers.IO) {
        try {
            val url = URL(PLAYLIST_URL)
            val text = url.readText()
            val lines = text.lines()
            val masterTvChannels = mutableListOf<Channel>()
            var currentName = ""

            for (line in lines) {
                val trimmed = line.trim()
                if (trimmed.startsWith("#EXTINF:")) {
                    val parts = trimmed.split(",")
                    currentName = parts.last().trim().ifEmpty { "Unknown Channel" }
                } else if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                    masterTvChannels.add(Channel(
                        name = currentName,
                        url = trimmed,
                        fav = false,
                        locked = false
                    ))
                    currentName = ""
                }
            }

            val matchedRadio = masterTvChannels.filter { channel ->
                channel.name.matches(Regex("(?i)music|9x|mastiii|b4u|mtv|vh1|radio|fm|hits|sangeet|zing|tune"))
            }

            val masterRadioChannels = DEFAULT_RADIO_NAMES.mapIndexed { idx, rName ->
                Channel(
                    name = rName,
                    url = matchedRadio.getOrNull(idx % matchedRadio.size)?.url
                        ?: masterTvChannels.getOrNull(idx)?.url
                        ?: "",
                    fav = false,
                    locked = false
                )
            } + matchedRadio.map { it.copy() }

            Result.Success(masterTvChannels)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    fun getDefaultRadioNames(): List<String> = DEFAULT_RADIO_NAMES

    fun filterRadioChannels(channels: List<Channel>): List<Channel> {
        return channels.filter { channel ->
            channel.name.matches(Regex("(?i)music|9x|mastiii|b4u|mtv|vh1|radio|fm|hits|sangeet|zing|tune"))
        }
    }
}

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Failure(val exception: Throwable) : Result<Nothing>()
}