package com.wxk.timeserverdemo.server

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.wxk.timeserverdemo.IAlarmAidlInterface


/**
 * Date: 2019/12/28
 *Author: wangxiankai
 *Description: 远程服务，用于与闹钟服务AlarmService，进行双进程保护。
 *  1、开启两个不同进程的服务，android:process。
 *  2、在服务启动之后，绑定两一个服务。
 */
class RemoteService : Service() {

    private var startArgFlags: Int = 0

    private var mContext: Context? = null

    override fun onBind(intent: Intent?): IBinder? {
        return IAlarmImpl()
    }

    override fun onCreate() {
        super.onCreate()
        this.mContext = this
        Log.i("LogUtils","远程服务 onCreate()")
        // 保证内存不足，杀死会重新创建
        startArgFlags =
            if (applicationInfo.targetSdkVersion < Build.VERSION_CODES.ECLAIR)
                START_STICKY_COMPATIBILITY
            else START_STICKY
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("LogUtils","远程服务 onStartCommand()")

        if (AlarmClockManager.instance.getAlarmClockStatus() == 1){
            //绑定闹钟服务
            bindAlarmService()

            //开启前置服务
        } else {
            closeAlarm()
        }
        return super.onStartCommand(intent, startArgFlags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("LogUtils","远程服务 onDestroy()")
        if (AlarmClockManager.instance.getAlarmClockStatus() == 1){
            //启动闹钟服务
            startAlarmService()
            //绑定闹钟服务
            bindAlarmService()
        }
    }

    /**
     * 创建 ServiceConnection 类型的对象实例，在后面绑定服务时会用到
     */
    private val mServiceConnection = object : ServiceConnection {

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.i("LogUtils","远程服务 onServiceDisconnected()")
            //服务断开可以进行 服务再次连接与绑定
            if (AlarmClockManager.instance.getAlarmClockStatus() == 1){
                // 启动闹钟服务
                startAlarmService()
                // 绑定闹钟服务
                bindAlarmService()
            }
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.i("LogUtils","远程服务 onServiceConnected()")
            val alarmImpl = IAlarmAidlInterface.Stub.asInterface(service)
            Log.i("LogUtils","远程服务 onServiceConnected() >>> process id ：${alarmImpl?.serviceName}")
        }
    }

    /**
     * 开启闹钟服务
     */
    private fun startAlarmService(){
        val intent = Intent(this, AlarmService::class.java)
        intent.action = "com.wxk.timeserverdemo.alarm_service"
        startService(intent)
    }

    /**
     * 绑定闹钟服务
     */
    private fun bindAlarmService(){
        val intent = Intent(this, AlarmService::class.java)
        intent.action = "com.wxk.timeserverdemo.alarm_service"
        bindService(intent, mServiceConnection, Context.BIND_IMPORTANT)
    }

    /**
     * 关闭远程服务
     */
    private fun stopAlarmService() {
        val intent = Intent(this, RemoteService::class.java)
        intent.action = "com.wxk.timeserverdemo.remote_service"
        stopService(intent)
    }

    /**
     * 解绑远程服务
     */
    private fun unBindAlarmService() {
        if (AlarmClockManager.instance.getAlarmClockStatus() == 0){
            unbindService(mServiceConnection)
        }
    }

    /**
     * 关闭远程服务
     */
    private fun closeAlarm(){
        Log.i("LogUtils","关闭远程服务 closeAlarm()")

        //关闭前置服务

        stopAlarmService()
        unBindAlarmService()

        //关闭当前闹钟服务
        stopSelf()
    }
}