package com.example

import android.app.PictureInPictureParams
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.*
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.LightSurface
import com.example.ui.theme.MutedSurface
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TubeAmber
import com.example.ui.theme.TubeRed
import com.example.ui.theme.GlowBlue
import com.example.ui.theme.OledBackground
import com.example.ui.theme.BorderWhiteNormal
import com.example.ui.theme.BorderWhiteSmall
import com.example.ui.viewmodel.Tab
import com.example.ui.viewmodel.TubeViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: TubeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Sync initial AI suggestions list on first start
        viewModel.triggerAiRecommendations()

        setContent {
            MyApplicationTheme {
                val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
                val activeVideoId by viewModel.activeVideoId.collectAsStateWithLifecycle()
                val activeTitle by viewModel.activeTitle.collectAsStateWithLifecycle()
                val activeChannel by viewModel.activeChannel.collectAsStateWithLifecycle()
                val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
                val progress by viewModel.playbackProgress.collectAsStateWithLifecycle()
                val timeFormatted by viewModel.playbackTimeFormatted.collectAsStateWithLifecycle()
                val audioQuality by viewModel.audioQuality.collectAsStateWithLifecycle()
                val subtitlesEnabled by viewModel.subtitlesEnabled.collectAsStateWithLifecycle()
                val subtitleLanguage by viewModel.subtitleLanguage.collectAsStateWithLifecycle()
                val subtitleText by viewModel.currentSubtitleText.collectAsStateWithLifecycle()
                val isMusicMode by viewModel.isMusicPlayerMode.collectAsStateWithLifecycle()

                // Telemetry & DB
                val historyList by viewModel.viewHistory.collectAsStateWithLifecycle()
                val recommendations by viewModel.aiRecommendationList.collectAsStateWithLifecycle()
                val isLoadingRecommendations by viewModel.isLoadingRecommendations.collectAsStateWithLifecycle()
                val blockedAdsCount by viewModel.blockedAdsCount.collectAsStateWithLifecycle()
                val offlineSimulated by viewModel.isOfflineSimulated.collectAsStateWithLifecycle()
                val syncStatus by viewModel.syncStatusText.collectAsStateWithLifecycle()
                val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
                val adBlockingActive by viewModel.adBlockingActive.collectAsStateWithLifecycle()
                val playlists by viewModel.playlists.collectAsStateWithLifecycle()
                val offlineVideosList by viewModel.offlineVideos.collectAsStateWithLifecycle()
                val activeDownloads by viewModel.activeDownloads.collectAsStateWithLifecycle()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        if (currentTab != Tab.Player) {
                            @OptIn(ExperimentalMaterial3Api::class)
                            TopAppBar(
                                title = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        androidx.compose.foundation.Image(
                                            painter = androidx.compose.ui.res.painterResource(id = R.drawable.img_streamx_logo_1781246041009),
                                            contentDescription = "StreamX Logo",
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(RoundedCornerShape(6.dp)),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                        )
                                        Text(
                                            text = "StreamX",
                                            color = Color.White,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = (-0.5).sp
                                        )
                                    }
                                },
                                actions = {
                                    IconButton(onClick = {}) {
                                        Icon(imageVector = Icons.Default.Cast, contentDescription = "Cast", tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                    IconButton(onClick = {}) {
                                        Icon(imageVector = Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                    IconButton(onClick = {}) {
                                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(GlowBlue),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "JD",
                                            color = Color.Black,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = OledBackground,
                                    scrolledContainerColor = OledBackground,
                                    navigationIconContentColor = Color.White,
                                    titleContentColor = Color.White,
                                    actionIconContentColor = Color.White
                                )
                            )
                        }
                    },
                    bottomBar = {
                        Column {
                            // Sticky bottom Mini-Player (Floating style capsule)
                            AnimatedVisibility(
                                visible = activeVideoId.isNotEmpty() && currentTab != Tab.Player,
                                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MutedSurface)
                                        .border(1.dp, BorderWhiteNormal, RoundedCornerShape(12.dp))
                                        .clickable { viewModel.selectTab(Tab.Player) }
                                        .padding(10.dp)
                                        .testTag("floating_mini_player"),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isMusicMode) Icons.Default.MusicNote else Icons.Default.SmartDisplay,
                                        contentDescription = null,
                                        tint = if (isMusicMode) TubeAmber else TubeRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = activeTitle,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = activeChannel,
                                            color = TextSecondary,
                                            fontSize = 10.sp,
                                            maxLines = 1
                                        )
                                        // Small progressive loader
                                        LinearProgressIndicator(
                                            progress = progress,
                                            color = TubeRed,
                                            trackColor = LightSurface,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 4.dp)
                                                .height(2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = { viewModel.togglePlayback() },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = "Quick Play/Pause",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            // Navigation tabs bar
                            NavigationBar(
                                containerColor = OledBackground,
                                contentColor = Color.White,
                                modifier = Modifier
                                    .testTag("navigation_tab_bar")
                                    .windowInsetsPadding(WindowInsets.navigationBars)
                            ) {
                                NavigationBarItem(
                                    selected = currentTab == Tab.Browse,
                                    onClick = { viewModel.selectTab(Tab.Browse) },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentTab == Tab.Browse) Icons.Filled.Language else Icons.Outlined.Language,
                                            contentDescription = "Browse"
                                        )
                                    },
                                    label = { Text("Browse", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = TubeRed,
                                        selectedTextColor = TubeRed,
                                        indicatorColor = LightSurface,
                                        unselectedIconColor = TextSecondary,
                                        unselectedTextColor = TextSecondary
                                    ),
                                    modifier = Modifier.testTag("nav_item_browse")
                                )

                                NavigationBarItem(
                                    selected = currentTab == Tab.Player,
                                    onClick = { viewModel.selectTab(Tab.Player) },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentTab == Tab.Player) Icons.Filled.PlayCircleFilled else Icons.Outlined.PlayCircle,
                                            contentDescription = "Player"
                                        )
                                    },
                                    label = { Text("Player", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = TubeRed,
                                        selectedTextColor = TubeRed,
                                        indicatorColor = LightSurface,
                                        unselectedIconColor = TextSecondary,
                                        unselectedTextColor = TextSecondary
                                    ),
                                    modifier = Modifier.testTag("nav_item_player")
                                )

                                NavigationBarItem(
                                    selected = currentTab == Tab.Recommendations,
                                    onClick = { viewModel.selectTab(Tab.Recommendations) },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentTab == Tab.Recommendations) Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome,
                                            contentDescription = "AI Feed"
                                        )
                                    },
                                    label = { Text("AI Feed", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = TubeRed,
                                        selectedTextColor = TubeRed,
                                        indicatorColor = LightSurface,
                                        unselectedIconColor = TextSecondary,
                                        unselectedTextColor = TextSecondary
                                    ),
                                    modifier = Modifier.testTag("nav_item_recs")
                                )

                                NavigationBarItem(
                                    selected = currentTab == Tab.Playlists,
                                    onClick = { viewModel.selectTab(Tab.Playlists) },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentTab == Tab.Playlists) Icons.Filled.QueueMusic else Icons.Outlined.QueueMusic,
                                            contentDescription = "Playlists"
                                        )
                                    },
                                    label = { Text("Playlists", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = TubeRed,
                                        selectedTextColor = TubeRed,
                                        indicatorColor = LightSurface,
                                        unselectedIconColor = TextSecondary,
                                        unselectedTextColor = TextSecondary
                                    ),
                                    modifier = Modifier.testTag("nav_item_playlists")
                                )

                                NavigationBarItem(
                                    selected = currentTab == Tab.Offline,
                                    onClick = { viewModel.selectTab(Tab.Offline) },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentTab == Tab.Offline) Icons.Filled.DownloadForOffline else Icons.Outlined.OfflinePin,
                                            contentDescription = "Offline"
                                        )
                                    },
                                    label = { Text("Offline", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = TubeRed,
                                        selectedTextColor = TubeRed,
                                        indicatorColor = LightSurface,
                                        unselectedIconColor = TextSecondary,
                                        unselectedTextColor = TextSecondary
                                    ),
                                    modifier = Modifier.testTag("nav_item_offline")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(OledBackground)
                    ) {
                        // --- CROSS NAVIGATION SCREENS DECK ---
                        when (currentTab) {
                            Tab.Browse -> {
                                YouTubeWebView(
                                    onVideoDetected = { videoId, detectedTitle ->
                                        // Instantly start inside background-enabled native player
                                        viewModel.startPlayback(videoId, detectedTitle, "YouTube Browser Stream")
                                        viewModel.selectTab(Tab.Player)
                                    },
                                    onAdBlocked = {
                                        viewModel.incrementBlockedAds()
                                    },
                                    isAdBlockingActive = adBlockingActive
                                )
                            }

                            Tab.Player -> {
                                val dlProgress = activeDownloads[activeVideoId]
                                NativePlayer(
                                    videoId = activeVideoId,
                                    title = activeTitle,
                                    channel = activeChannel,
                                    isPlaying = isPlaying,
                                    progress = progress,
                                    timeFormatted = timeFormatted,
                                    audioQuality = audioQuality,
                                    subtitlesEnabled = subtitlesEnabled,
                                    subtitleLanguage = subtitleLanguage,
                                    subtitleText = subtitleText,
                                    isMusicMode = isMusicMode,
                                    onTogglePlay = { viewModel.togglePlayback() },
                                    onSeek = { viewModel.seekTo(it) },
                                    onSetQuality = { viewModel.setAudioQuality(it) },
                                    onSetLanguage = { viewModel.setSubtitleLanguage(it) },
                                    onToggleSubtitles = { viewModel.toggleSubtitles() },
                                    onDownload = {
                                        viewModel.downloadVideoForOffline(activeVideoId, activeTitle, activeChannel, isMusicMode)
                                    },
                                    downloadProgress = dlProgress,
                                    isOfflineMode = offlineSimulated,
                                    onShare = {}
                                )
                            }

                            Tab.Recommendations -> {
                                StatsDashboard(
                                    historyList = historyList,
                                    recommendations = recommendations,
                                    isLoadingRecommendations = isLoadingRecommendations,
                                    blockedAdsCount = blockedAdsCount,
                                    offlineSimulated = offlineSimulated,
                                    syncStatus = syncStatus,
                                    isSyncing = isSyncing,
                                    adBlockingActive = adBlockingActive,
                                    onToggleAdBlock = { viewModel.toggleAdBlocking() },
                                    onToggleOfflineSimulated = { viewModel.toggleOfflineSimulated() },
                                    onSyncNow = { viewModel.syncDevicesNow() },
                                    onTriggerRecommendations = { viewModel.triggerAiRecommendations() },
                                    onSelectVideo = { vidId, title, creator, isMusic ->
                                        // Add bookmark to playlist if desired
                                        if (playlists.isNotEmpty() && !offlineSimulated) {
                                            // Auto sync download or add to first playlist folder
                                            viewModel.addItemToPlaylist(playlists.first().id, vidId, title, creator, isMusic)
                                        }
                                        viewModel.startPlayback(vidId, title, creator, isMusic)
                                        viewModel.selectTab(Tab.Player)
                                    },
                                    onDeleteHistoryItem = { viewModel.deleteHistoryItem(it) },
                                    onClearHistory = { viewModel.clearAllHistory() }
                                )
                            }

                            Tab.Playlists -> {
                                PlaylistShelf(
                                    playlists = playlists,
                                    onCreatePlaylist = { name, desc ->
                                        viewModel.createPlaylist(name, desc)
                                    },
                                    onDeletePlaylist = { viewModel.deletePlaylist(it) },
                                    getItemsFlow = { viewModel.getItemsForPlaylist(it) },
                                    onRemovePlaylistItem = { viewModel.removePlaylistItem(it) },
                                    onSelectTrack = { vidId, title, creator, isMusic ->
                                        viewModel.startPlayback(vidId, title, creator, isMusic)
                                        viewModel.selectTab(Tab.Player)
                                    }
                                )
                            }

                            Tab.Offline -> {
                                OfflineShelf(
                                    offlineList = offlineVideosList,
                                    isOfflineModeActive = offlineSimulated,
                                    onToggleOfflineSandbox = { viewModel.toggleOfflineSimulated() },
                                    onPlayOfflineTrack = { vidId, title, creator, isMusic ->
                                        viewModel.startPlayback(vidId, title, creator, isMusic)
                                        viewModel.selectTab(Tab.Player)
                                    },
                                    onDeleteOfflineMedia = { viewModel.removeOfflineVideo(it) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // --- PIP (PICTURE IN PICTURE) HANDLER ---
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (viewModel.isPlaying.value) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val aspectRatio = Rational(16, 9)
                    val params = PictureInPictureParams.Builder()
                        .setAspectRatio(aspectRatio)
                        .build()
                    enterPictureInPictureMode(params)
                }
            } catch (_: Exception) {}
        }
    }
}
