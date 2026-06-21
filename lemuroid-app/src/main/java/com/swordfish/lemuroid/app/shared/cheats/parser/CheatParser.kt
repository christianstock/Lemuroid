package com.swordfish.lemuroid.app.shared.cheats.parser

import com.swordfish.lemuroid.app.shared.cheats.Cheat
import java.io.InputStream
import java.util.Properties

object CheatParser {
    fun parse(inputStream: InputStream): List<Cheat> {
        val properties = Properties()
        properties.load(inputStream)

        val cheatsCount = properties.getProperty("cheats")?.toIntOrNull() ?: 0
        val cheats = mutableListOf<Cheat>()

        for (i in 0 until cheatsCount) {
            var desc = properties.getProperty("cheat${i}_desc") ?: "Cheat $i"
            var code = properties.getProperty("cheat${i}_code") ?: continue
            val enabled = properties.getProperty("cheat${i}_enable")?.trim()?.toBoolean() ?: false

            desc = desc.trim().removeSurrounding("\"")
            code = code.trim().removeSurrounding("\"")

            cheats.add(Cheat(i, desc, code, enabled))
        }

        return cheats
    }
}
