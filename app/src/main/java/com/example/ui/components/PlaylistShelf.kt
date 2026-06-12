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
import com.example.data.database.PlaylistEntity
import com.example.data.database.PlaylistItemEntity
import com.example.ui.theme.*
import kotlinx.coroutines.flow.Flow

@Composable
fun PlaylistShelf(
    playlists: List<PlaylistEntity>,
    onCreatePlaylist: (String, String) -> Unit,
    onDeletePlaylist: (Int) -> Unit,
    getItemsFlow: (Int) -> Flow<List<PlaylistItemEntity>>,
    onRemovePlaylistItem: (Int) -> Unit,
    onSelectTrack: (videoId: String, title: String, channel: String, isMusic: Boolean) -> Unit
) {
    val context = LocalContext.current
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
            .padding(16.dp)
            .testTag("playlists_screen")
    ) {
        // --- ADD NEW PLAYLIST PANEL HEADER ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Your Playlist Deck",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Organize high-quality music and video streams",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                Button(
                    onClick = { isCreatingByDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = TubeRed),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("create_playlist_trigger")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Playlist",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Create", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- EMPTY STATE IF NO PLAYLISTS ---
        if (playlists.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.QueueMusic,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No playlists found",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tap 'Create' to assemble local music streams or video shelves.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        } else {
            // --- HORIZONTAL EXPANSIVE PLAYLIST FOLDERS CHIP ---
            item {
                Text(
                    text = "Select Active Folder Shelf",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    playlists.forEach { pl ->
                        val isSelected = selectedPlaylistId == pl.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) TubeRed.copy(alpha = 0.2f) else DarkSurface)
                                .border(1.dp, if (isSelected) TubeRed else BorderWhiteSmall, RoundedCornerShape(10.dp))
                                .clickable {
                                    selectedPlaylistId = if (isSelected) null else pl.id
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .testTag("playlist_folder_chip"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.FolderOpen else Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = if (isSelected) TubeRed else Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = pl.name,
                                    color = if (isSelected) TubeRed else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- TRACKS DISPLAY SCREEN AREA FOR ACTIVE FOLDER ---
        selectedPlaylistId?.let { plId ->
            val folderData = playlists.find { it.id == plId }
            folderData?.let { activeFolder ->
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Playlist Shelf: ${activeFolder.name}",
                                color = TubeAmber,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (activeFolder.description.isNotEmpty()) {
                                Text(
                                    text = activeFolder.description,
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                onDeletePlaylist(plId)
                                selectedPlaylistId = null
                            },
                            modifier = Modifier.testTag("delete_playlist_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderDelete,
                                contentDescription = "Delete Folder",
                                tint = TubeRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Divider(color = LightSurface, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(10.dp))
                }

                val itemsList = itemsListState?.value ?: emptyList()

                if (itemsList.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.PlaylistAdd,
                                        contentDescription = null,
                                        tint = TextMuted,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Folder is completely empty.",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "To stream tracks inside playlists, select recommendations or histories in Stats tab, click and load them into active folders.",
                                        color = TextSecondary,
                                        fontSize = 10.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                } else {
                    items(itemsList) { track ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurface)
                                .border(1.dp, BorderWhiteSmall, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                                .clickable {
                                    onSelectTrack(track.videoId, track.title, track.channelTitle, track.isMusic)
                                }
                                .testTag("playlist_track_row"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (track.isMusic) Icons.Default.MusicNote else Icons.Default.PlayCircle,
                                contentDescription = null,
                                tint = if (track.isMusic) TubeAmber else TubeRed,
                                modifier = Modifier.size(18.dp)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = track.title,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    text = track.channelTitle,
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    maxLines = 1
                                )
                            }

                            IconButton(onClick = { onRemovePlaylistItem(track.id) }) {
                                Icon(
                                    imageVector = Icons.Default.RemoveCircleOutline,
                                    contentDescription = "Remove Track",
                                    tint = TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal dialog to build playlist
    if (isCreatingByDialog) {
        AlertDialog(
            onDismissRequest = { isCreatingByDialog = false },
            containerColor = DarkSurface,
            title = {
                Text("Create Playlist Folder", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = playlistTitle,
                        onValueChange = { playlistTitle = it },
                        label = { Text("Folder Name (e.g. Chill Beats)", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = TubeRed,
                            unfocusedBorderColor = BorderWhiteNormal
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .testTag("playlist_title_input")
                    )

                    OutlinedTextField(
                        value = playlistDescription,
                        onValueChange = { playlistDescription = it },
                        label = { Text("Description (Optional)", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = TubeRed,
                            unfocusedBorderColor = BorderWhiteNormal
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("playlist_description_input")
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
                            Toast.makeText(context, "Playlist created successfully!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TubeRed),
                    modifier = Modifier.testTag("submit_create_playlist_button")
                ) {
                    Text("Create Folder", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { isCreatingByDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}
