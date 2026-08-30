package com.example.steamfun.ui

import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.steamfun.data.Screenshot
import com.example.steamfun.data.Trailer

/** Shared shell: near-fullscreen, dark, with a close button under the content. */
@Composable
private fun FullscreenDialog(
    onDismiss: () -> Unit,
    title: String,
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.94f)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(12.dp),
            )
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                content()
            }
            TextButton(onClick = onDismiss, modifier = Modifier.padding(bottom = 20.dp)) {
                Text("Schliessen")
            }
        }
    }
}

@Composable
fun ScreenshotDialog(screenshot: Screenshot, onDismiss: () -> Unit) {
    FullscreenDialog(onDismiss = onDismiss, title = "Screenshot") {
        AsyncImage(
            model = screenshot.fullUrl,
            contentDescription = "Screenshot in voller Grösse",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * Plays a trailer with the platform's own player.
 *
 * It lives in a dialog so the surface is created and torn down with the
 * overlay — a VideoView left in a scrolling page keeps playing once it is out
 * of sight.
 */
@Composable
fun TrailerDialog(trailer: Trailer, onDismiss: () -> Unit) {
    FullscreenDialog(onDismiss = onDismiss, title = trailer.name) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { context ->
                VideoView(context).apply {
                    setVideoURI(Uri.parse(trailer.videoUrl))
                    setMediaController(
                        MediaController(context).also { it.setAnchorView(this) },
                    )
                    setOnPreparedListener { start() }
                }
            },
            onRelease = { view -> view.stopPlayback() },
        )
    }
}

/**
 * Steam's description is HTML with images — animated GIFs and AVIF among them.
 * A WebView renders it as the store does, which no Compose text layout would.
 * Scripting stays off: this is other people's markup and nothing here needs it.
 */
@Composable
fun DescriptionDialog(title: String, html: String, onDismiss: () -> Unit) {
    FullscreenDialog(onDismiss = onDismiss, title = title) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = false
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    webViewClient = WebViewClient()
                }
            },
            update = { view ->
                view.loadDataWithBaseURL(
                    STEAM_BASE_URL,
                    wrapInDarkPage(html),
                    "text/html",
                    "utf-8",
                    null,
                )
            },
            onRelease = { view ->
                view.loadUrl("about:blank")
                view.destroy()
            },
        )
    }
}

/** Steam's markup carries no styling of its own, so it gets the app's. */
private fun wrapInDarkPage(html: String): String = """
    <!DOCTYPE html>
    <html><head>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <style>
      body { background:#171A21; color:#C7D5E0; font-family:sans-serif;
             margin:14px; line-height:1.55; font-size:15px; }
      img, video { max-width:100%; height:auto; border-radius:6px; }
      h1,h2,h3,h4 { color:#66C0F4; }
      a { color:#66C0F4; }
      table { max-width:100%; }
    </style>
    </head><body>$html</body></html>
""".trimIndent()

private const val STEAM_BASE_URL = "https://store.steampowered.com/"
