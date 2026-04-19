package com.onlyjesus

import org.junit.Assert.assertEquals
import org.junit.Test

class DisplayNameFromPathTest {
    @Test
    fun formatsDatabaseFileNamesToReadableTitles() {
        assertEquals("Eng Kjv", displayNameFromPath("databases/eng_kjv.db"))
        assertEquals("Spanish Reina Valera", displayNameFromPath("spanish-reina_valera.sqlite3"))
        assertEquals("Bible", displayNameFromPath(""))
    }
}
