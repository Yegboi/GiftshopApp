package com.example.steamfun.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.steamfun.data.Screenshot
import com.example.steamfun.data.StorePage
import com.example.steamfun.data.Trailer
import com.example.steamfun.data.formatCount

/**
 * The store page as the game shows it: artwork, trailers, screenshots, the
 * text and the facts — everything except the review count, which appears only
 * once [revealedReviews] is non-null.
 */
@Composable
fun StorePageView(
    page: StorePage,
    revealedReviews: Int?,
    onOpenScreenshot: (Screenshot) -> Unit,
    onPlayTrailer: (Trailer) -> Unit,
    onOpenDescription: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column {
            AsyncImage(
                model = page.headerImageUrl,
                contentDescription = "Titelbild von ${page.name}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(HEADER_ASPECT)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = page.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                FactsLine(page)

                if (page.shortDescription.isNotBlank()) {
                    Text(
                        text = page.shortDescription,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (page.detailedDescriptionHtml.isNotBlank()) {
                    OutlinedButton(onClick = onOpenDescription) {
                        Text("Ganze Beschreibung")
                    }
                }

                Text(
                    text = if (revealedReviews == null) {
                        "Wie viele Reviews hat dieses Spiel?"
                    } else {
                        "Tatsächlich: ${formatCount(revealedReviews)} Reviews"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (revealedReviews == null) FontWeight.Normal else FontWeight.Bold,
                    color = if (revealedReviews == null) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    modifier = Modifier.padding(top = 2.dp),
                )
            }

            if (page.trailers.isNotEmpty()) {
                SectionLabel("Trailer")
                TrailerStrip(trailers = page.trailers, onPlay = onPlayTrailer)
            }

            if (page.screenshots.isNotEmpty()) {
                SectionLabel("Screenshots")
                ScreenshotPager(screenshots = page.screenshots, onOpen = onOpenScreenshot)
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 6.dp, bottom = 6.dp),
    )
}

@Composable
private fun FactsLine(page: StorePage) {
    val facts = buildList {
        page.developers.firstOrNull()?.let { add(it) }
        page.releaseDate.takeIf { it.isNotBlank() }?.let { add(it) }
        page.price.takeIf { it.isNotBlank() }?.let { add(it) }
    }
    if (facts.isNotEmpty()) {
        Text(
            text = facts.joinToString(" · "),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    if (page.genres.isNotEmpty()) {
        Text(
            text = page.genres.joinToString(", "),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TrailerStrip(trailers: List<Trailer>, onPlay: (Trailer) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(bottom = 8.dp),
    ) {
        items(trailers, key = { it.videoUrl }) { trailer ->
            Box(
                modifier = Modifier
                    .width(240.dp)
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onPlay(trailer) },
                contentAlignment = Alignment.Center,
            ) {
                if (trailer.thumbnailUrl.isNotBlank()) {
                    AsyncImage(
                        model = trailer.thumbnailUrl,
                        contentDescription = trailer.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "${trailer.name} abspielen",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ScreenshotPager(screenshots: List<Screenshot>, onOpen: (Screenshot) -> Unit) {
    val pagerState = rememberPagerState(pageCount = { screenshots.size })

    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 10.dp,
            modifier = Modifier.fillMaxWidth(),
        ) { index ->
            val shot = screenshots[index]
            AsyncImage(
                model = shot.thumbnailUrl,
                contentDescription = "Screenshot ${index + 1} von ${screenshots.size}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onOpen(shot) },
            )
        }
        Text(
            text = "${pagerState.currentPage + 1} / ${screenshots.size} · tippen zum Vergrössern",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, top = 6.dp),
        )
    }
}

private const val HEADER_ASPECT = 460f / 215f
