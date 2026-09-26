package com.swordfish.lemuroid.lib.library.db.dao

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {
    val VERSION_8_9: Migration =
        object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `datafiles`(
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `gameId` INTEGER NOT NULL,
                        `fileName` TEXT NOT NULL,
                        `fileUri` TEXT NOT NULL,
                        `lastIndexedAt` INTEGER NOT NULL,
                        `path` TEXT, FOREIGN KEY(`gameId`
                    ) REFERENCES `games`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )
                    """.trimIndent(),
                )

                database.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS `index_datafiles_id` ON `datafiles` (`id`)
                    """.trimIndent(),
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS `index_datafiles_fileUri` ON `datafiles` (`fileUri`)
                    """.trimIndent(),
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS `index_datafiles_gameId` ON `datafiles` (`gameId`)
                    """.trimIndent(),
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS `index_datafiles_lastIndexedAt` ON `datafiles` (`lastIndexedAt`)
                    """.trimIndent(),
                )
            }
        }

    val VERSION_9_10: Migration =
        object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `game_cheats` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `gameId` INTEGER NOT NULL,
                        `zipUri` TEXT NOT NULL,
                        `entryName` TEXT NOT NULL,
                        `cheatIndex` INTEGER NOT NULL,
                        `description` TEXT NOT NULL,
                        `code` TEXT NOT NULL,
                        `enabled` INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS `index_game_cheats_gameId` ON `game_cheats` (`gameId`)
                    """.trimIndent(),
                )
            }
        }

    val VERSION_11_12: Migration =
        object : Migration(11, 12) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE games ADD COLUMN publisher TEXT")
                database.execSQL("ALTER TABLE games ADD COLUMN coverBackUrl TEXT")
                database.execSQL("ALTER TABLE games ADD COLUMN releaseDate TEXT")
                database.execSQL("ALTER TABLE games ADD COLUMN summary TEXT")
                database.execSQL("ALTER TABLE games ADD COLUMN country TEXT")
            }
        }

    val VERSION_12_13: Migration =
        object : Migration(12, 13) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE game_cheats ADD COLUMN displayOrder INTEGER NOT NULL DEFAULT 0")
            }
        }

    val VERSION_13_14: Migration =
        object : Migration(13, 14) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE games ADD COLUMN cartridgeUrl TEXT")
            }
        }

    val VERSION_14_15: Migration =
        object : Migration(14, 15) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE games ADD COLUMN manualUrl TEXT")
            }
        }
}
