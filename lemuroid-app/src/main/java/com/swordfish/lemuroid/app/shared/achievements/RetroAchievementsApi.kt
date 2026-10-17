package com.swordfish.lemuroid.app.shared.achievements

import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

data class RetroAchievementDto(
    val id: Int,
    val title: String,
    val description: String,
    val points: Int,
    val badgeName: String,
    val isUnlocked: Boolean,
    val unlockTimestamp: Long?,
    val displayOrder: Int,
) {
    val badgeUrl: String
        get() = "https://media.retroachievements.org/Badge/$badgeName.png"
}

data class RetroLeaderboardDto(
    val id: Int,
    val title: String,
    val description: String,
    val format: String,
    val userRank: Int?,
    val userResult: String?,
    val topEntryUser: String?,
    val topEntryResult: String?,
    val displayOrder: Int,
)

data class RetroGameInfoAndProgress(
    val gameId: Int,
    val title: String,
    val achievements: List<RetroAchievementDto>,
)

class RetroAchievementsApi(
    private val okHttpClient: OkHttpClient,
) {
    companion object {
        private const val BASE_URL = "https://retroachievements.org"
    }

    suspend fun login(username: String, password: String): String? = withContext(Dispatchers.IO) {
        runCatching {
            val url = "$BASE_URL/dorequest.php".toHttpUrlOrNull()!!
                .newBuilder()
                .addQueryParameter("r", "login")
                .addQueryParameter("u", username)
                .addQueryParameter("p", password)
                .build()

            val request = Request.Builder().url(url).build()
            val response = okHttpClient.newCall(request).execute()
            val body = response.body?.string() ?: return@runCatching null
            val json = JSONObject(body)

            if (json.optBoolean("Success", false)) {
                json.optString("Token", null)
            } else {
                null
            }
        }.getOrNull()
    }

    suspend fun getGameIdByMd5(md5Hash: String): Int? = withContext(Dispatchers.IO) {
        runCatching {
            val url = "$BASE_URL/dorequest.php".toHttpUrlOrNull()!!
                .newBuilder()
                .addQueryParameter("r", "gameid")
                .addQueryParameter("m", md5Hash.lowercase())
                .build()

            val request = Request.Builder().url(url).build()
            val response = okHttpClient.newCall(request).execute()
            val body = response.body?.string()?.trim() ?: return@runCatching null

            if (body.startsWith("{")) {
                val json = JSONObject(body)
                val id = json.optInt("GameID", 0)
                if (id > 0) id else null
            } else {
                val id = body.toIntOrNull()
                if (id != null && id > 0) id else null
            }
        }.getOrNull()
    }

    suspend fun getGameInfoAndUserProgress(
        username: String,
        apiKey: String,
        gameId: Int,
    ): RetroGameInfoAndProgress? = withContext(Dispatchers.IO) {
        runCatching {
            val url = "$BASE_URL/API/API_GetGameInfoAndUserProgress.php".toHttpUrlOrNull()!!
                .newBuilder()
                .addQueryParameter("z", username)
                .addQueryParameter("y", apiKey)
                .addQueryParameter("u", username)
                .addQueryParameter("g", gameId.toString())
                .build()

            val request = Request.Builder().url(url).build()
            val response = okHttpClient.newCall(request).execute()
            val body = response.body?.string() ?: return@runCatching null
            val json = JSONObject(body)

            val id = json.optInt("ID", gameId)
            val title = json.optString("Title", "")

            val achievementsList = mutableListOf<RetroAchievementDto>()
            val achievementsJson = json.opt("Achievements")

            if (achievementsJson is JSONObject) {
                val keys = achievementsJson.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val item = achievementsJson.optJSONObject(key) ?: continue
                    val achId = item.optInt("ID", key.toIntOrNull() ?: 0)
                    val achTitle = item.optString("Title", "")
                    val achDesc = item.optString("Description", "")
                    val points = item.optInt("Points", 0)
                    val badgeName = item.optString("BadgeName", "")
                    val displayOrder = item.optInt("DisplayOrder", 0)
                    val dateEarned = item.optString("DateEarned", "").takeIf { it.isNotBlank() && it != "null" }
                    val dateEarnedHardcore = item.optString("DateEarnedHardcore", "").takeIf { it.isNotBlank() && it != "null" }

                    val isUnlocked = dateEarned != null || dateEarnedHardcore != null
                    val unlockTimestamp = System.currentTimeMillis() // default or parsed timestamp

                    achievementsList.add(
                        RetroAchievementDto(
                            id = achId,
                            title = achTitle,
                            description = achDesc,
                            points = points,
                            badgeName = badgeName,
                            isUnlocked = isUnlocked,
                            unlockTimestamp = if (isUnlocked) unlockTimestamp else null,
                            displayOrder = displayOrder,
                        ),
                    )
                }
            }

            achievementsList.sortBy { it.displayOrder }

            RetroGameInfoAndProgress(
                gameId = id,
                title = title,
                achievements = achievementsList,
            )
        }.getOrNull()
    }

    suspend fun getGameLeaderboards(
        username: String,
        apiKey: String,
        gameId: Int,
    ): List<RetroLeaderboardDto> = withContext(Dispatchers.IO) {
        runCatching {
            val url = "$BASE_URL/API/API_GetGameExtended.php".toHttpUrlOrNull()!!
                .newBuilder()
                .addQueryParameter("z", username)
                .addQueryParameter("y", apiKey)
                .addQueryParameter("g", gameId.toString())
                .build()

            val request = Request.Builder().url(url).build()
            val response = okHttpClient.newCall(request).execute()
            val body = response.body?.string() ?: return@runCatching emptyList()
            val json = JSONObject(body)

            val leaderboardsList = mutableListOf<RetroLeaderboardDto>()
            val leaderboardsJson = json.opt("Leaderboards")

            if (leaderboardsJson is JSONObject) {
                val keys = leaderboardsJson.keys()
                var order = 0
                while (keys.hasNext()) {
                    val key = keys.next()
                    val item = leaderboardsJson.optJSONObject(key) ?: continue
                    val lbId = item.optInt("ID", key.toIntOrNull() ?: 0)
                    val title = item.optString("Title", "")
                    val description = item.optString("Description", "")
                    val format = item.optString("Format", "TIME")
                    val userRank = item.optInt("Rank", 0).takeIf { it > 0 }
                    val userResult = item.optString("Result", "").takeIf { it.isNotBlank() && it != "null" }

                    var topUser: String? = null
                    var topResult: String? = null

                    val topEntry = item.optJSONObject("TopEntry")
                    if (topEntry != null) {
                        topUser = topEntry.optString("User", "").takeIf { it.isNotBlank() }
                        topResult = topEntry.optString("FormattedScore", topEntry.optString("Score", "")).takeIf { it.isNotBlank() }
                    }

                    leaderboardsList.add(
                        RetroLeaderboardDto(
                            id = lbId,
                            title = title,
                            description = description,
                            format = format,
                            userRank = userRank,
                            userResult = userResult,
                            topEntryUser = topUser,
                            topEntryResult = topResult,
                            displayOrder = order++,
                        ),
                    )
                }
            }

            leaderboardsList
        }.getOrDefault(emptyList())
    }
}
