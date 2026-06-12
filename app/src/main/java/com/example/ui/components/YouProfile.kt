package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.HistoryEntity
import com.example.data.database.OfflineVideoEntity
import com.example.data.database.PlaylistEntity
import com.example.data.database.PlaylistItemEntity
import com.example.ui.theme.*
import kotlinx.coroutines.flow.Flow

sealed interface YouSubSection {
    object Playlists : YouSubSection
    object Offline : YouSubSection
    object History : YouSubSection
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YouProfile(
    // 1. Playlists
    playlists: List<PlaylistEntity>,
    onCreatePlaylist: (String, String) -> Unit,
    onDeletePlaylist: (Int) -> Unit,
    getItemsFlow: (Int) -> Flow<List<PlaylistItemEntity>>,
    onRemovePlaylistItem: (Int) -> Unit,
    // 2. Offline Downloads
    offlineList: List<OfflineVideoEntity>,
    isOfflineModeActive: Boolean,
    onToggleOfflineSandbox: () -> Unit,
    onDeleteOfflineMedia: (String) -> Unit,
    // 3. History
    historyList: List<HistoryEntity>,
    onDeleteHistoryItem: (Int) -> Unit,
    onClearHistory: () -> Unit,
    // 4. Global Playback Trigger
    onPlayTrackNow: (videoId: String, title: String, channel: String, isMusic: Boolean) -> Unit,
    // 5. System settings
    blockedAdsCount: Int,
    adBlockingActive: Boolean,
    onToggleAdBlock: () -> Unit,
    syncStatus: String,
    isSyncing: Boolean,
    onSyncNow: () -> Unit
) {
    val context = LocalContext.current
    var activeSubSection by remember { mutableStateOf<YouSubSection>(YouSubSection.Offline) } // Default to Offline inside You as requested

    // Playlist states
    var isCreatingByDialog by remember { mutableStateOf(false) }
    var playlistTitle by remember { mutableStateOf("") }
    var playlistDescription by remember { mutableStateOf("") }
    var selectedPlaylistId by remember { mutableStateOf<Int?>(null) }

    val activePlaylistItems by remember(selectedPlaylistId) {
        derivedStateOf {
            selectedPlaylistId?.let { getItemsFlow(it) }
        }
    }
    val itemsListState = activePlaylistItems?.collectAsState(initial = emptyList())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(OledBackground)
            .padding(horizontal = 16.dp)
            .testTag("you_profile_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(6.dp)) }

        // 1. GLOWING USER AVATAR CARD
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
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
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(colors = listOf(TubeRed, Color.Transparent)))
                            .border(1.5.dp, TubeRed, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "JD",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "John Doe",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(TubeRed.copy(0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "PREMIUM MEMBER",
                                    color = TubeRed,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "ID: 98142",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onSyncNow,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(LightSurface)
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                color = TubeRed,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Sync",
                                tint = Color.Green,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. QUICK ADVANCED CONTROLS STRIP (ADBLOCKING, NET SANDBOX, SYNC)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurface)
                    .border(1.dp, BorderWhiteNormal, RoundedCornerShape(16.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "StreamX Parameters Settings",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                // Row A: Adblocking setting
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = TubeRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Ad-Block Secure Filter", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("Blocked Ads Count: $blockedAdsCount files", color = TextSecondary, fontSize = 9.sp)
                    }
                    Switch(
                        checked = adBlockingActive,
                        onCheckedChange = { onToggleAdBlock() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = TubeRed,
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = LightSurface
                        ),
                        modifier = Modifier
                            .scale(0.8f)
                            .testTag("you_ad_block_switch")
                    )
                }

                Divider(color = LightSurface, thickness = 0.5.dp)

                // Row B: Simulated network disconnect
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AirplaneTicket,
                        contentDescription = null,
                        tint = TubeAmber,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Simulate Offline Mode", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (isOfflineModeActive) "Offline sandbox enabled" else "Online network enabled",
                            color = TextSecondary,
                            fontSize = 9.sp
                        )
                    }
                    Switch(
                        checked = isOfflineModeActive,
                        onCheckedChange = { onToggleOfflineSandbox() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = TubeAmber,
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = LightSurface
                        ),
                        modifier = Modifier
                            .scale(0.8f)
                            .testTag("you_offline_sandbox_switch")
                    )
                }
            }
        }

        // 3. INTERNAL TAB NAVIGATION PILLS (Playlists vs Offline vs History)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Choice A: Offline
                val isOffSelected = activeSubSection == YouSubSection.Offline
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isOffSelected) TubeRed else DarkSurface)
                        .border(1.dp, if (isOffSelected) TubeRed else BorderWhiteSmall, RoundedCornerShape(10.dp))
                        .clickable { activeSubSection = YouSubSection.Offline }
                        .padding(vertical = 10.dp)
                        .testTag("you_sub_offline"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DownloadForOffline,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Text("Offline (${offlineList.size})", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Choice B: Playlists
                val isPlaySelected = activeSubSection == YouSubSection.Playlists
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isPlaySelected) TubeRed else DarkSurface)
                        .border(1.dp, if (isPlaySelected) TubeRed else BorderWhiteSmall, RoundedCornerShape(10.dp))
                        .clickable { activeSubSection = YouSubSection.Playlists }
                        .padding(vertical = 10.dp)
                        .testTag("you_sub_playlists"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QueueMusic,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Text("Playlists (${playlists.size})", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Choice C: History
                val isHistSelected = activeSubSection == YouSubSection.History
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isHistSelected) TubeRed else DarkSurface)
                        .border(1.dp, if (isHistSelected) TubeRed else BorderWhiteSmall, RoundedCornerShape(10.dp))
                        .clickable { activeSubSection = YouSubSection.History }
                        .padding(vertical = 10.dp)
                        .testTag("you_sub_history"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Text("History (${historyList.size})", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 4. RENDERING SUB-DECK STATES
        when (activeSubSection) {
            YouSubSection.Offline -> {
                // RENDERING OFFLINE DOWNLOAD CARD LIST
                if (offlineList.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            border = BorderStroke(1.dp, BorderWhiteSmall)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.OfflinePin, contentDescription = null, tint = TextMuted, modifier = Modifier.size(40.dp))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Offline shelf is blank", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("To cache tracks offline click the Download button on any playing video in the Home browser.", color = TextSecondary, fontSize = 10.sp, textAlign = TextAlign.Center)
                            }
                        }
                    }
                } else {
                    items(offlineList) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurface)
                                .border(1.dp, BorderWhiteSmall, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                                .clickable {
                                    onPlayTrackNow(item.videoId, item.title, item.channelTitle, item.isMusic)
                                }
                                .testTag("you_offline_row"),
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
                                    imageVector = if (item.isMusic) Icons.Default.LibraryMusic else Icons.Default.VideoLibrary,
                                    contentDescription = null,
                                    tint = if (item.isMusic) TubeAmber else TubeRed,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                Text(item.channelTitle, color = TextSecondary, fontSize = 10.sp)
                            }
                            IconButton(onClick = { onDeleteOfflineMedia(item.videoId) }) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Purge Cache", tint = TextMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            YouSubSection.Playlists -> {
                // RENDERING PLAYLISTS FOLDER MANAGER
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Create and organize playlist folders", color = TextSecondary, fontSize = 11.sp)
                        Button(
                            onClick = { isCreatingByDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = TubeRed),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("New", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (playlists.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            border = BorderStroke(1.dp, BorderWhiteSmall)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.QueueMusic, contentDescription = null, tint = TextMuted, modifier = Modifier.size(40.dp))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("No playlist folders", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("Assemble folders to bundle tracks and listen seamlessly without constraints.", color = TextSecondary, fontSize = 10.sp, textAlign = TextAlign.Center)
                            }
                        }
                    }
                } else {
                    // Playlists Selector horizontal Chips
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            playlists.forEach { pl ->
                                val isSelected = selectedPlaylistId == pl.id
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) TubeRed.copy(0.15f) else DarkSurface)
                                        .border(1.dp, if (isSelected) TubeRed else BorderWhiteSmall, RoundedCornerShape(8.dp))
                                        .clickable { selectedPlaylistId = if (isSelected) null else pl.id }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(pl.name, color = if (isSelected) TubeRed else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    selectedPlaylistId?.let { plId ->
                        val activeFolder = playlists.find { it.id == plId }
                        activeFolder?.let { folder ->
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Folder: ${folder.name}", color = TubeAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        if (folder.description.isNotEmpty()) {
                                            Text(folder.description, color = TextSecondary, fontSize = 10.sp)
                                        }
                                    }
                                    IconButton(onClick = {
                                        onDeletePlaylist(plId)
                                        selectedPlaylistId = null
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TubeRed, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            val itemsList = itemsListState?.value ?: emptyList()
                            if (itemsList.isEmpty()) {
                                item {
                                    Text("This playlist has no items yet.", color = TextMuted, fontSize = 10.sp, modifier = Modifier.padding(vertical = 12.dp))
                                }
                            } else {
                                items(itemsList) { track ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(DarkSurface)
                                            .border(1.dp, BorderWhiteSmall, RoundedCornerShape(10.dp))
                                            .padding(10.dp)
                                            .clickable {
                                                onPlayTrackNow(track.videoId, track.title, track.channelTitle, track.isMusic)
                                            }
                                    ) {
                                        Icon(
                                            imageVector = if (track.isMusic) Icons.Default.MusicNote else Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = if (track.isMusic) TubeAmber else TubeRed,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(track.title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, modifier = Modifier.weight(1f))
                                        IconButton(
                                            onClick = { onRemovePlaylistItem(track.id) },
                                            modifier = Modifier.size(18.dp)
                                        ) {
                                            Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Remove", tint = TextMuted, modifier = Modifier.size(12.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            YouSubSection.History -> {
                // RENDERING WATCH HISTORIES FEED
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Past played sessions logs", color = TextSecondary, fontSize = 11.sp)
                        if (historyList.isNotEmpty()) {
                            TextButton(
                                onClick = onClearHistory,
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("Clear All", color = TubeRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                if (historyList.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            border = BorderStroke(1.dp, BorderWhiteSmall)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.History, contentDescription = null, tint = TextMuted, modifier = Modifier.size(40.dp))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("No playback history yet", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("Stream sounds or video links in YouTube browser to record histories.", color = TextSecondary, fontSize = 10.sp, textAlign = TextAlign.Center)
                            }
                        }
                    }
                } else {
                    items(historyList) { element ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkSurface)
                                .border(1.dp, BorderWhiteSmall, RoundedCornerShape(10.dp))
                                .padding(10.dp)
                                .clickable {
                                    onPlayTrackNow(element.videoId, element.title, element.channelTitle, element.category == "Music")
                                }
                        ) {
                            Icon(Icons.Default.HistoryToggleOff, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(element.title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                Text(element.channelTitle, color = TextSecondary, fontSize = 9.sp)
                            }
                            IconButton(
                                onClick = { onDeleteHistoryItem(element.id) },
                                modifier = Modifier.size(18.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Delete Item", tint = TextMuted, modifier = Modifier.size(12.dp))
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    // Modal dialog to build playlist inside You Screen
    if (isCreatingByDialog) {
        AlertDialog(
            onDismissRequest = { isCreatingByDialog = false },
            containerColor = DarkSurface,
            title = {
                Text("Create Playlist Folder", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = playlistTitle,
                        onValueChange = { playlistTitle = it },
                        label = { Text("Folder Name (e.g. Chill Beats)", color = TextSecondary, fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = TubeRed,
                            unfocusedBorderColor = BorderWhiteNormal
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .testTag("you_playlist_title_input")
                    )

                    OutlinedTextField(
                        value = playlistDescription,
                        onValueChange = { playlistDescription = it },
                        label = { Text("Description (Optional)", color = TextSecondary, fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = TubeRed,
                            unfocusedBorderColor = BorderWhiteNormal
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("you_playlist_desc_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (playlistTitle.trim().isNotEmpty()) {
                            onCreatePlaylist(playlistTitle.trim(), playlistDescription.trim())
                            playlistTitle = ""
                            playlistDescription = ""
                            isCreatingByDialog = false
                            Toast.makeText(context, "Playlist created inside You profile!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TubeRed)
                ) {
                    Text("Create Folder", color = Color.White, fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { isCreatingByDialog = false }) {
                    Text("Cancel", color = TextSecondary, fontSize = 11.sp)
                }
            }
        )
    }
}
