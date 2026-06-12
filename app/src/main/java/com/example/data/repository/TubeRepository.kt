package com.example.data.repository

import android.util.Log
import com.example.BuildConfig
import com.example.data.api.Content
import com.example.data.api.GenerateContentRequest
import com.example.data.api.Part
import com.example.data.api.RetrofitClient
import com.example.data.database.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

data class Recommendation(
    val title: String,
    val channelTitle: String,
    val videoId: String,
    val isMusic: Boolean = false,
    val reason: String = "Curated Recommendation",
    val duration: String = "4:15",
    val category: String = "General"
)

class TubeRepository(private val tubeDao: TubeDao) {

    // --- REPLAY CHANNELS AND HISTORY ---
    val viewHistory: Flow<List<HistoryEntity>> = tubeDao.getHistory()
    val playlists: Flow<List<PlaylistEntity>> = tubeDao.getAllPlaylists()
    val offlineVideos: Flow<List<OfflineVideoEntity>> = tubeDao.getOfflineVideos()

    suspend fun addToHistory(videoId: String, title: String, channelTitle: String, category: String) = withContext(Dispatchers.IO) {
        val entry = HistoryEntity(
            videoId = videoId,
            title = title,
            channelTitle = channelTitle,
            category = category
        )
        tubeDao.insertHistory(entry)
    }

    suspend fun deleteHistoryItem(id: Int) = withContext(Dispatchers.IO) {
        tubeDao.deleteHistoryItem(id)
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        tubeDao.clearHistory()
    }

    // --- PLAYLIST SECTOR ---
    suspend fun createPlaylist(name: String, description: String): Long = withContext(Dispatchers.IO) {
        val playlist = PlaylistEntity(name = name, description = description)
        tubeDao.createPlaylist(playlist)
    }

    suspend fun deletePlaylist(id: Int) = withContext(Dispatchers.IO) {
        tubeDao.deletePlaylist(id)
    }

    fun getItemsForPlaylist(playlistId: Int): Flow<List<PlaylistItemEntity>> {
        return tubeDao.getItemsForPlaylist(playlistId)
    }

    suspend fun addItemToPlaylist(playlistId: Int, videoId: String, title: String, channelTitle: String, isMusic: Boolean) = withContext(Dispatchers.IO) {
        val item = PlaylistItemEntity(
            playlistId = playlistId,
            videoId = videoId,
            title = title,
            channelTitle = channelTitle,
            isMusic = isMusic
        )
        tubeDao.insertPlaylistItem(item)
    }

    suspend fun deletePlaylistItem(id: Int) = withContext(Dispatchers.IO) {
        tubeDao.deletePlaylistItem(id)
    }

    // --- OFFLINE BOOKMARKS & CACHES ---
    suspend fun saveOfflineVideo(videoId: String, title: String, channelTitle: String, isMusic: Boolean, description: String = "Saved offline", mockAudioFilePath: String = "") = withContext(Dispatchers.IO) {
        val entity = OfflineVideoEntity(
            videoId = videoId,
            title = title,
            channelTitle = channelTitle,
            description = description,
            isDownloaded = true,
            isMusic = isMusic,
            mockAudioFilePath = mockAudioFilePath
        )
        tubeDao.insertOfflineVideo(entity)
    }

    suspend fun removeOfflineVideo(videoId: String) = withContext(Dispatchers.IO) {
        tubeDao.deleteOfflineVideo(videoId)
    }

    // --- STATIC SEED LIST FOR NO-HISTORY MODE ---
    val defaultSeeds = listOf(
        Recommendation("Lofi Girl - lofi hip hop radio - beats to relax/study to", "Lofi Girl", "jfKfPfyJRdk", false, "Featured study beats stream", "Live", "Ambient"),
        Recommendation("Deep Focus Ambient Music for Coding/Studying", "Chilli Beats", "tntOCGkgt98", false, "Excellent background soundscape", "3:10:00", "Chill"),
        Recommendation("Liquid Drum & Bass Mix for Focus & Energy", "D&B Lounge", "3JZ_D3ELwOQ", true, "High tempo streaming beats", "54:12", "Music"),
        Recommendation("Synthesizer Chillwave Beats & Retro Synthwave", "RetroWaves", "D5g0_nS_uRE", true, "Warm neon ambient analog tones", "2:05:00", "Music"),
        Recommendation("Perfect - Official Music Video", "Ed Sheeran", "2Vv-BfVoq4g", true, "Top romantic melody selection", "4:40", "Music"),
        Recommendation("Shape of You - Acoustic Classic", "Ed Sheeran", "JGwWNGJdvx8", true, "Beautiful acoustic live session", "3:55", "Music"),
        Recommendation("Roar - Dynamic Stadium Mix", "Katy Perry", "CevxZvSJLk8", true, "Inspiring energy booster song", "3:43", "Music")
    )

