package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.database.HistoryEntity
import com.example.data.database.OfflineVideoEntity
import com.example.data.database.PlaylistEntity
import com.example.data.database.PlaylistItemEntity
import com.example.data.repository.Recommendation
import com.example.data.repository.TubeRepository
import kotlinx.coroutines.Delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface Tab {
    object Browse : Tab
    object Player : Tab
    object Recommendations : Tab
    object Playlists : Tab
    object Offline : Tab
}

class TubeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = androidx.room.Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        "tube_companion_db"
    ).build()

    private val repository = TubeRepository(db.tubeDao())

    // --- RESPONSIVE STATEFLOWS FROM REPOSITORY ---
    val viewHistory = repository.viewHistory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val playlists = repository.playlists.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val offlineVideos = repository.offlineVideos.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // --- NAVIGATION SELECTION ---
    private val _currentTab = MutableStateFlow<Tab>(Tab.Browse)
    val currentTab: StateFlow<Tab> = _currentTab.asStateFlow()

    fun selectTab(tab: Tab) {
        _currentTab.value = tab
    }

    // --- GEMINI RECOMMENDATION ENGINE ---
    private val _aiRecommendationList = MutableStateFlow<List<Recommendation>>(repository.defaultSeeds)
    val aiRecommendationList: StateFlow<List<Recommendation>> = _aiRecommendationList.asStateFlow()

    private val _isLoadingRecommendations = MutableStateFlow(false)
    val isLoadingRecommendations: StateFlow<Boolean> = _isLoadingRecommendations.asStateFlow()

    private val _aiErrorLog = MutableStateFlow<String?>(null)
    val aiErrorLog: StateFlow<String?> = _aiErrorLog.asStateFlow()

    fun triggerAiRecommendations() {
        viewModelScope.launch {
            _isLoadingRecommendations.value = true
            _aiErrorLog.value = null
            try {
                val list = repository.fetchPersonalizedRecommendations(viewHistory.value)
                _aiRecommendationList.value = list
            } catch (e: Exception) {
                _aiErrorLog.value = e.localizedMessage ?: "Connection Timeout"
                _aiRecommendationList.value = repository.defaultSeeds
            } finally {
                _isLoadingRecommendations.value = false
            }
        }
    }

    // --- ACTIVE PLAYBACK HUD DECK ---
    private val _activeVideoId = MutableStateFlow("jfKfPfyJRdk") // Default Lofi Girl
    val activeVideoId: StateFlow<String> = _activeVideoId.asStateFlow()

    private val _activeTitle = MutableStateFlow("Lofi Girl - lofi hip hop radio")
    val activeTitle: StateFlow<String> = _activeTitle.asStateFlow()

    private val _activeChannel = MutableStateFlow("Lofi Girl")
    val activeChannel: StateFlow<String> = _activeChannel.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackProgress = MutableStateFlow(0f) // 0.0f to 1.0f
    val playbackProgress: StateFlow<Float> = _playbackProgress.asStateFlow()

    private val _playbackTimeFormatted = MutableStateFlow("00:00")
    val playbackTimeFormatted: StateFlow<String> = _playbackTimeFormatted.asStateFlow()

    private val _isMusicPlayerMode = MutableStateFlow(false)
    val isMusicPlayerMode: StateFlow<Boolean> = _isMusicPlayerMode.asStateFlow()

    // --- AUDIO QUALITY & SUBTITLE CONTROLS ---
    private val _audioQuality = MutableStateFlow("Hi-Res FLAC (24-bit/96kHz)")
    val audioQuality: StateFlow<String> = _audioQuality.asStateFlow()

    private val _subtitlesEnabled = MutableStateFlow(true)
    val subtitlesEnabled: StateFlow<Boolean> = _subtitlesEnabled.asStateFlow()

    private val _subtitleLanguage = MutableStateFlow("English")
    val subtitleLanguage: StateFlow<String> = _subtitleLanguage.asStateFlow()

    private val _currentSubtitleText = MutableStateFlow("🎵 [Ambient melodic intro playing] 🎵")
    val currentSubtitleText: StateFlow<String> = _currentSubtitleText.asStateFlow()

    // --- AD BLOCKING CONFIGS & STATS ---
    private val _adBlockingActive = MutableStateFlow(true)
    val adBlockingActive: StateFlow<Boolean> = _adBlockingActive.asStateFlow()

    private val _blockedAdsCount = MutableStateFlow(342) // Seed starting ad-count
    val blockedAdsCount: StateFlow<Int> = _blockedAdsCount.asStateFlow()

    fun toggleAdBlocking() {
        _adBlockingActive.value = !_adBlockingActive.value
    }

    fun incrementBlockedAds() {
        if (_adBlockingActive.value) {
            _blockedAdsCount.value += 1
        }
    }

    // --- SYSTEM OPTIONS ---
    private val _backgroundPlaybackActive = MutableStateFlow(true)
    val backgroundPlaybackActive: StateFlow<Boolean> = _backgroundPlaybackActive.asStateFlow()

    private val _isOfflineSimulated = MutableStateFlow(false) // Toggle to experience strict local playback
    val isOfflineSimulated: StateFlow<Boolean> = _isOfflineSimulated.asStateFlow()

    private val _syncStatusText = MutableStateFlow("Synced 2 mins ago")
    val syncStatusText: StateFlow<String> = _syncStatusText.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    // --- DOWNLOADING METADATA CONTROLS ---
    private val _activeDownloads = MutableStateFlow<Map<String, Float>>(emptyMap()) // videoId to progress (value 0f-1f)
    val activeDownloads: StateFlow<Map<String, Float>> = _activeDownloads.asStateFlow()

    init {
        // Run a simulation loop for progress, subtitles, and playback ticker
        viewModelScope.launch {
            var ticker = 0
            while (true) {
                delay(1000)
                if (_isPlaying.value) {
                    ticker += 1
                    val currentProgress = _playbackProgress.value + 0.005f
                    if (currentProgress >= 1f) {
                        _playbackProgress.value = 0f
                        _isPlaying.value = false
                    } else {
                        _playbackProgress.value = currentProgress
                    }

                    // Format track time
                    val totalSec = (currentProgress * 240).toInt() // assume 4 mins track
                    val mins = totalSec / 60
                    val secs = totalSec % 60
                    _playbackTimeFormatted.value = String.format("%02d:%02d", mins, secs)

                    // Subtitle updates
                    updateSubtitleCap(totalSec)
                }
            }
        }
    }

    private fun updateSubtitleCap(seconds: Int) {
        if (!_subtitlesEnabled.value) {
            _currentSubtitleText.value = ""
            return
        }

        val capsEnglish = listOf(
            0 to "🎵 [Mellow lofi synthesis introduces a calm acoustic theme] 🎵",
            10 to "🎵 [Dynamic piano chords swelling, welcoming peaceful mind space] 🎵",
            25 to "🎵 [Crisp sub-bass kicks in with warm analogue crackling] 🎵",
            40 to "🔔 [Gentle brass bell tones echo, relaxing sensory focus] 🔔",
            55 to "✨ [Voiceover Sample: 'Welcome to Tube Companion, your ad-free escape'] ✨",
            70 to "🎵 [Smooth electronic rhythm loops, elevating evening mood] 🎵",
            90 to "🎵 [Relaxing acoustic guitar strings fade into record crackle] 🎵",
            110 to "🎵 [Subtle vinyl fuzz layer with nostalgic keyboard delay] 🎵"
        )

        val capsSpanish = listOf(
            0 to "🎵 [La síntesis lofi suave introduce un tema acústico tranquilo] 🎵",
            10 to "🎵 [Acordes de piano dinámicos que se hinchan para relajar la mente] 🎵",
            25 to "🎵 [El bajo nítido entra con un cálido crujido analógico] 🎵",
            40 to "🔔 [Tonos suaves de campanillas de latón resuenan en el fondo] 🔔",
            55 to "✨ [Muestra de voz: 'Bienvenido a Tube Companion, tu escape sin anuncios'] ✨",
            70 to "🎵 [El ritmo electrónico suave entra en bucle, elevando el ánimo] 🎵",
            90 to "🎵 [Cuerdas de guitarra acústica relajantes se desvanecen en crujidos] 🎵",
            110 to "🎵 [Capa sutil de pelusa de vinilo con retraso de teclado nostálgico] 🎵"
        )

        val _caps = if (_subtitleLanguage.value == "Spanish") capsSpanish else capsEnglish
        val activeText = _caps.lastOrNull { seconds >= it.first }?.second ?: "🎵 [Lounge streaming loops] 🎵"
        _currentSubtitleText.value = activeText
    }

    // --- ACTIONS ---

    fun startPlayback(videoId: String, title: String, channel: String, isMusic: Boolean = false) {
        _activeVideoId.value = videoId
        _activeTitle.value = title
        _activeChannel.value = channel
        _playbackProgress.value = 0f
        _isPlaying.value = true
        _isMusicPlayerMode.value = isMusic

        // Save entry to local history DB
        viewModelScope.launch {
            repository.addToHistory(
                videoId = videoId,
                title = title,
                channelTitle = channel,
                category = if (isMusic) "Music" else "Video"
            )
            // Refresh AI suggestions occasionally after watching
            triggerAiRecommendations()
        }
    }

    fun togglePlayback() {
        _isPlaying.value = !_isPlaying.value
    }

    fun seekTo(progress: Float) {
        _playbackProgress.value = progress.coerceIn(0f, 1f)
        val totalSec = (progress * 240).toInt()
        val mins = totalSec / 60
        val secs = totalSec % 60
        _playbackTimeFormatted.value = String.format("%02d:%02d", mins, secs)
        updateSubtitleCap(totalSec)
    }

    fun setAudioQuality(quality: String) {
        _audioQuality.value = quality
    }

    fun setSubtitleLanguage(lang: String) {
        _subtitleLanguage.value = lang
        val totalSec = (_playbackProgress.value * 240).toInt()
        updateSubtitleCap(totalSec)
    }

    fun toggleSubtitles() {
        _subtitlesEnabled.value = !_subtitlesEnabled.value
    }

    fun toggleOfflineSimulated() {
        _isOfflineSimulated.value = !_isOfflineSimulated.value
    }

    fun toggleBackgroundPlayback() {
        _backgroundPlaybackActive.value = !_backgroundPlaybackActive.value
    }

    // --- DATABASE ACTIONS WRAPPERS ---

    fun createPlaylist(name: String, desc: String) {
        viewModelScope.launch {
            repository.createPlaylist(name, desc)
        }
    }

    fun deletePlaylist(id: Int) {
        viewModelScope.launch {
            repository.deletePlaylist(id)
        }
    }

    fun getItemsForPlaylist(playlistId: Int): Flow<List<PlaylistItemEntity>> {
        return repository.getItemsForPlaylist(playlistId)
    }

    fun addItemToPlaylist(playlistId: Int, videoId: String, title: String, channelTitle: String, isMusic: Boolean) {
        viewModelScope.launch {
            repository.addItemToPlaylist(playlistId, videoId, title, channelTitle, isMusic)
        }
    }

    fun removePlaylistItem(id: Int) {
        viewModelScope.launch {
            repository.deletePlaylistItem(id)
        }
    }

    // --- DOWNLOADER PROTOCOL (MOCK real-time caching) ---
    fun downloadVideoForOffline(videoId: String, title: String, channel: String, isMusic: Boolean) {
        if (_activeDownloads.value.containsKey(videoId)) return

        viewModelScope.launch {
            _activeDownloads.update { it + (videoId to 0f) }
            // Play dynamic download animation steps
            for (step in 1..10) {
                delay(400)
                _activeDownloads.update { it + (videoId to (step / 10f)) }
            }
            // Add to database
            repository.saveOfflineVideo(
                videoId = videoId,
                title = title,
                channelTitle = channel,
                isMusic = isMusic,
                description = "Offline master copy downloaded successfully.",
                mockAudioFilePath = "internal://assets/media/$videoId.mp3"
            )
            // Remove from active tracker
            _activeDownloads.update { it - videoId }
        }
    }

    fun removeOfflineVideo(videoId: String) {
        viewModelScope.launch {
            repository.removeOfflineVideo(videoId)
        }
    }

    fun deleteHistoryItem(id: Int) {
        viewModelScope.launch {
            repository.deleteHistoryItem(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    // --- SYNCHRONIZE CROSS DEVICE ---
    fun syncDevicesNow() {
        viewModelScope.launch {
            _isSyncing.value = true
            delay(1500)
            _syncStatusText.value = "Synced just now"
            _isSyncing.value = false
        }
    }
}
