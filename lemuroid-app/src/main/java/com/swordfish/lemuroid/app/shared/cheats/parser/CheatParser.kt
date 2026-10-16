package com.swordfish.lemuroid.app.shared.cheats.parser

import com.swordfish.lemuroid.app.shared.cheats.Cheat
import java.io.InputStream
import java.util.Properties

object CheatParser {
    fun parse(inputStream: InputStream): List<Cheat> {
        val properties = Properties()
        try {
            properties.load(inputStream)
        } catch (e: Exception) {
            return emptyList()
        }

        // Normalize all keys by trimming and lowercasing them for case-insensitive lookup
        val normalizedProps = mutableMapOf<String, String>()
        for ((key, value) in properties) {
            if (key != null && value != null) {
                normalizedProps[key.toString().trim().lowercase()] = value.toString().trim()
            }
        }

        // Try getting count from "cheats" key
        val cheatsCountStr = normalizedProps["cheats"]?.removeSurrounding("\"")
        var cheatsCount = cheatsCountStr?.toIntOrNull() ?: 0
        
        // Fallback: If "cheats" is 0, try to count cheatX_code occurrences
        if (cheatsCount == 0) {
            cheatsCount = normalizedProps.keys.count { it.startsWith("cheat") && it.endsWith("_code") }
        }

        if (cheatsCount == 0) {
            return emptyList()
        }
        
        val cheats = mutableListOf<Cheat>()

        // Use a map to handle non-contiguous indices if they exist
        val indices = normalizedProps.keys
            .filter { it.startsWith("cheat") && it.endsWith("_code") }
            .mapNotNull { it.removePrefix("cheat").removeSuffix("_code").toIntOrNull() }
            .sorted()

        for (i in indices) {
            val codeKey = "cheat${i}_code"
            var code = normalizedProps[codeKey]?.removeSurrounding("\"") ?: continue
            
            // Handle hybrid format: strip _L prefix if present
            if (code.startsWith("_L ")) {
                code = code.removePrefix("_L ").trim()
            }

            val descKey = "cheat${i}_desc"
            val desc = normalizedProps[descKey]?.removeSurrounding("\"") ?: "Cheat $i"

            val enableKey = "cheat${i}_enable"
            val enabled = normalizedProps[enableKey]?.equals("true", ignoreCase = true) ?: false

            // Check multiple possible type keys
            val typeKey1 = "cheat${i}_type"
            val typeKey2 = "cheat${i}_cheat_type"
            val typeValue = (normalizedProps[typeKey1] ?: normalizedProps[typeKey2])?.removeSurrounding("\"")

            val type = when(typeValue?.lowercase()) {
                "1" -> "GameShark"
                "2" -> "ActionReplay"
                "3", "gamegenie", "game genie", "gg" -> "GameGenie"
                "4", "codebreaker", "code breaker", "cb" -> "CodeBreaker"
                "gameshark", "game shark", "gs" -> "GameShark"
                "actionreplay", "action replay", "ar" -> "ActionReplay"
                else -> typeValue
            }

            cheats.add(Cheat(i, desc, code, enabled, type))
        }

        return cheats
    }
}











