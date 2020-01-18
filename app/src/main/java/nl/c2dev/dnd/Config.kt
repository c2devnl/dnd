package nl.c2dev.dnd

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.core.content.edit
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class Config(ctx: Context) {
    private val prefs = PreferenceManager.getDefaultSharedPreferences(ctx)

    var defaultFilter by IntPreference(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
    var defaultMode by IntPreference(DndActivity.DndMode.MANUAL.ordinal)
    var defaultTimerOption by IntPreference(2)

    private class IntPreference(private val default: Int) : ReadWriteProperty<Config, Int> {
        override fun getValue(thisRef: Config, property: KProperty<*>): Int {
            return thisRef.prefs.getInt(property.name, default)
        }

        override fun setValue(thisRef: Config, property: KProperty<*>, value: Int) {
            thisRef.prefs.edit { putInt(property.name, value) }
        }
    }
}