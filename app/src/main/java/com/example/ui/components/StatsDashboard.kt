package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.HistoryEntity
import com.example.data.repository.Recommendation
import com.example.ui.theme.*

@Composable
fun StatsDashboard(
    historyList: List<HistoryEntity>,
    recommendations: List<Recommendation>,
    isLoadingRecommendations: Boolean,
    blockedAdsCount: Int,
    offlineSimulated: Boolean,
    syncStatus: String,
    isSyncing: Boolean,
    adBlockingActive: Boolean,
    onToggleAdBlock: () -> Unit,
    onToggleOfflineSimulated: () -> Unit,
    onSyncNow: () -> Unit,
    onTriggerRecommendations: () -> Unit,
    onSelectVideo: (videoId: String, title: String, channel: String, isMusic: Boolean) -> Unit,
    onDeleteHistoryItem: (Int) -> Unit,
    onClearHistory: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(OledBackground)
            .padding(16.dp)
            .testTag("stats_dashboard_screen")
    ) {
        // --- BRAND LOGO BANNER ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .border(1.dp, BorderWhiteSmall, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.img_streamx_logo_1781246041009),
                        contentDescription = "StreamX Logo",
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "StreamX Companion",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Elegant Media Control Suite",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "v2.0 • Adblock Security Active",
                            color = TubeRed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // --- AD SHIELD HUD SECTION ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(DarkSurface, OledBackground)
                        )
                    )
                    .border(1.dp, BorderWhiteNormal, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Shield Icon
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (adBlockingActive) TubeRed.copy(alpha = 0.15f) else Color.DarkGray)
                            .border(1.dp, if (adBlockingActive) TubeRed else BorderWhiteNormal, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (adBlockingActive) Icons.Default.Security else Icons.Default.SecurityUpdateWarning,
                            contentDescription = "Adblock Shield",
                            tint = if (adBlockingActive) TubeRed else Color.LightGray,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (adBlockingActive) "Ad-Shield: Active" else "Ad-Shield: Paused",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "$blockedAdsCount",
                                color = TubeRed,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ads filtered total",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }

                    Switch(
                        checked = adBlockingActive,
                        onCheckedChange = { onToggleAdBlock() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PureWhite,
                            checkedTrackColor = TubeRed,
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = LightSurface
                        ),
                        modifier = Modifier.testTag("adblock_switch")
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // --- SYSTEM OPTIONS: SYNC & OFFLINE HUB ---
        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                // Cross Device Sync Tile
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, BorderWhiteSmall, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                tint = GlowBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(Color.Green)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Device Sync", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(syncStatus, color = TextSecondary, fontSize = 10.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(LightSurface)
                                .clickable { onSyncNow() }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(color = GlowBlue, modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp)
                            } else {
                                Text("Sync Devices", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Network Simulator card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, BorderWhiteSmall, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = if (offlineSimulated) Icons.Default.WifiOff else Icons.Default.Wifi,
                                contentDescription = null,
                                tint = TubeAmber,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Offline Sandbox", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (offlineSimulated) "Internet Offline Mode" else "Network Online Mode",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (offlineSimulated) TubeAmber.copy(alpha = 0.2f) else LightSurface)
                                .clickable { onToggleOfflineSimulated() }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (offlineSimulated) "Restore Online" else "Simulate Offline",
                                color = if (offlineSimulated) TubeAmber else Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // --- GEMINI PERSONALIZED RECOMMENDATIONS FEED ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Personalized recommendation feed",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = onTriggerRecommendations,
                    modifier = Modifier.testTag("regenerate_recommendations_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Regenerate Recommendations",
                        tint = TubeRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (isLoadingRecommendations) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = TubeRed, modifier = Modifier.size(28.dp), strokeWidth = 2.5.dp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Gemini generating taste-profile content...", color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }
        } else {
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("recommendations_row"),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(recommendations) { rec ->
                        Card(
                            modifier = Modifier
                                .width(220.dp)
                                .border(1.dp, BorderWhiteSmall, RoundedCornerShape(12.dp))
                                .clickable {
                                    onSelectVideo(rec.videoId, rec.title, rec.channelTitle, rec.isMusic)
                                }
                                .testTag("recommendation_card"),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                // Miniature thumbnail visual background
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(85.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(LightSurface, Color.Black)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (rec.isMusic) Icons.Default.MusicNote else Icons.Default.PlayCircleFilled,
                                        contentDescription = null,
                                        tint = if (rec.isMusic) TubeAmber else TubeRed,
                                        modifier = Modifier.size(24.dp)
                                    )

                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(4.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color.Black.copy(alpha = 0.8f))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(text = rec.duration, color = Color.White, fontSize = 9.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = rec.title,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    text = rec.channelTitle,
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // AI Reason tag
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.Black)
                                        .padding(6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = TubeRed,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = rec.reason,
                                            color = TubeAmber,
                                            fontSize = 9.sp,
                                            maxLines = 1,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // --- HISTORY VIEWING TIMELINE LOGS ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Viewing History Log",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                if (historyList.isNotEmpty()) {
                    TextButton(onClick = onClearHistory) {
                        Text("Clear All", color = TubeRed, fontSize = 12.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (historyList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No play history detected yet.",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Watch content under Browse or search to populate history logs.",
                            color = TextMuted,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            }
        } else {
            items(historyList) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface)
                        .border(1.dp, BorderWhiteSmall, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                        .clickable {
                            onSelectVideo(item.videoId, item.title, item.channelTitle, item.category == "Music")
                        }
                        .testTag("history_item_row"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (item.category == "Music") Icons.Default.MusicNote else Icons.Default.PlayCircle,
                        contentDescription = null,
                        tint = if (item.category == "Music") TubeAmber else TubeRed,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Row {
                            Text(text = item.channelTitle, color = TextSecondary, fontSize = 10.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "•  ${item.category}", color = GlowBlue, fontSize = 10.sp)
                        }
                    }

                    IconButton(onClick = { onDeleteHistoryItem(item.id) }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
