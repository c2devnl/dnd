package nl.c2dev.dnd


import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.getSystemService
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.HOURS
import java.util.concurrent.TimeUnit.MINUTES


sealed class DndConfig() {
    object None : DndConfig()
    class Manual(val filter: Int) : DndConfig()
    class Timer(val units: Long , val unit: TimeUnit, val filter: Int) : DndConfig()
    class Alarm(val time: Long, val filter: Int) : DndConfig()
}

fun Context.applyDndConfig(config: DndConfig) {
    when (config) {
        is DndConfig.None -> {
            setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
        }
        is DndConfig.Manual -> {
            setInterruptionFilter(config.filter)
        }
        is DndConfig.Alarm -> {
            setInterruptionFilter(config.filter)
            disableDndAt(config.time)
        }
        is DndConfig.Timer -> {
            setInterruptionFilter(config.filter)
            disableDndAt(System.currentTimeMillis() + config.unit.toMillis(config.units))
        }
    }
}

private fun Context.disableDndAt(time: Long) {
    val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            Intent(this, DndTimerReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT
    )

    val alarm = getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarm.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent)
}

fun Context.setInterruptionFilter(filter: Int) {
    val notificationManager = getSystemService<NotificationManager>()!!

    if (!notificationManager.isNotificationPolicyAccessGranted) {
        startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })

        Toast.makeText(this, R.string.toast_give_dnd, Toast.LENGTH_SHORT).show()
    } else {
        notificationManager.setInterruptionFilter(filter)
    }
}