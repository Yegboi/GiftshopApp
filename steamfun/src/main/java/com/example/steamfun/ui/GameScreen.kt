@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.steamfun.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.steamfun.data.GuessMode
import com.example.steamfun.data.Guessing
import com.example.steamfun.data.ReviewBucket
import com.example.steamfun.data.Screenshot
import com.example.steamfun.data.StorePage
import com.example.steamfun.data.Trailer

@Composable
fun GameScreen(viewModel: SteamFunViewModel) {
    val round = viewModel.round

    var openScreenshot by remember { mutableStateOf<Screenshot?>(null) }
    var openTrailer by remember { mutableStateOf<Trailer?>(null) }
    var showDescription by remember { mutableStateOf(false) }

    val page: StorePage? = when (round) {
        is RoundState.Asking -> round.game.page
        is RoundState.Answered -> round.game.page
        else -> null
    }

    // A new store page closes whatever overlay belonged to the previous one.
    LaunchedEffect(page?.appId) {
        openScreenshot = null
        openTrailer = null
        showDescription = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Steam Fun") },
                    actions = {
                        if (viewModel.playedCount > 0) {
                            Text(
                                text = "${viewModel.correctCount} / ${viewModel.playedCount}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 16.dp),
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                    ),
                )
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ModeSelector(mode = viewModel.mode, onSelect = viewModel::selectMode)

                when (round) {
                    is RoundState.Loading -> LoadingCard(round.message)

                    is RoundState.Failed -> FailedCard(round.message, viewModel::nextGame)

                    is RoundState.Asking -> {
                        StorePageView(
                            page = round.game.page,
                            // Held back on purpose until a guess is in.
                            revealedReviews = null,
                            onOpenScreenshot = { openScreenshot = it },
                            onPlayTrailer = { openTrailer = it },
                            onOpenDescription = { showDescription = true },
                        )
                        GuessControls(viewModel)
                    }

                    is RoundState.Answered -> {
                        ResultCard(round, viewModel::nextGame)
                        StorePageView(
                            page = round.game.page,
                            revealedReviews = round.game.totalReviews,
                            onOpenScreenshot = { openScreenshot = it },
                            onPlayTrailer = { openTrailer = it },
                            onOpenDescription = { showDescription = true },
                        )
                    }
                }

                if (viewModel.catalogueSize > 0) {
                    Text(
                        text = "Gezogen aus ${viewModel.catalogueSize} Steam-Einträgen.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        if (round is RoundState.Answered && round.correct) {
            ConfettiBurst(modifier = Modifier.fillMaxSize())
        }
    }

    openScreenshot?.let { shot ->
        ScreenshotDialog(screenshot = shot, onDismiss = { openScreenshot = null })
    }
    openTrailer?.let { trailer ->
        TrailerDialog(trailer = trailer, onDismiss = { openTrailer = null })
    }
    if (showDescription && page != null) {
        DescriptionDialog(
            title = page.name,
            html = page.detailedDescriptionHtml,
            onDismiss = { showDescription = false },
        )
    }
}

@Composable
private fun ModeSelector(mode: GuessMode, onSelect: (GuessMode) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        GuessMode.entries.forEach { candidate ->
            FilterChip(
                selected = candidate == mode,
                onClick = { onSelect(candidate) },
                label = { Text(candidate.label) },
            )
        }
    }
}

@Composable
private fun GuessControls(viewModel: SteamFunViewModel) {
    when (viewModel.mode) {
        GuessMode.ACCURATE -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = viewModel.typedGuess,
                onValueChange = viewModel::onGuessTyped,
                label = { Text("Anzahl Reviews") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = "Treffer bei ±${(Guessing.TOLERANCE * 100).toInt()} % der echten Zahl.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                onClick = viewModel::submitTypedGuess,
                enabled = viewModel.canSubmitTyped,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Tipp abgeben")
            }
        }

        GuessMode.ROUNDABOUT -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ReviewBucket.entries.chunked(2).forEach { pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    pair.forEach { bucket ->
                        Button(
                            onClick = { viewModel.submitBucket(bucket) },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                        ) {
                            Text(bucket.label)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultCard(round: RoundState.Answered, onNext: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (round.correct) {
                Text(
                    text = "Richtig!",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
            } else {
                Text(text = "❌", fontSize = 64.sp)
                Text(
                    text = "Daneben",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Text(
                text = "Dein Tipp: ${round.guessLabel}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            round.deviationPercent?.let { deviation ->
                Text(
                    text = if (deviation == 0) "Punktgenau." else "$deviation % daneben.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
            ) {
                Text("Nächstes Spiel")
            }
        }
    }
}

@Composable
private fun LoadingCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun FailedCard(message: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            OutlinedButton(onClick = onRetry) { Text("Nochmal versuchen") }
        }
    }
}
