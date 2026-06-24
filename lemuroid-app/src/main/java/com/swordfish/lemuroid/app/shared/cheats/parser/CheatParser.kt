package com.swordfish.lemuroid.app.shared.cheats.parser

import com.swordfish.lemuroid.app.shared.cheats.Cheat
import java.io.InputStream
import java.util.Properties

object CheatParser {
    fun parse(inputStream: InputStream): List<Cheat> {
        val properties = Properties()
        properties.load(inputStream)

        // Normalize all keys by trimming them (in case file has spaces around = sign)
        val normalizedProps = mutableMapOf<String, String>()
        for ((key, value) in properties) {
            normalizedProps[(key as String).trim()] = (value as String).trim()
        }

        val cheatsCount = normalizedProps["cheats"]?.toIntOrNull() ?: 0
        val cheats = mutableListOf<Cheat>()

        for (i in 0 until cheatsCount) {
            val codeKey = "cheat${i}_code"
            val code = normalizedProps[codeKey]?.removeSurrounding("\"") ?: continue

            val descKey = "cheat${i}_desc"
            var desc = normalizedProps[descKey]?.removeSurrounding("\"") ?: "Cheat $i"

            val enableKey = "cheat${i}_enable"
            val enabled = normalizedProps[enableKey]?.toBoolean() ?: false

            // Check multiple possible type keys
            val typeKey1 = "cheat${i}_type"
            val typeKey2 = "cheat${i}_cheat_type"
            val typeValue = (normalizedProps[typeKey1] ?: normalizedProps[typeKey2])?.removeSurrounding("\"")

            val type = when(typeValue) {
                "1" -> "GameShark"
                "2" -> "ActionReplay"
                "3" -> "GameGenie"
                "4" -> "CodeBreaker"
                else -> typeValue
            }

            cheats.add(Cheat(i, desc, code, enabled, type))
        }

        return cheats
    }
}


