@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.steamfun.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.steamfun.data.GuessMode
import com.example.steamfun.data.Guessing
import com.example.steamfun.data.ReviewBucket
import com.example.steamfun.data.formatCount

@Composable
fun GameScreen(viewModel: SteamFunViewModel) {
    val round = viewModel.round

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
                ModeSelector(
                    mode = viewModel.mode,
                    onSelect = viewModel::selectMode,
                )

                when (round) {
                    RoundState.Loading -> LoadingCard()

                    is RoundState.Failed -> FailedCard(
                        message = round.message,
                        onRetry = viewModel::nextGame,
                    )

                    is RoundState.Asking -> {
                        StorePageCard(
                            name = round.game.name,
                            header = round.header,
                            // The count stays out of this state on purpose.
                            revealed = null,
                        )
                        GuessControls(viewModel = viewModel)
                    }

                    is RoundState.Answered -> {
                        StorePageCard(
                            name = round.game.name,
                            header = round.header,
                            revealed = round.game.totalReviews,
                        )
                        ResultCard(round = round, onNext = viewModel::nextGame)
                    }
                }
            }
        }

        if (round is RoundState.Answered && round.correct) {
            ConfettiBurst(modifier = Modifier.fillMaxSize())
        }
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
private fun StorePageCard(name: String, header: ImageBitmap?, revealed: Int?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(HEADER_ASPECT)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                if (header != null) {
                    Image(
                        bitmap = header,
                        contentDescription = "Store-Bild von $name",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Text(
                        text = "Kein Bild",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = if (revealed == null) {
                        "Wie viele Reviews hat dieses Spiel?"
                    } else {
                        "Tatsächlich: ${formatCount(revealed)} Reviews"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (revealed == null) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    fontWeight = if (revealed == null) FontWeight.Normal else FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
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
                Text(text = "Richtig!", style = MaterialTheme.typography.headlineSmall,
                     color = MaterialTheme.colorScheme.secondary)
            } else {
                Text(text = "❌", fontSize = 64.sp)
                Text(text = "Daneben", style = MaterialTheme.typography.headlineSmall,
                     color = MaterialTheme.colorScheme.error)
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

            Button(onClick = onNext, modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)) {
                Text("Nächstes Spiel")
            }
        }
    }
}

@Composable
private fun LoadingCard() {
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
                text = "Steam-Seite wird geladen …",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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

private const val HEADER_ASPECT = 460f / 215f
