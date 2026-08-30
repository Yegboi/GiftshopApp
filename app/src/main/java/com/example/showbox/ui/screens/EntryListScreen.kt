@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.showbox.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.showbox.data.Category
import com.example.showbox.data.Entry
import com.example.showbox.ui.LibraryViewModel

/**
 * One screen for all four question sections. Categories with an answer get a
 * reveal toggle so the question can be read out before the answer is shown.
 */
@Composable
fun EntryListScreen(category: Category, library: LibraryViewModel) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    val entries = library.entriesOf(category)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(category.label) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(Icons.Filled.Add, contentDescription = "${category.promptLabel} hinzufügen")
            }
        },
    ) { innerPadding ->
        if (entries.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Noch nichts da",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "Tippe auf das Plus, um die erste " +
                        "${category.promptLabel.lowercase()} anzulegen.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                itemsIndexed(entries, key = { _, entry -> entry.id }) { index, entry ->
                    EntryCard(
                        index = index + 1,
                        entry = entry,
                        showAnswer = category.hasAnswer,
                        onDelete = { library.removeEntry(entry.id) },
                    )
                }
            }
        }
    }

    if (showDialog) {
        AddEntryDialog(
            category = category,
            onDismiss = { showDialog = false },
            onConfirm = { prompt, answer ->
                library.addEntry(category, prompt, answer)
                showDialog = false
            },
        )
    }
}

@Composable
private fun EntryCard(
    index: Int,
    entry: Entry,
    showAnswer: Boolean,
    onDelete: () -> Unit,
) {
    var revealed by rememberSaveable(entry.id) { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, top = 12.dp, end = 8.dp, bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = "$index.",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 10.dp),
                )
                Text(
                    text = entry.prompt,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Eintrag löschen",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (showAnswer) {
                HorizontalDivider(
                    modifier = Modifier.padding(top = 8.dp, end = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
                if (revealed) {
                    Text(
                        text = entry.answer.ifBlank { "Keine Antwort hinterlegt" },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 10.dp, end = 8.dp),
                    )
                }
                TextButton(onClick = { revealed = !revealed }) {
                    Text(if (revealed) "Antwort verbergen" else "Antwort zeigen")
                }
            }
        }
    }
}

@Composable
private fun AddEntryDialog(
    category: Category,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit,
) {
    var prompt by rememberSaveable { mutableStateOf("") }
    var answer by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        title = {
            Text(
                text = "Neue ${category.promptLabel}",
                color = MaterialTheme.colorScheme.onBackground,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    label = { Text(category.promptLabel) },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (category.hasAnswer) {
                    OutlinedTextField(
                        value = answer,
                        onValueChange = { answer = it },
                        label = { Text("Antwort") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(prompt, answer) },
                enabled = prompt.isNotBlank(),
            ) {
                Text("Speichern")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Abbrechen") }
        },
    )
}
