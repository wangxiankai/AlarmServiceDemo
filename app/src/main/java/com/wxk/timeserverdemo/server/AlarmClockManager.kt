package com.wxk.timeserverdemo.server

import android.content.Context
import android.content.Intent
import android.util.Log
import com.wxk.timeserverdemo.MainActivity
import com.wxk.timeserverdemo.util.Saver
import java.util.*
import kotlin.collections.ArrayList

/**
 * Date: 2020/1/10
 *Author: wangxiankai
 *Description: 闹钟管理类
 */
class AlarmClockManager private constructor() {

    //单例  双重校验锁式（Double Check)
    companion object {
        val instance: AlarmClockManager by lazy (mode = LazyThreadSafetyMode.SYNCHRONIZED){
            AlarmClockManager()
        }
    }

    /**
     * 上下文
     */
    private var mContext: Context? = null
    //日历
    private var mCalendar: Calendar? = null
    //闹钟同步手机状态 0关闭 1开启  当为0状态时 所有闹钟包括服务都得清除
    private var mAlarmClockStatus: Int = -1
    //星期数据列表
    private val DAY = arrayOf<Int>(-1, 7, 1, 2, 3, 4, 5, 6)
    //有效分钟
    private val VALID_MINUTE = 1

    /**
     * 初始化
     */
    fun init(context: Context){
        this.mContext = context
        mCalendar = Calendar.getInstance()
        mCalendar?.timeZone = TimeZone.getTimeZone("GMT+8")
    }

    /**
     * 闹钟处理
     */
    fun handleAlarm(){
        //读取闹钟数据
        val alarmClockList = getAlarmClockFromSP()
        //闹钟处理
        if (alarmClockList.isNotEmpty()){
            for (alarm in alarmClockList){
                handleAlarm(alarm)
            }
        }
    }

    /**
     * 处理每个闹钟
     */
    private fun handleAlarm(alarm: Alarm){
        //判断是否打开闹钟手机同步
        if (getAlarmClockStatus() != 1){
            return
        }

        //是否开启闹钟
        if (alarm.status == 0){
            return
        }

        //处理周几可以打开闹钟
        Log.i("LogUtils", "今天是周 ： " + getWeekDay())
        Log.i("LogUtils","当前闹钟id是 ： " + alarm.id)
        if (alarm.time.isNotEmpty() &&
            (alarm.time.contains(",") || alarm.time.length == 1)){
            if (!alarm.time.contains(getWeekDay().toString())){
                Log.i("LogUtils","今天闹钟不开哦")
                return
            }
        } else {
            if (!alarm.time.contains("不重复")){
                Log.i("LogUtils","日期不符合规范")
                return
            }
        }

        //当前小时
        val currentHour = getHour()
        Log.i("LogUtils","当前小时$currentHour")
        //当前分钟
        val currentMinute = getMinute()
        Log.i("LogUtils","当前分钟$currentMinute")

        if (currentHour != alarm.hour){
            return
        }

        //3分钟之内有效
        if (currentMinute >= alarm.minute && currentMinute <= (alarm.minute + VALID_MINUTE)){
            handleRemind(alarm)
        }
    }

    /**
     * 闹钟提醒
     */
    private fun handleRemind(alarm: Alarm) {
        Log.i("LogUtils","闹钟响了 id： " + alarm.id)
        Log.i("LogUtils","闹钟响了 时： " + alarm.hour)
        Log.i("LogUtils","闹钟响了 分： " + alarm.minute)
        val rIntent = Intent(mContext, MainActivity::class.java)
        rIntent.putExtra("isAlarmClock", true)
            .putExtra("id", alarm.id)
            .putExtra("hour", alarm.hour)
            .putExtra("minute", alarm.minute)
        rIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        mContext?.startActivity(rIntent)
    }

    /**
     * 从SP读取数据
     */
    private fun getAlarmClockFromSP() : List<Alarm>{
        Log.i("LogUtils", "AlarmClockManager getAlarmClockFromSP")
        //读取sp数据 (流对象)
        val alarmClockList = Saver.getAlarmClockList()
        return alarmClockList?: ArrayList<Alarm>()
    }

    /**
     * 获取闹钟手机同步状态 0关闭 1开启 当为0状态时 所有闹钟包括服务都得清除
     */
    fun getAlarmClockStatus() : Int {
        //从sp获取
        mAlarmClockStatus = Saver.getAlarmPhoneSynStatus()
        return mAlarmClockStatus
    }

    /**
     * 获取今天是周几
     */
    private fun getWeekDay() : Int {
        val date = Date()
        return mCalendar?.let {
            it.time = date
            DAY[it.get(Calendar.DAY_OF_WEEK)]
        } ?: -1
    }

    /**
     * 获取时
     * @return
     */
    private fun getHour(): Int {
        return mCalendar?.get(Calendar.HOUR_OF_DAY)?:-1
    }

    /**
     * 获取分
     * @return
     */
    private fun getMinute(): Int {
        return mCalendar?.get(Calendar.MINUTE)?:-1
    }

    /**
     * 获取年
     * @return
     */
    private fun getYear(): Int {
        return mCalendar?.get(Calendar.YEAR)?:-1
    }

    /**
     * 获取月
     * @return
     */
    private fun getMonth(): Int {
        return mCalendar?.let {
            it.get(Calendar.MONTH) + 1
        }?:-1
    }

    /**
     * 获取日
     * @return
     */
    private fun getDay(): Int {
        return mCalendar?.get(Calendar.DATE)?:-1
    }
}