package nl.c2dev.dnd

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dndAccessButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
        }

        dndPopupButton.setOnClickListener {
            startActivity(Intent(this@MainActivity, DndActivity::class.java))
        }

        dndSettingsButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ZEN_MODE_PRIORITY_SETTINGS))
        }
    }
}