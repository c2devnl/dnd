package nl.c2dev.dnd

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.CompoundButton
import androidx.core.content.getSystemService
import kotlinx.android.synthetic.main.activity_dnd.*
import kotlinx.android.synthetic.main.dnd3.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit.*


class DndActivity : Activity() {
    private lateinit var config: Config

    private lateinit var mode: DndMode
    private var dndEnabled = true
    private var filter = -1
    private var timerOption = -1
    private var alarmTime: Long? = null

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val alarmFormatter = SimpleDateFormat("EE HH:mm", Locale.getDefault())

    enum class DndMode { MANUAL, TIMER, ALARM }

    private val timerOptions = listOf(
            MINUTES to 1,
            MINUTES to 15,
            MINUTES to 30,
            MINUTES to 45,
            HOURS to 1,
            HOURS to 2,
            HOURS to 3,
            HOURS to 4,
            HOURS to 5,
            HOURS to 6,
            HOURS to 7,
            HOURS to 8,
            HOURS to 9,
            HOURS to 10,
            HOURS to 11,
            HOURS to 12
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dnd)

        window.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        config = Config(this)
        alarmTime = getSystemService<AlarmManager>()?.nextAlarmClock?.triggerTime

        if (alarmTime == null && config.defaultMode == DndMode.ALARM.ordinal) {
            config.defaultMode = DndMode.MANUAL.ordinal
        }

        mode = DndMode.values()[config.defaultMode]
        filter = config.defaultFilter
        timerOption = config.defaultTimerOption

        initUI()

        onSetDndFilter(config.defaultFilter)
        onSetDndMode(DndMode.values()[config.defaultMode])
        onSetDndTimerOption(config.defaultTimerOption)
        onSetAlarmTime(alarmTime)

        val layoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                background.viewTreeObserver.removeOnGlobalLayoutListener(this)
                animateClipIn(background, container)
            }
        }
        background.viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
    }

    private fun initUI () {
        makeRadioButtons(
                listOf(
                        priorityButton to NotificationManager.INTERRUPTION_FILTER_PRIORITY,
                        alarmsButton to NotificationManager.INTERRUPTION_FILTER_ALARMS,
                        silenceButton to NotificationManager.INTERRUPTION_FILTER_NONE
                ),
                defaultValue = filter,
                action = this::onSetDndFilter
        )

        makeRadioButtons(
                listOf(
                        manualButton to DndMode.MANUAL,
                        timerButton to DndMode.TIMER,
                        alarmButton to DndMode.ALARM
                ),
                defaultValue = mode,
                action = this::onSetDndMode
        )

        // increase clickable surface of the buttons
        manualText.setOnClickListener { manualButton.performClick() }
        timerContainer.setOnClickListener { timerButton.performClick() }
        alarmText.setOnClickListener { alarmButton.performClick() }

        plusButton.setOnClickListener { onSetDndTimerOption(timerOption + 1) }
        minusButton.setOnClickListener { onSetDndTimerOption( timerOption - 1) }

        dndEnabledSwitch.setOnCheckedChangeListener { _, isChecked -> onSetDndEnabled(isChecked) }
        titleText.setOnClickListener { dndEnabledSwitch.performClick() }

        container.setOnClickListener { onDoneClicked() }
        doneButton.setOnClickListener { onDoneClicked() }

        moreSettingsButton.setOnClickListener { startActivity(Intent(Settings.ACTION_ZEN_MODE_PRIORITY_SETTINGS)) }
    }

    private fun <T> makeRadioButtons(
            buttonValuePairs: List<Pair<CompoundButton, T>>,
            defaultValue: T,
            action: (T) -> Unit
    ) {
        var selectedButton: CompoundButton? = null

        for ((button, value) in buttonValuePairs) {
            button.isChecked = value == defaultValue
            if (button.isChecked) selectedButton = button

            button.setOnClickListener {
                selectedButton?.isChecked = false
                selectedButton = button
                selectedButton?.isChecked = true
                action(value)
            }
        }
    }

    private fun onSetDndEnabled(enabled: Boolean) {
        dndEnabled = enabled

        val alphaTo = if (enabled) 1F else 0.1F

        val animatedViews = listOf<View>(
                filterButtonsContainer, manualText, timerText, timerUntilText, manualButton,
                timerButton, minusButton, plusButton, alarmText, alarmButton
        )

        val anims = animatedViews.map { ObjectAnimator.ofFloat(it, View.ALPHA, alphaTo) }
        val anim = AnimatorSet()

        anim.playTogether(anims)
        anim.duration = 300
        anim.start()

        for (view in animatedViews) view.isEnabled = enabled
    }

    private fun onSetDndFilter(newFilter: Int) {
        this.filter = newFilter
        config.defaultFilter = newFilter
    }

    private fun onSetDndMode(newMode: DndMode) {
        this.mode = newMode
        config.defaultMode = newMode.ordinal
    }

    private fun onSetDndTimerOption(newOption: Int) {
        if (mode != DndMode.TIMER) onSetDndMode(DndMode.TIMER)

        timerOption = newOption
        config.defaultTimerOption = newOption

        minusButton.isEnabled = newOption != 0
        plusButton.isEnabled = newOption != timerOptions.size-1

        val (unit, count) = timerOptions[newOption]
        val timeStr = resources.getQuantityString(if (unit == MINUTES) R.plurals.minute else R.plurals.hour, count, count)
        val str = getString(R.string.dndOptionTimer, timeStr)

        val untilTime = System.currentTimeMillis() + unit.toMillis(count.toLong())
        val untilStr = timeFormat.format(untilTime)
        val timerUntilStr = getString(R.string.dndUntil, untilStr)

        timerText.text = str
        timerUntilText.text = timerUntilStr
    }

    private fun onSetAlarmTime(alarmTime: Long?) {
        this.alarmTime = alarmTime

        if (alarmTime != null) {
            val timeStr = alarmFormatter.format(alarmTime)
            val alarmStr = getString(R.string.dndUntil, timeStr)
            alarmText.text = alarmStr
        } else {
            alarmButton.visibility = View.GONE
            alarmText.visibility = View.GONE
        }
    }

    private fun onDoneClicked() {
        val dndConfig = when {
            !dndEnabled -> DndConfig.None
            mode == DndMode.MANUAL -> DndConfig.Manual(filter)
            mode == DndMode.ALARM -> DndConfig.Alarm(alarmTime!!, filter)
            mode == DndMode.TIMER -> {
                val (unit, units) = timerOptions[timerOption]
                DndConfig.Timer(units.toLong(), unit, filter)
            }
            else -> throw IllegalStateException()
        }

        applyDndConfig(dndConfig)
        animateClipOut(background, container, ::finish)
    }
}
