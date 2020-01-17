package com.wxk.timeserverdemo.server

import android.annotation.SuppressLint
import android.app.Activity
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import com.wxk.timeserverdemo.receiver.SystemBroadcastReceiver

/**
 * Date: 2020/1/10
 *Author: wangxiankai
 *Description:  服务管理类
 */
class ServiceManager {

    //单例  双重校验锁式（Double Check)
    companion object {
        val instance: ServiceManager by lazy (mode = LazyThreadSafetyMode.SYNCHRONIZED){
            ServiceManager()
        }
    }

    private var sysBroadcastReceiver: SystemBroadcastReceiver? = null

    /**
     * 注册广播
     *
     * @param context 上下文对象
     */
    fun registerReceiver(context: Context) {
        // 开启系统广播
        if (sysBroadcastReceiver == null) {
            sysBroadcastReceiver = SystemBroadcastReceiver()
            val filter = IntentFilter()
            // 注册开机广播
            filter.addAction(Intent.ACTION_BOOT_COMPLETED)
            // 注册网络状态更新
            filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
            filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
            // 注册电池电量变化
            filter.addAction(Intent.ACTION_BATTERY_CHANGED)
            // 注册应用安装状态变化
            filter.addAction(Intent.ACTION_PACKAGE_ADDED)
            filter.addAction(Intent.ACTION_PACKAGE_REPLACED)
            filter.addAction(Intent.ACTION_PACKAGE_REMOVED)
            // 注册屏幕亮度变化广播
            filter.addAction(Intent.ACTION_SCREEN_OFF)
            filter.addAction(Intent.ACTION_SCREEN_ON)
            // 注册锁屏广播
            filter.addAction(Intent.ACTION_USER_PRESENT)
            val appContext = context.applicationContext
            appContext.registerReceiver(sysBroadcastReceiver, filter)
        }
    }

    /**
     * 取消广播
     *
     * @param context 上下文对象
     */
    fun unregisterReceiver(context: Context) {
        if (sysBroadcastReceiver != null) {
            val appContext = context.applicationContext
            appContext.unregisterReceiver(sysBroadcastReceiver)
            sysBroadcastReceiver = null
        }
    }

    /**
     * 开启闹钟服务
     */
    fun startAlarmService(context: Context){
        val intent = Intent(context, AlarmService::class.java)
        intent.action = "com.wxk.timeserverdemo.alarm_service"
        context.startService(intent)
    }

    /**
     * 开启远程服务
     */
    fun startRemoteService(context: Context){
        val intent = Intent(context, RemoteService::class.java)
        intent.action = "com.wxk.timeserverdemo.remote_service"
        context.startService(intent)
    }

    private val JOB_ID = 122

    /**
     * 开启JobSchedulerService，1分钟执行一次
     */
    fun startJobSchedulerService(context: Context){
        if (AlarmClockManager.instance.getAlarmClockStatus() == 1){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
                val builder = JobInfo.Builder(
                    JOB_ID,
                    ComponentName(context.packageName, JobSchedulerService::class.java.name)
                )
                // 每隔60秒运行一次
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    builder.setMinimumLatency((60 * 1000).toLong())// 小于15分钟的设置方案
                else builder.setPeriodic((60 * 1000).toLong())// 大于15分钟
                //builder.setOverrideDeadline(60 * 1000);// 执行的最长延时时间
                builder.setRequiresCharging(true)// 当插入充电器，执行该任务
                builder.setPersisted(true)  // 设置设备重启后，是否重新执行任务
                builder.setRequiresDeviceIdle(true)
                if (jobScheduler != null) {
                    val result = jobScheduler.schedule(builder.build())
                    if (result <= 0) {
                        // If something goes wrong
                        jobScheduler.cancel(JOB_ID)
                    }
                }
            }
        } else {
            cancelJobScheduler(context)
        }
    }

    /**
     * 取消JobScheduler
     */
    fun cancelJobScheduler(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            jobScheduler?.cancel(JOB_ID)
        }
    }


    private val REQUEST_IGNORE_BATTERY_CODE = 0X1115
    /**
     * 针对N以上的Doze模式
     *
     * @param activity 当前Fragment
     */
    @SuppressLint("BatteryLife")
    fun isIgnoreBatteryOption(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent()
                val packageName = activity.packageName
                val pm = activity.getSystemService(Context.POWER_SERVICE) as PowerManager
                if (!(pm != null && pm.isIgnoringBatteryOptimizations(packageName))) {
                    //                    intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                    intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                    intent.data = Uri.parse("package:$packageName")
                    activity.startActivityForResult(intent, REQUEST_IGNORE_BATTERY_CODE)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


}