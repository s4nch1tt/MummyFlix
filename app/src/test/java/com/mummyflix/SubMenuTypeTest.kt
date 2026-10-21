package com.mummyflix

import com.mummyflix.model.SubMenuType
import org.junit.Assert.assertEquals
import org.junit.Test

class SubMenuTypeTest {

    @Test
    fun `submenu titles match the receiver menu labels`() {
        val menuLabels = listOf(
            "Program Setup",
            "Program Edit",
            "Program Info",
            "Language",
            "System Setting",
            "Game",
            "System Info"
        )

        assertEquals(menuLabels, SubMenuType.entries.map { it.title })
    }
}
