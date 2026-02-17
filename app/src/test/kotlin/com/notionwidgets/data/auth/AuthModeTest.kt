package com.notionwidgets.data.auth

import org.junit.Assert.assertEquals
import org.junit.Test

class AuthModeTest {

    @Test
    fun `AuthMode has expected values`() {
        val modes = AuthMode.values()
        assertEquals(2, modes.size)
        assertEquals(AuthMode.INTERNAL_TOKEN, AuthMode.valueOf("INTERNAL_TOKEN"))
        assertEquals(AuthMode.OAUTH, AuthMode.valueOf("OAUTH"))
    }
}