    // --- GEMINI POWERED DYNAMIC PERSONALIZATION FEED ---
    suspend fun fetchPersonalizedRecommendations(history: List<HistoryEntity>): List<Recommendation> = withContext(Dispatchers.IO) {
        if (history.isEmpty()) {
            return@withContext defaultSeeds
        }

        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e("TubeRepository", "Gemini API key is unconfigured, falling back to default curated seeds.")
            return@withContext defaultSeeds
        }

        // Aggregate history to explain preferences to Gemini
        val historyOverview = history.take(15).mapIndexed { idx, it ->
            "${idx + 1}. Title: \"${it.title}\" by Creator: \"${it.channelTitle}\" (Category: ${it.category})"
        }.joinToString("\n")

        val prompt = """
            You are a premium AI entertainment engine. Based on the following viewing history of the user, generate exactly 5-6 personalized YouTube and YouTube Music recommendations.
            
            Viewing History:
            $historyOverview
            
            For each recommendation, output:
            1. Title: A real-sounding video or song title
            2. Creator/Channel: The appropriate creator/channel
            3. VideoId: A real YouTube videoId that matches (e.g. from valid YouTube Music/lofi library, or use mock realistic IDs like 'jfKfPfyJRdk', '2Vv-BfVoq4g', 'tntOCGkgt98', '3JZ_D3ELwOQ' or similar standard IDs)
            4. IsMusic: true if it is primarily music/audio, false if it is video/lofi
            5. Reason: A friendly explanations on why you recommend it based on their history (keep it under 15 words)
            6. Duration: approximate duration (e.g., '4:15', '12:30')
            7. Category: Category name
            
            Return ONLY a valid JSON Array. Do NOT include markdown blocks. Do NOT include ```json markup. Just output the clean JSON text. All keys must be inside quotes.
            JSON Schema:
            [
              {
                "title": "Example Video",
                "channelTitle": "Example Channel",
                "videoId": "tntOCGkgt98",
                "isMusic": true,
                "reason": "Because you listened to Chilli Beats",
                "duration": "5:30",
                "category": "Chill"
              }
            ]
        """.trimIndent()

        try {
            val request = GenerateContentRequest(
                contents = listOf(Content(parts = listOf(Part(text = prompt))))
            )
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val responseText = response.candidates?.flatMap { it.content?.parts ?: emptyList() }
                ?.firstOrNull()?.text ?: ""

            if (responseText.isNotEmpty()) {
                val cleanJson = responseText.trim()
                    .removePrefix("```json")
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()

                val moshi = Moshi.Builder().build()
                val listType = Types.newParameterizedType(List::class.java, Map::class.java, String::class.java, Any::class.java)
                val adapter = moshi.adapter<List<Map<String, Any>>>(listType)
                val parsed = adapter.fromJson(cleanJson)

                if (parsed != null) {
                    val recommendations = parsed.map { map ->
                        val duration = (map["duration"] ?: "3:45") as String
                        val isMusic = when (val musicVal = map["isMusic"]) {
                            is Boolean -> musicVal
                            is String -> musicVal.toBoolean()
                            else -> true
                        }
                        Recommendation(
                            title = (map["title"] ?: "Personalized Stream") as String,
                            channelTitle = (map["channelTitle"] ?: "Curated Channel") as String,
                            videoId = (map["videoId"] ?: "jfKfPfyJRdk") as String,
                            isMusic = isMusic,
                            reason = (map["reason"] ?: "Based on your viewing habits") as String,
                            duration = duration,
                            category = (map["category"] ?: "Recommended") as String
                        )
                    }
                    if (recommendations.isNotEmpty()) {
                        return@withContext recommendations
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("TubeRepository", "Failed to get AI recommendations, falling back to default seed list.", e)
        }

        return@withContext defaultSeeds
    }
}
