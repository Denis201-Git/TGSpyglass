package com.example.tgparser

import com.example.tgparser.utils.formatTelegramLink
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LinkFormatterTest {

    @Test
    fun bareUsername() {
        assertEquals("https://t.me/s/minecraftapp", formatTelegramLink("minecraftapp"))
    }

    @Test
    fun atUsername() {
        assertEquals("https://t.me/s/minecraftapp", formatTelegramLink("@minecraftapp"))
    }

    @Test
    fun channelLink() {
        assertEquals("https://t.me/s/minecraftapp", formatTelegramLink("https://t.me/minecraftapp"))
    }

    @Test
    fun previewLink() {
        assertEquals("https://t.me/s/minecraftapp", formatTelegramLink("https://t.me/s/minecraftapp/"))
    }

    @Test
    fun postLinkWithComment() {
        assertEquals(
            "https://t.me/s/minecraftapp",
            formatTelegramLink("https://t.me/minecraftapp/14745?comment=12345")
        )
    }

    @Test
    fun postLink() {
        assertEquals(
            "https://t.me/s/minecraftapp",
            formatTelegramLink("https://t.me/minecraftapp/14745")
        )
    }

    @Test
    fun uppercaseLink() {
        assertEquals(
            "https://t.me/s/MinecraftApp",
            formatTelegramLink("HTTPS://T.ME/MinecraftApp")
        )
    }

    @Test
    fun inviteLinkRejected() {
        assertNull(formatTelegramLink("https://t.me/+abcdef"))
        assertNull(formatTelegramLink("https://t.me/joinchat/abcdef"))
        assertNull(formatTelegramLink("https://t.me/c/123456/789"))
    }

    @Test
    fun invalidInputRejected() {
        assertNull(formatTelegramLink(""))
        assertNull(formatTelegramLink("two words"))
        assertNull(formatTelegramLink("https://t.me/"))
    }
}
