package com.wxk.timeserverdemo

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatCheckBox
import com.wxk.timeserverdemo.server.Alarm
import com.wxk.timeserverdemo.server.ServiceManager
import com.wxk.timeserverdemo.server.WordService
import com.wxk.timeserverdemo.util.Saver

class MainActivity : AppCompatActivity() {

    private var clockStatus: Int = 0
    private lateinit var clockCB: AppCompatCheckBox
    private lateinit var sysCB: AppCompatCheckBox
    private lateinit var dateView: EditText
    private lateinit var minuteView: EditText
    private lateinit var hourView: EditText
    private lateinit var idView: EditText
    private var mVibrator: Vibrator? = null
    private var mWakelock: PowerManager.WakeLock? = null
    private var mAlarmClockList = ArrayList<Alarm>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()

        initListener()

        Handler().postDelayed({
            initAlarmClock(intent)
        }, 2000)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.i("LogUtils","!MainActivity onNewIntent")
        initAlarmClock(intent)
    }

    private fun initView() {
        idView = findViewById<EditText>(R.id.et_id)
        hourView = findViewById<EditText>(R.id.et_hour)
        minuteView = findViewById<EditText>(R.id.et_minute)
        dateView = findViewById<EditText>(R.id.et_date)
        sysCB = findViewById<AppCompatCheckBox>(R.id.sys_clock)
        clockCB = findViewById<AppCompatCheckBox>(R.id.open_clock)
    }

    private fun initListener() {
        sysCB.setOnClickListener {
            Log.i("LogUtils", "是否打开手机同步 ： ${sysCB.isChecked}")
            Saver.saveAlarmPhoneSynStatus(if(sysCB.isChecked)1 else 0)
            ServiceManager.instance.isIgnoreBatteryOption(this)

            Handler().postDelayed({
                ServiceManager.instance.startAlarmService(this.applicationContext)
                ServiceManager.instance.startRemoteService(this.applicationContext)
                ServiceManager.instance.startJobSchedulerService(this.applicationContext)
                ServiceManager.instance.registerReceiver(this.applicationContext)
            }, 1000)
        }

        clockCB.setOnClickListener {
            Log.i("LogUtils", "是否打开闹钟 ： ${clockCB.isChecked}")
            clockStatus = if (clockCB.isChecked)1 else 0
            if (mAlarmClockList.isNotEmpty()){
                for (alarm in mAlarmClockList){
                    if (alarm.id.toString() == idView.text.toString().trim()){
                        alarm.status = clockStatus
                    }
                }
                Saver.saveAlarmClockList(mAlarmClockList)
            }
        }

        findViewById<TextView>(R.id.add).setOnClickListener {

            val id = idView.text.toString().trim()
            val hour = hourView.text.toString().trim()
            val minute = minuteView.text.toString().trim()
            val date = dateView.text.toString().trim()

            if (id.isEmpty()){
                Toast.makeText(this, "请输入闹钟id", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (hour.isEmpty() || hour.toInt() > 24 || hour.toInt() < 0){
                Toast.makeText(this, "请正确输入闹钟小时", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (minute.isEmpty() || minute.toInt() > 60 || minute.toInt() < 0){
                Toast.makeText(this, "请正确输入闹钟分钟", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (date.isEmpty()){
                Toast.makeText(this, "请输入闹钟重复时间", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            var isExist = false
            if (mAlarmClockList.isNotEmpty()){
                for (alarm in mAlarmClockList){
                    if (alarm.id.toString() == id){
                        isExist = true
                        alarm.hour = hour.toInt()
                        alarm.minute = minute.toInt()
                        alarm.time = date
                        alarm.status = clockStatus
                    }
                }
            }
            if (!isExist){
                val alarm = Alarm(id.toInt(), hour.toInt(), minute.toInt(), date, clockStatus)
                mAlarmClockList.add(alarm)
            }
            Saver.saveAlarmClockList(mAlarmClockList)
        }

        findViewById<TextView>(R.id.delete).setOnClickListener {
            val id = idView.text.toString().trim()
            if (mAlarmClockList.isNotEmpty()){
                val iterator = mAlarmClockList.iterator()
                while (iterator.hasNext()){
                    val alarm = iterator.next()
                    if (alarm.id.toString() == id){
                        iterator.remove()
                    }
                }
            }
            Saver.saveAlarmClockList(mAlarmClockList)
        }
    }

    private fun initAlarmClock(intent: Intent?) {
        Log.i("LogUtils","发送闹钟提醒事件initAlarmClock() isAlarmClock: " + intent?.getBooleanExtra("isAlarmClock", false))
        if (intent?.getBooleanExtra("isAlarmClock", false) == true) {
            acquireWakeLock(this)
            startVibrator(this)
            Handler().postDelayed({
                mVibrator?.cancel()
            }, 20000)
        }
    }

    /**
     * 震动 - 想设置震动大小可以通过改变pattern来设定，如果开启时间太短，震动效果可能感觉不到
     */
    private fun startVibrator(context: Context) {
        if (mVibrator == null)
            mVibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (mVibrator != null) {
            val pattern = longArrayOf(500, 1000, 500, 1000) // 停止 开启 停止 开启
            mVibrator?.vibrate(pattern, 0)
        }
    }


    /**
     * 唤醒屏幕
     */
    @SuppressLint("InvalidWakeLockTag", "WakelockTimeout")
    private fun acquireWakeLock(context: Context) {
        if (mWakelock == null) {
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            if (pm != null) {
                mWakelock = pm.newWakeLock(
                    PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.SCREEN_DIM_WAKE_LOCK,
                    "alarm"
                )
                //五分钟内可以有效
                mWakelock?.acquire(5 * 60 * 1000L /*5 minutes*/)
                mWakelock?.release()
            }
        }
    }
}
