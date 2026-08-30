package com.example.showbox.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.showbox.data.Person
import com.example.showbox.data.ShiftPlan
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Registers one system alarm per remaining shift end. Falls back to an
 * inexact window when the user has not granted exact-alarm access, so the
 * reminder still arrives — just within a minute rather than to the second.
 */
object AlarmScheduler {

    fun reschedule(context: Context, person: Person?, festivalStart: LocalDate?) {
        cancelAll(context)
        if (person == null || festivalStart == null) return

        val manager = context.getSystemService(AlarmManager::class.java) ?: return
        ShiftAlarmReceiver.ensureChannel(context)

        val now = LocalDateTime.now()
        ShiftPlan.instancesFor(person, festivalStart)
            .filter { it.end.isAfter(now) }
            .forEach { instance ->
                val triggerAtMs = instance.end
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()

                val pendingIntent = pendingIntent(context, instance.shift.id)
                runCatching {
                    if (canScheduleExact(manager)) {
                        manager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerAtMs,
                            pendingIntent,
                        )
                    } else {
                        manager.setWindow(
                            AlarmManager.RTC_WAKEUP,
                            triggerAtMs,
                            INEXACT_WINDOW_MS,
                            pendingIntent,
                        )
                    }
                }
            }
    }

    fun cancelAll(context: Context) {
        val manager = context.getSystemService(AlarmManager::class.java) ?: return
        ShiftPlan.shifts.forEach { shift ->
            runCatching { manager.cancel(pendingIntent(context, shift.id)) }
        }
    }

    private fun canScheduleExact(manager: AlarmManager): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || manager.canScheduleExactAlarms()

    private fun pendingIntent(context: Context, shiftId: String): PendingIntent {
        val intent = Intent(context, ShiftAlarmReceiver::class.java)
            .putExtra(ShiftAlarmReceiver.EXTRA_SHIFT_ID, shiftId)
        return PendingIntent.getBroadcast(
            context,
            shiftId.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    private const val INEXACT_WINDOW_MS = 60_000L
}
