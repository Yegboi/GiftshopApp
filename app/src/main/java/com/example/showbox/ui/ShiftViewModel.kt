package com.example.showbox.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.showbox.alarm.AlarmPlayer
import com.example.showbox.alarm.AlarmScheduler
import com.example.showbox.data.Person
import com.example.showbox.data.ShiftInstance
import com.example.showbox.data.ShiftPlan
import com.example.showbox.data.ShiftSettings
import com.example.showbox.data.ShiftStatus
import com.example.showbox.data.ShiftStore
import com.example.showbox.data.defaultFestivalStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Ticks once a second, derives the countdown from the rota, and raises the
 * shift-over alarm exactly once per shift.
 */
class ShiftViewModel(app: Application) : AndroidViewModel(app) {

    private val store = ShiftStore(File(app.filesDir, "showbox-shift.json"))
    private val alarmPlayer = AlarmPlayer(app)

    var settings by mutableStateOf(ShiftSettings())
        private set

    var now by mutableStateOf(LocalDateTime.now())
        private set

    /** True while the shift-over banner and its sound are up. */
    var alarmActive by mutableStateOf(false)
        private set

    val person: Person? get() = settings.person

    val festivalStart: LocalDate
        get() = settings.festivalStartDate ?: defaultFestivalStart(LocalDate.now())

    val status: ShiftStatus?
        get() = settings.person?.let { ShiftPlan.statusAt(it, festivalStart, now) }

    val myShifts: List<ShiftInstance>
        get() = settings.person?.let { ShiftPlan.instancesFor(it, festivalStart) }.orEmpty()

    init {
        settings = store.load()
            ?: ShiftSettings(festivalStart = defaultFestivalStart(LocalDate.now()).toString())

        catchUpMissedAlarm()
        rescheduleSystemAlarms()

        viewModelScope.launch {
            while (true) {
                delay(TICK_MS)
                tick()
            }
        }
    }

    fun selectPerson(person: Person) {
        persist(settings.copy(person = person))
        rescheduleSystemAlarms()
    }

    fun clearPerson() {
        dismissAlarm()
        persist(settings.copy(person = null))
        AlarmScheduler.cancelAll(getApplication())
    }

    /** Changing the festival date re-arms every alarm from scratch. */
    fun setFestivalStart(date: LocalDate) {
        persist(settings.copy(festivalStart = date.toString(), alarmedShiftIds = emptyList()))
        rescheduleSystemAlarms()
    }

    fun dismissAlarm() {
        alarmActive = false
        alarmPlayer.stop()
    }

    private fun tick() {
        val previous = now
        now = LocalDateTime.now()

        val person = settings.person ?: return
        val justEnded = ShiftPlan
            .endedBetween(person, festivalStart, previous, now)
            .filter { it.shift.id !in settings.alarmedShiftIds }

        if (justEnded.isNotEmpty()) {
            markAlarmed(justEnded)
            alarmActive = true
            alarmPlayer.start()
        }
    }

    /**
     * The app may have been closed when a shift ended. Show the banner for a
     * recent miss, but stay silent — the system notification already sounded.
     */
    private fun catchUpMissedAlarm() {
        val person = settings.person ?: return
        val missed = ShiftPlan
            .endedBetween(person, festivalStart, now.minusHours(CATCH_UP_HOURS), now)
            .filter { it.shift.id !in settings.alarmedShiftIds }

        if (missed.isNotEmpty()) {
            markAlarmed(missed)
            alarmActive = true
        }
    }

    private fun markAlarmed(instances: List<ShiftInstance>) {
        persist(
            settings.copy(
                alarmedShiftIds = settings.alarmedShiftIds + instances.map { it.shift.id },
            ),
        )
    }

    private fun rescheduleSystemAlarms() {
        AlarmScheduler.reschedule(getApplication(), settings.person, festivalStart)
    }

    private fun persist(updated: ShiftSettings) {
        settings = updated
        viewModelScope.launch(Dispatchers.IO) { store.save(updated) }
    }

    override fun onCleared() {
        alarmPlayer.stop()
        super.onCleared()
    }

    private companion object {
        const val TICK_MS = 1000L
        const val CATCH_UP_HOURS = 2L
    }
}
