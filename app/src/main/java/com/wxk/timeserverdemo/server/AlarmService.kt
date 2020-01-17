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
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

/**
 * Date: 2020/1/10
 *Author: wangxiankai
 *Description: 后台推送服务
 */
class AlarmService : Service(){

    private var disposable: Disposable? = null

    private var startArgFlags: Int = 0

    override fun onBind(p0: Intent?): IBinder? {
        return IAlarmImpl()
    }

    override fun onCreate() {
        super.onCreate()
        Log.i("LogUtils","闹钟服务 onCreate()")
        init()
    }

    private fun init() {
        // 保证内存不足，杀死会重新创建
        startArgFlags =
            if (applicationInfo.targetSdkVersion < Build.VERSION_CODES.ECLAIR)
                START_STICKY_COMPATIBILITY
            else START_STICKY
        //开始定时
        disposable?:start()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("LogUtils","闹钟服务 onStartCommand()")
        //判断是否开启手机同步
        if (AlarmClockManager.instance.getAlarmClockStatus() == 1){
            //开启闹钟服务提醒
            //绑定远程服务
            bindRemoteService()
            //闹钟设置逻辑
            disposable?:start()
        } else {
            // 关闭闹钟服务
            closeAlarm()
        }
        return super.onStartCommand(intent, startArgFlags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("LogUtils","闹钟服务 onDestroy()")
        //为了防止服务被杀  需要重新开启服务
        if (AlarmClockManager.instance.getAlarmClockStatus() == 1){
            //开启闹钟服务
            startService(Intent(this, AlarmService::class.java))
            //开启远程服务
            startRemoteService()
            //绑定远程服务
            bindRemoteService()
        }
    }

    /**
     * 创建 ServiceConnection 类型的对象实例，在后面绑定服务时会用到
     */
    private val mServiceConnection = object : ServiceConnection {

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.i("LogUtils","闹钟服务 onServiceDisconnected()")
            //服务断开可以进行 服务再次连接与绑定
            if (AlarmClockManager.instance.getAlarmClockStatus() == 1){
                // 启动远程服务
                startRemoteService()
                // 绑定远程服务
                bindRemoteService()
            }
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.i("LogUtils","闹钟服务 onServiceConnected()")
            val alarmImpl = IAlarmAidlInterface.Stub.asInterface(service)
            Log.i("LogUtils","闹钟服务 onServiceConnected() >>> process id ：${alarmImpl?.serviceName}")
        }
    }

    /**
     * 开始定时
     */
    private fun start(){
        disposable =
            Observable
                .interval(60, TimeUnit.SECONDS)
                .map { System.currentTimeMillis() }
                .subscribe {
                    Log.i("LogUtils", "定时器执行 ： $it")
                    //工作服务
                    val intent = Intent(this, WordService::class.java)
                    intent.action = "com.wxk.timeserverdemo.word_service"
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(intent)
                    } else {
                        startService(intent)
                    }
                }
    }

    /**
     * 定时停止
     */
    private fun stop(){
        disposable?.dispose()
    }

    /**
     * 关闭闹钟服务
     */
    private fun closeAlarm(){
        Log.i("LogUtils", "关闭闹钟服务 closeAlarm()")
        //关闭闹钟
        stop()

        //关闭远程服务
        stopRemoteService()
        unBindRemoteService()

        //关闭当前闹钟服务
        stopSelf()
    }

    /**
     * 启动远程服务
     */
    private fun startRemoteService(){
        val intent = Intent(this, RemoteService::class.java)
        intent.action = "com.wxk.timeserverdemo.remote_service"
        startService(intent)
    }

    /**
     * 绑定远程服务
     */
    private fun bindRemoteService() {
        val intent = Intent(this, RemoteService::class.java)
        intent.action = "com.wxk.timeserverdemo.remote_service"
        bindService(intent, mServiceConnection, Context.BIND_IMPORTANT)
    }

    /**
     * 关闭远程服务
     */
    private fun stopRemoteService() {
        val intent = Intent(this, RemoteService::class.java)
        intent.action = "com.wxk.timeserverdemo.remote_service"
        stopService(intent)
    }

    /**
     * 解绑远程服务
     */
    private fun unBindRemoteService() {
        if (AlarmClockManager.instance.getAlarmClockStatus() == 0){
            unbindService(mServiceConnection)
        }
    }
}
