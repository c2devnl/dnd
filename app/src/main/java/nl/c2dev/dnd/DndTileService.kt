package nl.c2dev.dnd

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.core.content.getSystemService

class DndTileService : TileService() {
    private val DNDReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) = updateDNDToggle()
    }

    override fun onClick() {
        super.onClick()

        val openDNDDialogIntent = Intent(this, DndActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val closeQuickToggleAreaIntent = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)

        startActivity(openDNDDialogIntent)
        sendBroadcast(closeQuickToggleAreaIntent)
    }

    fun updateDNDToggle() {
        val tile = this.qsTile
        val notificationManager = getSystemService<NotificationManager>()!!
        val filter = notificationManager.currentInterruptionFilter

        val state = if (filter == NotificationManager.INTERRUPTION_FILTER_ALL) {
            Tile.STATE_INACTIVE
        } else {
            Tile.STATE_ACTIVE
        }

        val iconRes = if (filter == NotificationManager.INTERRUPTION_FILTER_ALL) {
            R.drawable.ic_do_not_disturb_off
        } else {
            R.drawable.ic_do_not_disturb_on
        }

        val labelRes = when(filter) {
            NotificationManager.INTERRUPTION_FILTER_PRIORITY -> R.string.interruptionFilterPriority
            NotificationManager.INTERRUPTION_FILTER_ALARMS -> R.string.interruptionFilterAlarms
            NotificationManager.INTERRUPTION_FILTER_NONE -> R.string.interruptionFilterNone
            else -> R.string.interruptionFilterAll
        }

        tile.state = state
        tile.label = getString(labelRes)
        tile.icon = Icon.createWithResource(this, iconRes)
        tile.updateTile()
    }

    override fun onStartListening() {
        super.onStartListening()

        registerReceiver(DNDReceiver, IntentFilter(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED))
        updateDNDToggle()
    }

    override fun onStopListening() {
        unregisterReceiver(DNDReceiver)

        super.onStopListening()
    }
}