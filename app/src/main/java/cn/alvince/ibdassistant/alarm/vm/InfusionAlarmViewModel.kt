package cn.alvince.ibdassistant.alarm.vm

import android.content.Context
import android.os.SystemClock
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Worker
import androidx.work.WorkerParameters
import cn.alvince.ibdassistant.alarm.AlarmInstrumentation
import cn.alvince.zanpakuto.core.time.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Infusion Alarm controller [ViewModel]
 *
 * Create by ZhangYang on 2024/8/21
 *
 * @author zhangyang.alvince@bytedance.com
 */
class InfusionAlarmViewModel : ViewModel() {

    val startedAlarm = mutableStateOf(false)

    val time = mutableStateOf("12:00:00")
    val countDownTime = mutableStateOf("00:00:00")
    val speed = mutableIntStateOf(0)

    private val isAlarmStarted: Boolean get() = startedAlarm.value

    private val infusionSpeed = intArrayOf(20, 40, 80, 150, 250) // 类克的调速
    private var curInfusionSpeedCursor = -1

    private var nextAlarmTime: Long = 0L
    private var updateTimeJob: Job? = null

    fun attachToLifecycle(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                when (event) {
                    Lifecycle.Event.ON_RESUME -> {
                        updateClock()
                        startUpdateTimeSync()
                    }

                    Lifecycle.Event.ON_PAUSE -> stopUpdateTime()
                    Lifecycle.Event.ON_DESTROY -> {
                        source.lifecycle.removeObserver(this)
                    }

                    else -> {
                        // do-nothing
                    }
                }
            }
        })
    }

    fun startAlarmOrNext() {
        val maxCursor = infusionSpeed.lastIndex
        val cursor = ++curInfusionSpeedCursor
        if (cursor >= maxCursor) {
            // stop alarm
            startedAlarm.value = false
            return
        }
        val interval = if (cursor == maxCursor - 1) 30 else 15
/*
        PeriodicWorkRequestBuilder<AlarmWorker>(interval.toLong(), TimeUnit.MINUTES).also {
            WorkManager.getInstance()
                .enqueueUniquePeriodicWork("", ExistingPeriodicWorkPolicy.KEEP, it.build())
        }
*/
        startedAlarm.value = true
        nextAlarmTime = SystemClock.elapsedRealtime() + interval.minutes.inWholeMilliseconds
        speed.intValue = infusionSpeed.getOrElse(cursor) { 20 }
        updateCountDownTime()
    }

    fun stopAlarm() {
        startedAlarm.value = false
    }

    private fun startUpdateTimeSync() {
        updateTimeJob = viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                delay(1.seconds)
                updateClock()
                if (isAlarmStarted) {
                    updateCountDownTime()
                }
            }
        }
    }

    private fun stopUpdateTime() {
        updateTimeJob?.cancel("")
    }

    private fun updateClock() {
        getCurrentTimeDisplay {
            time.value = it
        }
    }

    private fun updateCountDownTime() {
        val now = SystemClock.elapsedRealtime()
        if (nextAlarmTime <= now) {
            return
        }
        (nextAlarmTime - now).milliseconds.toComponents { hours, minutes, seconds, _ ->
            countDownTime.value = "%02d:%02d:%02d".format(hours, minutes, seconds)
        }
    }

    private inline fun getCurrentTimeDisplay(block: (String) -> Unit) {
        Timestamp.now().toComponents { _, _, _, hours, minutes, seconds, _, _ ->
            block("%02d:%02d:%02d".format(hours, minutes, seconds))
        }
    }
}

class AlarmWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        AlarmInstrumentation.alarmReached()
        return Result.success()
    }
}
