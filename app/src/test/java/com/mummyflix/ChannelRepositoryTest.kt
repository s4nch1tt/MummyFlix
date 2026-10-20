package com.mummyflix

import com.mummyflix.model.Channel
import com.mummyflix.repository.ChannelRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChannelRepositoryTest {

    private val repository = ChannelRepository()

    @Test
    fun `filters music and radio channels`() {
        val channels = listOf(
            Channel("NDTV", "https://example.com/ndtv.m3u8"),
            Channel("Radio Mirchi", "https://example.com/mirchi.m3u8"),
            Channel("MTV Hits", "https://example.com/mtv.m3u8")
        )

        val filtered = repository.filterRadioChannels(channels)

        assertEquals(2, filtered.size)
        assertTrue(filtered.any { it.name == "Radio Mirchi" })
        assertTrue(filtered.any { it.name == "MTV Hits" })
    }

    @Test
    fun `radio list always exposes the default station names`() {
        val radio = repository.buildRadioChannels(emptyList())

        assertTrue(radio.size >= repository.getDefaultRadioNames().size)
        repository.getDefaultRadioNames().forEach { name ->
            assertTrue("missing $name", radio.any { it.name == name })
        }
    }

    @Test
    fun `channels are de-duplicated by url`() {
        val channels = listOf(
            Channel("FM Gold", "https://example.com/a.m3u8"),
            Channel("FM Gold", "https://example.com/a.m3u8")
        )

        val radio = repository.buildRadioChannels(channels)

        assertEquals(radio.distinctBy { it.url }.size, radio.size)
    }
}
