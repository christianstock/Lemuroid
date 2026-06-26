package com.swordfish.lemuroid.app.shared.cheats.parser

import com.swordfish.lemuroid.app.shared.cheats.Cheat
import java.io.InputStream

object CwCheatParser {
    /**
     * Parse CWCheat format (used by PPSSPP for PSP cheats)
     * Format:
     * _S ULCES12345
     * _G "Game Name"
     * _C0 "Cheat Name"
     * _L 0xAddress 0xValue
     * _L 0xAddress2 0xValue2
     * _C1 "Another Cheat"
     * _L 0xAddress3 0xValue3
     */
    fun parse(inputStream: InputStream): List<Cheat> {
        val cheats = mutableListOf<Cheat>()
        val lines = inputStream.bufferedReader().readLines()

        var currentCheatIndex = 0
        var currentCheatName = ""
        var currentCheatCode = mutableListOf<String>()

        for (line in lines) {
            val trimmedLine = line.trim()

            // Skip empty lines and game section headers
            if (trimmedLine.isEmpty() || trimmedLine.startsWith("_S") || trimmedLine.startsWith("_G")) {
                continue
            }

            // Cheat title (starts with _C followed by digit or just _C)
            if (trimmedLine.startsWith("_C")) {
                // Save previous cheat if it has code
                if (currentCheatCode.isNotEmpty()) {
                    cheats.add(
                        Cheat(
                            index = currentCheatIndex++,
                            description = currentCheatName.ifEmpty { "Cheat ${currentCheatIndex}" },
                            code = currentCheatCode.joinToString("\n"),
                            enabled = false,
                            type = "CWCheat"
                        )
                    )
                }

                // Parse new cheat title
                currentCheatName = trimmedLine
                    .removePrefix("_C0 ")
                    .removePrefix("_C1 ")
                    .removePrefix("_C ")
                    .removeSurrounding("\"")
                    .ifEmpty { "Cheat ${currentCheatIndex}" }

                currentCheatCode = mutableListOf()
            }

            // Code line (starts with _L)
            if (trimmedLine.startsWith("_L")) {
                val codeValue = trimmedLine
                    .removePrefix("_L")
                    .trim()
                    .ifEmpty { null }

                if (codeValue != null) {
                    currentCheatCode.add(codeValue)
                }
            }
        }

        // Don't forget the last cheat
        if (currentCheatCode.isNotEmpty()) {
            cheats.add(
                Cheat(
                    index = currentCheatIndex,
                    description = currentCheatName.ifEmpty { "Cheat ${currentCheatIndex}" },
                    code = currentCheatCode.joinToString("\n"),
                    enabled = false,
                    type = "CWCheat"
                )
            )
        }

        return cheats
    }
}

