package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class ShortVideoItem(
    val id: String,
    val videoId: String,
    val title: String,
    val creator: String,
    val initialLikes: Int,
    val initialComments: Int,
    val bgGradient: List<Color>,
    val musicTrackName: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShortsShelf(
    onSelectShortStream: (videoId: String, title: String, channel: String, isMusic: Boolean) -> Unit,
    activeVideoId: String,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit
) {
    val context = LocalContext.current

    // Curated high quality short streams
    val shortVideos = remember {
        listOf(
            ShortVideoItem(
                id = "1",
                videoId = "jfKfPfyJRdk",
                title = "Satisfying vintage modular synthesizer patch lofi loop #modular #lofi",
                creator = "SynthLoops",
                initialLikes = 10432,
                initialComments = 249,
                bgGradient = listOf(Color(0xFF2C1654), Color(0xFF130E26)),
                musicTrackName = "Original Sound - SynthLoops"
            ),
            ShortVideoItem(
                id = "2",
                videoId = "5qap5aO4i9A",
                title = "Cozy rainy night in Tokyo coffee shop. Pure ambient mood #rain #tokyo",
                creator = "TokyoAmbience",
                initialLikes = 8901,
                initialComments = 112,
                bgGradient = listOf(Color(0xFF11253E), Color(0xFF0C1625)),
                musicTrackName = "Tokyo Rain Jazz Radio"
            ),
            ShortVideoItem(
                id = "3",
                videoId = "E1w0_bUInXY",
                title = "The ultimate lofi chilling coffee beat drop that hits different ☕️",
                creator = "LofiVibes Only",
                initialLikes = 14890,
                initialComments = 562,
                bgGradient = listOf(Color(0xFF42211C), Color(0xFF201210)),
                musicTrackName = "Chilling Coffeeshop (Beat Copy)"
            ),
            ShortVideoItem(
                id = "4",
                videoId = "tNkDsD1GYKY",
                title = "How to write lofi chord progressions on guitar in under 60 seconds! #guitar",
                creator = "GuitarLicksHub",
                initialLikes = 7531,
                initialComments = 95,
                bgGradient = listOf(Color(0xFF144D34), Color(0xFF09261A)),
                musicTrackName = "Jazzy Chord Warmup"
            )
        )
    }

    // Dynamic likes/dislikes tracking state
    val likedIds = remember { mutableStateMapOf<String, Boolean>() }
    val dislikedIds = remember { mutableStateMapOf<String, Boolean>() }
    val commentsOpen = remember { mutableStateOf<ShortVideoItem?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("shorts_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            itemsIndexed(shortVideos) { index, item ->
                val hasLiked = likedIds[item.id] == true
                val hasDisliked = dislikedIds[item.id] == true
                val isCurrentlyPlayingBg = activeVideoId == item.videoId && isPlaying

                // Build full-height responsive viewport container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(600.dp) // Perfect tablet/mobile standard container height
                        .background(Brush.verticalGradient(item.bgGradient))
                        .border(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    // Integrated ambient canvas decoration
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.02f),
                            radius = size.width / 1.5f,
                            center = androidx.compose.ui.geometry.Offset(size.width, size.height / 3f)
                        )
                    }

                    // Centered Big Play button indicator
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                onSelectShortStream(item.videoId, item.title, item.creator, false)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedVisibility(
                            visible = isCurrentlyPlayingBg,
                            enter = fadeIn() + scaleIn(),
                            exit = fadeOut() + scaleOut()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(TubeRed.copy(alpha = 0.15f))
                                    .border(1.5.dp, TubeRed, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = "Background stream active",
                                    tint = TubeRed,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        if (!isCurrentlyPlayingBg) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Load to background stream",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .padding(start = 4.dp)
                                )
                            }
                        }
                    }

                    // BOTTOM INFO DETAILS LAYER (Asymmetrical Overlay)
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth(0.82f)
                            .padding(16.dp)
                    ) {
                        // Creator Badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = item.creator.firstOrNull()?.toString()?.uppercase() ?: "S",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            Text(
                                text = "@${item.creator}",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )

                            // Follow mock button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.White)
                                    .clickable {
                                        Toast.makeText(context, "Subscribed to @${item.creator}", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "SUBSCRIBE",
                                    color = Color.Black,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        // Title description
                        Text(
                            text = item.title,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Sound track ticker
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.4f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = TubeAmber,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = item.musicTrackName,
                                color = TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // RIGHT FLOATING ACTION BAR COLUMN (Youtubesque Layout)
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 12.dp, bottom = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Likes controller
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(
                                onClick = {
                                    if (hasLiked) {
                                        likedIds.remove(item.id)
                                    } else {
                                        likedIds[item.id] = true
                                        dislikedIds.remove(item.id)
                                    }
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.4f))
                                    .testTag("shorts_like_button_${item.id}")
                            ) {
                                Icon(
                                    imageVector = if (hasLiked) Icons.Default.ThumbUp else Icons.Outlined.ThumbUp,
                                    contentDescription = "Like Short",
                                    tint = if (hasLiked) TubeRed else Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = "${item.initialLikes + if (hasLiked) 1 else 0}",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        // Dislikes controller
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(
                                onClick = {
                                    if (hasDisliked) {
                                        dislikedIds.remove(item.id)
                                    } else {
                                        dislikedIds[item.id] = true
                                        likedIds.remove(item.id)
                                    }
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.4f))
                            ) {
                                Icon(
                                    imageVector = if (hasDisliked) Icons.Default.ThumbDown else Icons.Outlined.ThumbDown,
                                    contentDescription = "Dislike Short",
                                    tint = if (hasDisliked) TubeAmber else Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = "Dislike",
                                color = Color.White,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        // Comments trigger
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(
                                onClick = { commentsOpen.value = item },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.4f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Comment,
                                    contentDescription = "Show comments",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = "Comments",
                                color = Color.White,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        // Share stream trigger
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(
                                onClick = {
                                    Toast.makeText(context, "Copied link to: ${item.title}", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.4f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share Short Link",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = "Share",
                                color = Color.White,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Rotating vinyl disc music tracker
                        val infiniteTransition = rememberInfiniteTransition(label = "VinylRotate")
                        val rotationAngle by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(3500, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "VinylAngle"
                        )

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .rotate(if (isCurrentlyPlayingBg) rotationAngle else 0f)
                                .clip(CircleShape)
                                .background(Color.Black)
                                .border(1.5.dp, if (isCurrentlyPlayingBg) TubeRed else BorderWhiteSmall, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Album,
                                contentDescription = null,
                                tint = if (isCurrentlyPlayingBg) TubeRed else Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Interactive Comment Drawer Sheet
    commentsOpen.value?.let { activeItem ->
        AlertDialog(
            onDismissRequest = { commentsOpen.value = null },
            containerColor = DarkSurface,
            title = {
                Text(
                    text = "Comments (${activeItem.initialComments})",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(LightSurface)
                            .padding(10.dp)
                    ) {
                        Column {
                            Text("@MelophileBob: This wave sequence is unbelievably relaxing! Playing this in background while studying.", color = Color.White, fontSize = 11.sp)
                            Text("10 mins ago", color = TextSecondary, fontSize = 9.sp)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(LightSurface)
                            .padding(10.dp)
                    ) {
                        Column {
                            Text("@LofiBeats_Collector: Pure gold! Keep uploading more #modular shorts loop please! 🥰", color = Color.White, fontSize = 11.sp)
                            Text("2 hours ago", color = TextSecondary, fontSize = 9.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { commentsOpen.value = null },
                    colors = ButtonDefaults.buttonColors(containerColor = TubeRed)
                ) {
                    Text("Close", color = Color.White, fontSize = 11.sp)
                }
            }
        )
    }
}
