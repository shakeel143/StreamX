package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- HISTORY ENTITY ---
@Entity(tableName = "view_history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val videoId: String,
    val title: String,
    val channelTitle: String,
    val timestamp: Long = System.currentTimeMillis(),
    val category: String = "General"
)

// --- PLAYLIST ENTITY ---
@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

// --- PLAYLIST ITEM ENTITY ---
@Entity(
    tableName = "playlist_items",
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["playlistId"])]
)
data class PlaylistItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val playlistId: Int,
    val videoId: String,
    val title: String,
    val channelTitle: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isMusic: Boolean = false
)

// --- OFFLINE VIDEO ENTITY (BOOKMARKED FOR OFFLINE USE) ---
@Entity(tableName = "offline_videos")
data class OfflineVideoEntity(
    @PrimaryKey val videoId: String,
    val title: String,
    val channelTitle: String,
    val description: String = "",
    val isDownloaded: Boolean = true,
    val downloadedAt: Long = System.currentTimeMillis(),
    val isMusic: Boolean = false,
    val mockAudioFilePath: String = "" // Holds resource string or cached mock base64 for real offline playing
)

// --- DAOS ---

@Dao
interface TubeDao {
    // History
    @Query("SELECT * FROM view_history ORDER BY timestamp DESC")
    fun getHistory(): Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: HistoryEntity)

    @Query("DELETE FROM view_history WHERE id = :id")
    suspend fun deleteHistoryItem(id: Int)

    @Query("DELETE FROM view_history")
    suspend fun clearHistory()

    // Playlists
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createPlaylist(playlist: PlaylistEntity): Long

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylist(id: Int)

    // Playlist Items
    @Query("SELECT * FROM playlist_items WHERE playlistId = :playlistId ORDER BY timestamp DESC")
    fun getItemsForPlaylist(playlistId: Int): Flow<List<PlaylistItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistItem(item: PlaylistItemEntity)

    @Query("DELETE FROM playlist_items WHERE id = :id")
    suspend fun deletePlaylistItem(id: Int)

    // Offline Videos
    @Query("SELECT * FROM offline_videos ORDER BY downloadedAt DESC")
    fun getOfflineVideos(): Flow<List<OfflineVideoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOfflineVideo(video: OfflineVideoEntity)

    @Query("DELETE FROM offline_videos WHERE videoId = :videoId")
    suspend fun deleteOfflineVideo(videoId: String)
}

// --- DATABASE HOLDER ---
@Database(
    entities = [
        HistoryEntity::class,
        PlaylistEntity::class,
        PlaylistItemEntity::class,
        OfflineVideoEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tubeDao(): TubeDao
}
