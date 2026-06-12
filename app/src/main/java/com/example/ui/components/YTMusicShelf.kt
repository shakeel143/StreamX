package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.Recommendation
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YTMusicShelf(
    recommendations: List<Recommendation>,
    activeVideoId: String,
    isPlaying: Boolean,
    audioQuality: String,
    onSelectMusicTrack: (videoId: String, title: String, channel: String, isMusic: Boolean) -> Unit,
    onAdBlocked: () -> Unit,
    adBlockingActive: Boolean
) {
    val context = LocalContext.current
    var isWebMusicOpen by remember { mutableStateOf(false) }

    // Curated YT Music quick listening mixes
    val ytMusicSeedTracks = remember(recommendations) {
        recommendations.filter { it.isMusic }
    }

    if (isWebMusicOpen) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(OledBackground)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { isWebMusicOpen = false },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back to Music Hub",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "YT Music Portal",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Extract backgrounds and playlists seamlessly",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }
            }

            YouTubeWebView(
                isMusicMode = true,
                onVideoDetected = { videoId, title ->
                    onSelectMusicTrack(videoId, title, "YT Music Portal", true)
                },
                onAdBlocked = onAdBlocked,
                isAdBlockingActive = adBlockingActive
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(OledBackground)
                .padding(horizontal = 16.dp)
                .testTag("yt_music_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // 1. BRAND YT MUSIC BANNER CARD
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFFF1A1A).copy(alpha = 0.15f), DarkSurface)
                            )
                        )
                        .border(1.dp, TubeRed.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = TubeRed,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "YouTube Music Engine",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Toggle active tracks and extract rich audio streams straight into your devices in real-time.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            // 2. YT MUSIC WEB SHELF LAUNCHER CARD
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF2E1C2B), Color(0xFF130F13))
                            )
                        )
                        .border(1.dp, BorderWhiteSmall, RoundedCornerShape(12.dp))
                        .clickable { isWebMusicOpen = true }
                        .padding(14.dp)
                        .testTag("launch_yt_music_portal"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Album,
                        contentDescription = "Music Portal",
                        tint = TubeRed,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Browse YT Music Portal",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Search songs, albums, artists and play in ambient mode.",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowForwardIos,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // 3. CURATED QUICK RELEASES SHELF
            item {
                Column {
                    Text(
                        text = "Curated Fast Mixes",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(listOf(
                            Triple("Sleep Lofi Mix", "jfKfPfyJRdk", "Lofi Studio"),
                            Triple("Classic Chill Piano", "5qap5aO4i9A", "Jazz Cafe"),
                            Triple("Study Acoustic Guitar", "tNkDsD1GYKY", "Instrumental Club"),
                            Triple("Ambient Synth Oasis", "E1w0_bUInXY", "Frequency Lab")
                        )) { quickTrack ->
                            val isCurrent = activeVideoId == quickTrack.second && isPlaying
                            Column(
                                modifier = Modifier
                                    .width(130.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DarkSurface)
                                    .border(
                                        1.dp,
                                        if (isCurrent) TubeRed.copy(0.4f) else BorderWhiteSmall,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        onSelectMusicTrack(quickTrack.second, quickTrack.first, quickTrack.third, true)
                                    }
                                    .padding(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(72.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(LightSurface, Color.Black)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LibraryMusic,
                                        contentDescription = null,
                                        tint = if (isCurrent) TubeRed else TubeAmber,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = quickTrack.first,
                                    color = if (isCurrent) TubeRed else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    text = quickTrack.third,
                                    color = TextSecondary,
                                    fontSize = 9.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // 4. MUSIC STREAMING ENTITY MATRIX
            item {
                Text(
                    text = "Personalized Audio Streams",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            if (ytMusicSeedTracks.isEmpty()) {
                item {
                    Text("No offline music entities found.", color = TextMuted, fontSize = 11.sp)
                }
            } else {
                items(ytMusicSeedTracks) { item ->
                    val isCurrent = item.videoId == activeVideoId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurface)
                            .border(
                                1.dp,
                                if (isCurrent) TubeRed.copy(alpha = 0.4f) else BorderWhiteSmall,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                onSelectMusicTrack(item.videoId, item.title, item.channelTitle, true)
                            }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(LightSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = if (isCurrent && isPlaying) TubeRed else Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                color = if (isCurrent) TubeRed else Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = item.channelTitle,
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(TubeAmber.copy(0.12f))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = audioQuality.split(" ").firstOrNull() ?: "FLAC",
                                        color = TubeAmber,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = if (isCurrent) TubeRed else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}
