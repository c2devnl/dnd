package nl.c2dev.dnd

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


class DndTimerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        context.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
    }
}