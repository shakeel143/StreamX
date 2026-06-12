package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.math.sin

@Composable
fun VideoScreen(
    isPlaying: Boolean,
    waveOffset: Float,
    isOfflineMode: Boolean,
    isMusicMode: Boolean,
    subtitlesEnabled: Boolean,
    subtitleText: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .border(1.dp, BorderWhiteNormal, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        // dynamic wavy animation when playing
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val midY = height / 2f

            if (isPlaying) {
                val strokeWidth = 3f
                val waveBrush = Brush.horizontalGradient(
                    colors = listOf(TubeRed, TubeAmber)
                )

                for (waveIndex in 0..2) {
                    val amplitude = (30f + waveIndex * 15f)
                    val freq = (0.005f + waveIndex * 0.003f)
                    val phase = waveOffset + (waveIndex * 1.5f)

                    var prevX = 0f
                    var prevY = midY

                    for (x in 0..width.toInt() step 5) {
                        val currX = x.toFloat()
                        val currY = midY + sin(currX * freq + phase) * amplitude

                        drawLine(
                            brush = waveBrush,
                            start = Offset(prevX, prevY),
                            end = Offset(currX, currY),
                            strokeWidth = strokeWidth - (waveIndex * 0.5f)
                        )
                        prevX = currX
                        prevY = currY
                    }
                }
            } else {
                drawLine(
                    color = TextMuted,
                    start = Offset(0f, midY),
                    end = Offset(width, midY),
                    strokeWidth = 2f
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart)
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isOfflineMode) TubeAmber.copy(alpha = 0.2f) else TubeRed.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isOfflineMode) "LOCALLY BUFFERED" else "AD-FREE REST-STREAMED",
                    color = if (isOfflineMode) TubeAmber else TubeRed,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (isMusicMode) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "Music Playing",
                    tint = TubeRed,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Cinematic subtitle display
        AnimatedVisibility(
            visible = subtitlesEnabled && subtitleText.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp, start = 12.dp, end = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.85f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = subtitleText,
                    color = Color.Yellow,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.SansSerif,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NativePlayer(
    videoId: String,
    title: String,
    channel: String,
    isPlaying: Boolean,
    progress: Float,
    timeFormatted: String,
    audioQuality: String,
    subtitlesEnabled: Boolean,
    subtitleLanguage: String,
    subtitleText: String,
    isMusicMode: Boolean,
    onTogglePlay: () -> Unit,
    onSeek: (Float) -> Unit,
    onSetQuality: (String) -> Unit,
    onSetLanguage: (String) -> Unit,
    onToggleSubtitles: () -> Unit,
    onDownload: () -> Unit,
    downloadProgress: Float?, // null if not downloading, otherwise 0f..1f
    isOfflineMode: Boolean,
    onShare: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    var qualityMenuExpanded by remember { mutableStateOf(false) }
    var subtitleMenuExpanded by remember { mutableStateOf(false) }

    var feedbackText by remember { mutableStateOf("") }
    var shareDialogOpen by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "Visualizer")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "WavePhase"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OledBackground)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // --- TOP QUALITY METRICS HUD HEADER ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(50))
                            .background(if (isPlaying) Color.Green else Color.DarkGray)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isOfflineMode) "Pure Offline Master Playback" else "HQ Stream: Bitrate Verified",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Quick Quality Selector Pill
            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(LightSurface)
                        .clickable { qualityMenuExpanded = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("quality_selector"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Hearing,
                        contentDescription = "Audio Quality",
                        tint = TubeAmber,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = audioQuality, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                DropdownMenu(
                    expanded = qualityMenuExpanded,
                    onDismissRequest = { qualityMenuExpanded = false },
                    modifier = Modifier.background(DarkSurface)
                ) {
                    val qualities = listOf(
                        "Hi-Res FLAC (24-bit/96kHz)",
                        "Studio Master (320kbps AAC)",
                        "Standard Audio (192kbps MP3)",
                        "Eco Stream (128kbps AAC)"
                    )
                    qualities.forEach { q ->
                        DropdownMenuItem(
                            text = { Text(q, color = Color.White, fontSize = 12.sp) },
                            onClick = {
                                onSetQuality(q)
                                qualityMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Dedicated Video Display Screen
        VideoScreen(
            isPlaying = isPlaying,
            waveOffset = waveOffset,
            isOfflineMode = isOfflineMode,
            isMusicMode = isMusicMode,
            subtitlesEnabled = subtitlesEnabled,
            subtitleText = subtitleText
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- VIDEO METADATA BLOCK ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = channel,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    maxLines = 1
                )
            }

            // Download actions button
            Box {
                if (downloadProgress != null) {
                    CircularProgressIndicator(
                        progress = downloadProgress,
                        color = TubeRed,
                        modifier = Modifier.size(40.dp),
                        strokeWidth = 3.dp
                    )
                } else {
                    IconButton(
                        onClick = onDownload,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(50))
                            .background(LightSurface)
                            .testTag("download_video_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download Offline",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- INTERACTIVE SLIDER SEEK BAR ---
        Column(modifier = Modifier.fillMaxWidth()) {
            Slider(
                value = progress,
                onValueChange = onSeek,
                colors = SliderDefaults.colors(
                    activeTrackColor = TubeRed,
                    inactiveTrackColor = LightSurface,
                    thumbColor = TubeRed
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("playback_progress_slider")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = timeFormatted, color = TextSecondary, fontSize = 11.sp)
                Text(text = "04:00", color = TextSecondary, fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- DECK OF PRIMARY NAVIGATION MEDIA PLAYS CONTROLS ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    Toast.makeText(context, "Entering Picture-In-Picture View", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.testTag("pip_mode_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PictureInPicture,
                    contentDescription = "Picture-In-Picture",
                    tint = Color.White
                )
            }

            IconButton(onClick = { onSeek(0f) }) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Rewind",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            IconButton(
                onClick = onTogglePlay,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(TubeRed, TubeAmber)
                        )
                    )
                    .testTag("media_play_pause_button")
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            IconButton(onClick = { onSeek(0.99f) }) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Fast Forward",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            IconButton(
                onClick = { shareDialogOpen = true },
                modifier = Modifier.testTag("share_video_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share Creator Card",
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- SUBTITLES LANGUAGE SELECT PILLS ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onToggleSubtitles,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (subtitlesEnabled) TubeRed.copy(alpha = 0.2f) else DarkSurface)
                        .testTag("toggle_subtitles_button")
                ) {
                    Icon(
                        imageVector = if (subtitlesEnabled) Icons.Default.Subtitles else Icons.Default.SubtitlesOff,
                        contentDescription = "Toggle Subtitles",
                        tint = if (subtitlesEnabled) TubeRed else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Captions", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            // Subtitle Language selection
            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(LightSurface)
                        .clickable { subtitleMenuExpanded = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("language_selector"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Subtitle Language",
                        tint = GlowBlue,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = subtitleLanguage, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                DropdownMenu(
                    expanded = subtitleMenuExpanded,
                    onDismissRequest = { subtitleMenuExpanded = false },
                    modifier = Modifier.background(DarkSurface)
                ) {
                    val langs = listOf("English", "Spanish")
                    langs.forEach { l ->
                        DropdownMenuItem(
                            text = { Text(l, color = Color.White, fontSize = 12.sp) },
                            onClick = {
                                onSetLanguage(l)
                                subtitleMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Divider(color = BorderWhiteSmall, thickness = 1.dp)
        Spacer(modifier = Modifier.height(12.dp))

        // --- DIRECT COMMENT FEEDBACK FOR SOCIAL ENGAGEMENT ---
        Text(
            text = "Your Private Sync Journal",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = feedbackText,
                onValueChange = { feedbackText = it },
                placeholder = { Text("Tape notes about this creator...", color = TextMuted, fontSize = 12.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = TubeRed,
                    unfocusedBorderColor = BorderWhiteNormal,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("private_sync_input"),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (feedbackText.trim().isNotEmpty()) {
                        Toast.makeText(context, "Comment synced to local creator card!", Toast.LENGTH_SHORT).show()
                        feedbackText = ""
                    }
                })
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (feedbackText.trim().isNotEmpty()) {
                        Toast.makeText(context, "Comment synced to local creator card!", Toast.LENGTH_SHORT).show()
                        feedbackText = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = TubeRed),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("sync_notes_button")
            ) {
                Text("Sync", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    // Dynamic sharing dialogue overlay
    if (shareDialogOpen) {
        AlertDialog(
            onDismissRequest = { shareDialogOpen = false },
            containerColor = DarkSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = TubeRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share Creator Social Card", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Generate ad-free streaming link for your friends to bypass video ads automatically!",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black)
                            .border(1.dp, LightSurface, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "🎬 Watch \"$title\" by \"$channel\" ad-free! Use TubeCompanion at time tag $timeFormatted: https://tubecompanion.aistudio/stream?id=$videoId&t=$timeFormatted",
                            color = TubeAmber,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val shareMessage = "🎬 Watch \"$title\" by \"$channel\" ad-free! Use TubeCompanion at time tag $timeFormatted: https://tubecompanion.aistudio/stream?id=$videoId&t=$timeFormatted"
                        clipboard.setText(AnnotatedString(shareMessage))
                        Toast.makeText(context, "Ad-Free Card copied to Clipboard!", Toast.LENGTH_SHORT).show()
                        shareDialogOpen = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TubeRed)
                ) {
                    Text("Copy Share Code", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { shareDialogOpen = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}
