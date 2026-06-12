package com.example.ui.components

import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.*
import java.net.URLDecoder

@Composable
fun YouTubeWebView(
    isMusicMode: Boolean = false,
    onVideoDetected: (videoId: String, title: String) -> Unit,
    onAdBlocked: () -> Unit,
    isAdBlockingActive: Boolean
) {
    val baseYoutubeUrl = if (isMusicMode) "https://music.youtube.com" else "https://m.youtube.com"

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var currentUrl by remember { mutableStateOf(baseYoutubeUrl) }
    var detectedVideoId by remember { mutableStateOf<String?>(null) }
    var detectedTitle by remember { mutableStateOf<String?>(null) }

    // Helper functions to parse video ID and titles declared first, before references
    fun parseYoutubeUrl(url: String) {
        try {
            if (url.contains("v=")) {
                val index = url.indexOf("v=") + 2
                val ampersandIndex = url.indexOf("&", index)
                val id = if (ampersandIndex != -1) {
                    url.substring(index, ampersandIndex)
                } else {
                    url.substring(index)
                }
                detectedVideoId = URLDecoder.decode(id, "UTF-8")
                detectedTitle = if (url.contains("music.youtube")) "Custom Stream - Audio Node" else "Custom Stream - Video Node"
            } else if (url.contains("shorts/")) {
                val index = url.indexOf("shorts/") + 7
                val separatorIndex = url.indexOf("?", index)
                val id = if (separatorIndex != -1) {
                    url.substring(index, separatorIndex)
                } else {
                    url.substring(index)
                }
                detectedVideoId = URLDecoder.decode(id, "UTF-8")
                detectedTitle = "Shorts - Dynamic Video Node"
            }
        } catch (_: Exception) {}
    }

    fun injectAdBlocker(view: WebView?) {
        view?.let { webView ->
            val cssSelector = """
                var style = document.createElement('style');
                style.innerHTML = 'div.ad-container, ytd-companion-ad-renderer, .video-ads, .ytp-ad-module, .ytp-ad-overlay-container, .ytp-ad-image-overlay, ytm-pivot-bar, .ytm-pivot-bar, ytm-pivot-bar-renderer, pivot-bar-renderer { display: none !important; }';
                document.head.appendChild(style);
            """.trimIndent()
            webView.evaluateJavascript(cssSelector, null)

            val jsSkipAd = """
                (function() {
                    setInterval(function() {
                        var skipBtn = document.querySelector('.ytp-ad-skip-button') || document.querySelector('.ytp-ad-skip-button-modern');
                        if (skipBtn) {
                            skipBtn.click();
                            console.log('TubeCompanion programmatically blocked and bypassed YouTube Ad element.');
                        }
                        var video = document.querySelector('video');
                        if (video && video.currentTime > 0) {
                            var adPlaying = document.querySelector('.ad-showing') || document.querySelector('.ytp-ad-player-overlay');
                            if (adPlaying) {
                                if (isFinite(video.duration)) {
                                    video.currentTime = video.duration - 0.1;
                                }
                            }
                        }
                    }, 500);
                })();
            """.trimIndent()
            webView.evaluateJavascript(jsSkipAd, null)
        }
    }

    // Reroute whenever the web-mode changes
    LaunchedEffect(isMusicMode) {
        webViewInstance?.loadUrl(baseYoutubeUrl)
    }

    Box(modifier = Modifier.fillMaxSize().background(OledBackground)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Web Content Node
            AndroidView(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("youtube_webview"),
                factory = { context ->
                    WebView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            mediaPlaybackRequiresUserGesture = false
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            setSupportZoom(true)
                        }
                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                val url = request?.url?.toString() ?: ""
                                currentUrl = url
                                parseYoutubeUrl(url)
                                return false
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                url?.let {
                                    currentUrl = it
                                    parseYoutubeUrl(it)
                                }
                                if (isAdBlockingActive) {
                                    injectAdBlocker(view)
                                    onAdBlocked()
                                }
                            }
                        }
                        loadUrl(baseYoutubeUrl)
                        webViewInstance = this
                    }
                },
                update = { webView ->
                    webViewInstance = webView
                }
            )
        }

        // Animated overlay to Play detected video ad-free
        AnimatedVisibility(
            visible = detectedVideoId != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            detectedVideoId?.let { videoId ->
                val safeTitle = detectedTitle ?: "YouTube Video Stream"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(TubeRed, TubeAmber)
                            )
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                        .clickable {
                            onVideoDetected(videoId, safeTitle)
                            detectedVideoId = null
                        }
                        .testTag("extract_and_play_banner"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Play Ad-Free & Background Supported",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = safeTitle,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = "Stream Now",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
