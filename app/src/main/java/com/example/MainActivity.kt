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
                        if (currentTab != Tab.Browse) {
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
                            // Persistent Premium Floating Player Control Hub (visible from all screens)
                            if (activeVideoId.isNotEmpty()) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(1.dp, TubeRed.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                                    color = DarkSurface
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(
                                                    imageVector = if (isMusicMode) Icons.Default.MusicNote else Icons.Default.PlayCircle,
                                                    contentDescription = null,
                                                    tint = TubeRed,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Column {
                                                    Text(
                                                        text = activeTitle,
                                                        color = Color.White,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        maxLines = 1
                                                    )
                                                    Text(
                                                        text = activeChannel,
                                                        color = TextSecondary,
                                                        fontSize = 10.sp,
                                                        maxLines = 1
                                                    )
                                                }
                                            }
                                            
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                // Toggle Subtitles
                                                IconButton(
                                                    onClick = { viewModel.toggleSubtitles() },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (subtitlesEnabled) Icons.Default.ClosedCaption else Icons.Default.ClosedCaptionDisabled,
                                                        contentDescription = "Toggle Subtitles",
                                                        tint = if (subtitlesEnabled) TubeRed else Color.White,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }

                                                // Download
                                                val dlProgress = activeDownloads[activeVideoId]
                                                IconButton(
                                                    onClick = {
                                                        viewModel.downloadVideoForOffline(activeVideoId, activeTitle, activeChannel, isMusicMode)
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    if (dlProgress != null) {
                                                        CircularProgressIndicator(
                                                            progress = dlProgress,
                                                            strokeWidth = 2.dp,
                                                            color = TubeAmber,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    } else {
                                                        Icon(
                                                            imageVector = Icons.Default.DownloadForOffline,
                                                            contentDescription = "Download Video",
                                                            tint = Color.White,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }

                                                // Play / Pause
                                                IconButton(
                                                    onClick = { viewModel.togglePlayback() },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                        contentDescription = "Control",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                        }

                                        // Subtitles block
                                        if (subtitlesEnabled && subtitleText.isNotEmpty()) {
                                            Text(
                                                text = subtitleText,
                                                color = Color.Yellow,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Medium,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                            )
                                        }

                                        // Progress slider bar
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(text = timeFormatted, color = Color.White, fontSize = 9.sp)
                                            Slider(
                                                value = progress,
                                                onValueChange = { viewModel.seekTo(it) },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(horizontal = 4.dp)
                                                    .height(20.dp),
                                                colors = SliderDefaults.colors(
                                                    thumbColor = TubeRed,
                                                    activeTrackColor = TubeRed,
                                                    inactiveTrackColor = LightSurface
                                                )
                                            )
                                            Text(text = "04:00", color = TextSecondary, fontSize = 9.sp)
                                        }
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
                                            imageVector = if (currentTab == Tab.Browse) Icons.Filled.Home else Icons.Outlined.Home,
                                            contentDescription = "Home"
                                        )
                                    },
                                    label = { Text("Home", fontSize = 10.sp) },
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
                                    selected = currentTab == Tab.Shorts,
                                    onClick = { viewModel.selectTab(Tab.Shorts) },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentTab == Tab.Shorts) Icons.Filled.OfflineShare else Icons.Outlined.OfflineShare,
                                            contentDescription = "Shorts"
                                        )
                                    },
                                    label = { Text("Shorts", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = TubeRed,
                                        selectedTextColor = TubeRed,
                                        indicatorColor = LightSurface,
                                        unselectedIconColor = TextSecondary,
                                        unselectedTextColor = TextSecondary
                                    ),
                                    modifier = Modifier.testTag("nav_item_shorts")
                                )

                                NavigationBarItem(
                                    selected = currentTab == Tab.YTMusic,
                                    onClick = { viewModel.selectTab(Tab.YTMusic) },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentTab == Tab.YTMusic) Icons.Filled.MusicNote else Icons.Outlined.MusicNote,
                                            contentDescription = "YT Music"
                                        )
                                    },
                                    label = { Text("YT Music", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = TubeRed,
                                        selectedTextColor = TubeRed,
                                        indicatorColor = LightSurface,
                                        unselectedIconColor = TextSecondary,
                                        unselectedTextColor = TextSecondary
                                    ),
                                    modifier = Modifier.testTag("nav_item_ytmusic")
                                )

                                NavigationBarItem(
                                    selected = currentTab == Tab.You,
                                    onClick = { viewModel.selectTab(Tab.You) },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentTab == Tab.You) Icons.Filled.AccountCircle else Icons.Outlined.AccountCircle,
                                            contentDescription = "You"
                                        )
                                    },
                                    label = { Text("You", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = TubeRed,
                                        selectedTextColor = TubeRed,
                                        indicatorColor = LightSurface,
                                        unselectedIconColor = TextSecondary,
                                        unselectedTextColor = TextSecondary
                                    ),
                                    modifier = Modifier.testTag("nav_item_you")
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
                                    isMusicMode = false,
                                    onVideoDetected = { videoId, title ->
                                        viewModel.startPlayback(videoId, title, "YouTube Web Player", false)
                                    },
                                    onAdBlocked = {
                                        viewModel.incrementBlockedAds()
                                    },
                                    isAdBlockingActive = adBlockingActive
                                )
                            }

                            Tab.Shorts -> {
                                ShortsShelf(
                                    onSelectShortStream = { vidId, title, creator, isMusic ->
                                        viewModel.startPlayback(vidId, title, creator, isMusic)
                                    },
                                    activeVideoId = activeVideoId,
                                    isPlaying = isPlaying,
                                    onTogglePlay = { viewModel.togglePlayback() }
                                )
                            }

                            Tab.YTMusic -> {
                                YTMusicShelf(
                                    recommendations = recommendations,
                                    activeVideoId = activeVideoId,
                                    isPlaying = isPlaying,
                                    audioQuality = audioQuality,
                                    onSelectMusicTrack = { vidId, title, creator, isMusic ->
                                        viewModel.startPlayback(vidId, title, creator, isMusic)
                                    },
                                    onAdBlocked = {
                                        viewModel.incrementBlockedAds()
                                    },
                                    adBlockingActive = adBlockingActive
                                )
                            }

                            Tab.You -> {
                                YouProfile(
                                    playlists = playlists,
                                    onCreatePlaylist = { name, desc ->
                                        viewModel.createPlaylist(name, desc)
                                    },
                                    onDeletePlaylist = { viewModel.deletePlaylist(it) },
                                    getItemsFlow = { viewModel.getItemsForPlaylist(it) },
                                    onRemovePlaylistItem = { viewModel.removePlaylistItem(it) },
                                    offlineList = offlineVideosList,
                                    isOfflineModeActive = offlineSimulated,
                                    onToggleOfflineSandbox = { viewModel.toggleOfflineSimulated() },
                                    onDeleteOfflineMedia = { viewModel.removeOfflineVideo(it) },
                                    historyList = historyList,
                                    onDeleteHistoryItem = { viewModel.deleteHistoryItem(it) },
                                    onClearHistory = { viewModel.clearAllHistory() },
                                    onPlayTrackNow = { vidId, title, creator, isMusic ->
                                        viewModel.startPlayback(vidId, title, creator, isMusic)
                                    },
                                    blockedAdsCount = blockedAdsCount,
                                    adBlockingActive = adBlockingActive,
                                    onToggleAdBlock = { viewModel.toggleAdBlocking() },
                                    syncStatus = syncStatus,
                                    isSyncing = isSyncing,
                                    onSyncNow = { viewModel.syncDevicesNow() }
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
