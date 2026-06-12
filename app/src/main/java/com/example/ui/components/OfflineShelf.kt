package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.OfflineVideoEntity
import com.example.ui.theme.*

@Composable
fun OfflineShelf(
    offlineList: List<OfflineVideoEntity>,
    isOfflineModeActive: Boolean,
    onToggleOfflineSandbox: () -> Unit,
    onPlayOfflineTrack: (videoId: String, title: String, channel: String, isMusic: Boolean) -> Unit,
    onDeleteOfflineMedia: (String) -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(OledBackground)
            .padding(16.dp)
            .testTag("offline_shelf_screen")
    ) {
        // --- TOP OFFLINE CONTROLS BANNER HEADER ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isOfflineModeActive) TubeAmber.copy(alpha = 0.15f) else DarkSurface)
                    .border(1.dp, if (isOfflineModeActive) TubeAmber else BorderWhiteNormal, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isOfflineModeActive) TubeAmber.copy(alpha = 0.2f) else LightSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isOfflineModeActive) Icons.Default.AirplaneTicket else Icons.Default.DownloadForOffline,
                                contentDescription = null,
                                tint = if (isOfflineModeActive) TubeAmber else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Offline Viewing Sandbox",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isOfflineModeActive) "Network Cut: Pure Local Playing" else "Online: Feeds streaming naturally",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        Switch(
                            checked = isOfflineModeActive,
                            onCheckedChange = { onToggleOfflineSandbox() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PureWhite,
                                checkedTrackColor = TubeAmber,
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = LightSurface
                            ),
                            modifier = Modifier.testTag("offline_sandbox_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Toggle Offline view to test watching and listening to your downloaded tracks fully disconnected from cellular data streams.",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        lineHeight = 13.sp
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // --- SUBTITLE HEADER ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Downloaded Local Cache (${offlineList.size})",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Secure local duplicates with synchronized subtitle engines",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // --- EMPTY STATE IF NO DOWNLOADED CACHE ---
        if (offlineList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.OfflinePin,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Offline shelf is bare",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "To download media, browse video nodes or play lists, click the download button inside the main video Player tab.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            }
        } else {
            // --- OFFLINE CARDS MATRIX LIST ---
            items(offlineList) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface)
                        .border(1.dp, BorderWhiteSmall, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                        .clickable {
                            onPlayOfflineTrack(item.videoId, item.title, item.channelTitle, item.isMusic)
                        }
                        .testTag("offline_track_row"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Caching thumb background
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(LightSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (item.isMusic) Icons.Default.LibraryMusic else Icons.Default.VideoLibrary,
                            contentDescription = null,
                            tint = if (item.isMusic) TubeAmber else TubeRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = item.channelTitle, color = TextSecondary, fontSize = 10.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.Black)
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "SECURE CACHE",
                                    color = Color.Green,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Delete storage cache button
                    IconButton(
                        onClick = {
                            onDeleteOfflineMedia(item.videoId)
                            Toast.makeText(context, "Storage space purged!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("delete_cache_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderDelete,
                            contentDescription = "Delete Offline Cache",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
