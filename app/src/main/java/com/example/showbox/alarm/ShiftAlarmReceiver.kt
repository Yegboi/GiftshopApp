package com.example.showbox.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import com.example.showbox.MainActivity
import com.example.showbox.R
import com.example.showbox.data.ShiftPlan

/** Posts the shift-over notification, so the alarm also lands with the app closed. */
class ShiftAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        ensureChannel(context)

        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val notification = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_shift)
            .setContentTitle("Schicht vorbei")
            .setContentText(ShiftPlan.SHIFT_OVER_MESSAGE)
            .setCategory(Notification.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(openApp)
            .build()

        val shiftId = intent.getStringExtra(EXTRA_SHIFT_ID).orEmpty()
        runCatching {
            context.getSystemService(NotificationManager::class.java)
                ?.notify(shiftId.hashCode(), notification)
        }
    }

    companion object {
        const val CHANNEL_ID = "shift_end"
        const val EXTRA_SHIFT_ID = "shift_id"

        fun ensureChannel(context: Context) {
            val manager = context.getSystemService(NotificationManager::class.java) ?: return
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Schichtende",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Alarm, wenn deine Schicht zu Ende ist"
                enableVibration(true)
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build(),
                )
            }
            runCatching { manager.createNotificationChannel(channel) }
        }
    }
}
