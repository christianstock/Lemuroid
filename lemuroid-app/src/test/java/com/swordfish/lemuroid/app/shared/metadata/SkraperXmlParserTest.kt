package com.swordfish.lemuroid.app.shared.metadata

import com.swordfish.lemuroid.lib.library.metadata.SkraperXmlParser
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream

class SkraperXmlParserTest {

    private val parser = SkraperXmlParser()

    @Test
    fun `parse LaunchBox format`() {
        val xml = """
            <?xml version="1.0" encoding="utf-8"?>
            <LaunchBox>
                <Game>
                    <Title>Super Mario World</Title>
                    <ApplicationPath>C:\Roms\SNES\Super Mario World (USA).sfc</ApplicationPath>
                    <Developer>Nintendo</Developer>
                    <Publisher>Nintendo</Publisher>
                    <Notes>A classic platformer game.</Notes>
                    <ManualPath>C:\Manuals\SMW.pdf</ManualPath>
                    <Genre>Platform</Genre>
                </Game>
            </LaunchBox>
        """.trimIndent()

        val entries = parser.parse(ByteArrayInputStream(xml.toByteArray()))

        assertEquals(1, entries.size)
        val entry = entries.first()
        assertEquals("Super Mario World", entry.title)
        assertEquals("Super Mario World (USA).sfc", entry.romFileName)
        assertEquals("Nintendo", entry.developer)
        assertEquals("Nintendo", entry.publisher)
        assertEquals("A classic platformer game.", entry.description)
        assertEquals("C:\\Manuals\\SMW.pdf", entry.manualPath)
        assertEquals("Platform", entry.genre)
    }

    @Test
    fun `parse EmulationStation gameList format`() {
        val xml = """
            <?xml version="1.0" encoding="utf-8"?>
            <gameList>
                <game>
                    <path>./Zelda.sfc</path>
                    <name>The Legend of Zelda: A Link to the Past</name>
                    <desc>An epic adventure.</desc>
                    <developer>Nintendo EAD</developer>
                    <publisher>Nintendo</publisher>
                    <releasedate>19911121T000000</releasedate>
                    <image>./covers/zelda.png</image>
                    <genre>Action-Adventure</genre>
                </game>
            </gameList>
        """.trimIndent()

        val entries = parser.parse(ByteArrayInputStream(xml.toByteArray()))

        assertEquals(1, entries.size)
        val entry = entries.first()
        assertEquals("The Legend of Zelda: A Link to the Past", entry.title)
        assertEquals("Zelda.sfc", entry.romFileName)
        assertEquals("Nintendo EAD", entry.developer)
        assertEquals("Nintendo", entry.publisher)
        assertEquals("An epic adventure.", entry.description)
        assertEquals("19911121", entry.releaseDate)
        assertEquals("./covers/zelda.png", entry.coverFrontPath)
        assertEquals("Action-Adventure", entry.genre)
    }

    @Test
    fun `parse Logiqx XML DAT format`() {
        val xml = """
            <?xml version="1.0" encoding="utf-8"?>
            <datafile>
                <game name="Sonic The Hedgehog">
                    <description>Sonic The Hedgehog (USA, Europe)</description>
                    <year>1991</year>
                    <manufacturer>Sega</manufacturer>
                    <rom name="sonic1.bin" size="524288" crc="12345678" />
                </game>
            </datafile>
        """.trimIndent()

        val entries = parser.parse(ByteArrayInputStream(xml.toByteArray()))

        assertEquals(1, entries.size)
        val entry = entries.first()
        assertEquals("Sonic The Hedgehog", entry.title)
        assertEquals("sonic1.bin", entry.romFileName)
        assertEquals("Sega", entry.developer)
        assertEquals("Sega", entry.publisher)
        assertEquals("Sonic The Hedgehog (USA, Europe)", entry.description)
        assertEquals("1991", entry.releaseDate)
    }
}
