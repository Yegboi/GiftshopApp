@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.showbox.ui.screens

import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.showbox.data.Person
import com.example.showbox.data.ShiftInstance
import com.example.showbox.data.ShiftPlan
import com.example.showbox.data.ShiftStatus
import com.example.showbox.data.formatCountdown
import com.example.showbox.ui.ShiftViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DayFormat: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEEE, d. MMMM", Locale.GERMAN)

@Composable
fun ShiftScreen(shift: ShiftViewModel) {
    val context = LocalContext.current

    // The shift-over alarm shows as a notification when the app is closed.
    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            if (!granted) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Schicht") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
    ) { innerPadding ->
        val person = shift.person

        if (person == null) {
            PersonPicker(
                onPick = { shift.selectPerson(it) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (shift.alarmActive) {
                item { AlarmBanner(onDismiss = { shift.dismissAlarm() }) }
            }

            item { CountdownCard(person = person, status = shift.status) }

            item {
                Text(
                    text = "Deine Schichten",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            items(shift.myShifts, key = { it.shift.id }) { instance ->
                ShiftRow(instance = instance, isCurrent = shift.status.isRunningShift(instance))
            }

            item { FestivalDateRow(shift = shift) }

            item {
                TextButton(onClick = { shift.clearPerson() }) {
                    Text("Ich bin jemand anders")
                }
            }
        }
    }
}

/** True when this status is the running shift for [candidate]. */
private fun ShiftStatus?.isRunningShift(candidate: ShiftInstance): Boolean = when (this) {
    is ShiftStatus.Running -> instance.shift.id == candidate.shift.id
    else -> false
}

@Composable
private fun PersonPicker(onPick: (Person) -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Wer bist du?",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "${ShiftPlan.VENUE} · ${ShiftPlan.FESTIVAL_NAME}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp),
        )

        Person.entries.forEach { person ->
            Button(
                onClick = { onPick(person) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
            ) {
                Text(person.displayName, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun AlarmBanner(onDismiss: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "🍺",
                fontSize = 48.sp,
            )
            Text(
                text = ShiftPlan.SHIFT_OVER_MESSAGE,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                text = "Schicht vorbei.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(top = 4.dp),
            )
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                modifier = Modifier.padding(top = 12.dp),
            ) {
                Text("Alarm aus")
            }
        }
    }
}

@Composable
private fun CountdownCard(person: Person, status: ShiftStatus?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = person.displayName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            when (status) {
                is ShiftStatus.Running -> {
                    Label("Schicht läuft — noch")
                    Countdown(formatCountdown(status.remaining))
                    Detail(
                        "${status.instance.shift.dayName}, " +
                            "${status.instance.shift.timeLabel}",
                    )
                }

                is ShiftStatus.Upcoming -> {
                    Label("Nächste Schicht in")
                    Countdown(formatCountdown(status.remaining))
                    Detail(
                        "${status.instance.shift.dayName}, " +
                            "${status.instance.shift.timeLabel}",
                    )
                    if (status.instance.shift.note.isNotBlank()) {
                        Detail(status.instance.shift.note)
                    }
                }

                ShiftStatus.AllDone, null -> {
                    Label("Keine Schicht mehr offen")
                    Detail("Feierabend — genieß das Festival.")
                }
            }
        }
    }
}

@Composable
private fun Label(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 12.dp),
    )
}

@Composable
private fun Countdown(text: String) {
    Text(
        text = text,
        fontSize = 40.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 4.dp),
    )
}

@Composable
private fun Detail(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 4.dp),
    )
}

@Composable
private fun ShiftRow(instance: ShiftInstance, isCurrent: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = instance.start.format(DayFormat),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = instance.shift.timeLabel,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            val colleagues = instance.shift.people.joinToString(" & ") { it.displayName }
            Text(
                text = "Mit dabei: $colleagues",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (instance.shift.note.isNotBlank()) {
                Text(
                    text = instance.shift.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun FestivalDateRow(shift: ShiftViewModel) {
    val context = LocalContext.current
    val start: LocalDate = shift.festivalStart

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Festivalstart (Donnerstag)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = start.format(DayFormat),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        OutlinedButton(
            onClick = {
                DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        shift.setFestivalStart(LocalDate.of(year, month + 1, dayOfMonth))
                    },
                    start.year,
                    start.monthValue - 1,
                    start.dayOfMonth,
                ).show()
            },
        ) {
            Text("Ändern")
        }
    }
}
