package com.wxk.timeserverdemo.server

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Message
import android.util.Log
import androidx.annotation.RequiresApi
import java.lang.ref.WeakReference

/**
 * Date: 2020/1/10
 *Author: wangxiankai
 *Description:
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class JobSchedulerService : JobService() {

    private val MESSAGE_ID_TASK = 0x01

    private val handler = WorkHandler(this)

    override fun onStopJob(params: JobParameters?): Boolean {
        Log.i("LogUtils","JobSchedulerService onStopJob")
        handler.removeMessages(MESSAGE_ID_TASK)
        return false
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        Log.i("LogUtils","JobSchedulerService onStartJob")
        val message = Message.obtain()
        message.obj = params
        message.what = MESSAGE_ID_TASK
        handler.sendMessage(message)
        return true
    }

    class WorkHandler : Handler {

        private lateinit var mWeakRef: WeakReference<JobSchedulerService>

        constructor(jobSchedulerService: JobSchedulerService){
            this.mWeakRef = WeakReference(jobSchedulerService)
        }

        override fun handleMessage(msg: Message) {
            Log.i("LogUtils", "JobSchedulerService handleMessage")
            if (AlarmClockManager.instance.getAlarmClockStatus() == 1){
                // 开启闹钟服务
                val alarmIntent = Intent(mWeakRef.get(), AlarmService::class.java)
                alarmIntent.action = "com.wxk.timeserverdemo.alarm_service"
                mWeakRef.get()?.startService(alarmIntent)
                //开启远程服务
                val remoteIntent = Intent(mWeakRef.get(), RemoteService::class.java)
                remoteIntent.action = "com.wxk.timeserverdemo.remote_service"
                mWeakRef.get()?.startService(remoteIntent)
            } else ServiceManager.instance.cancelJobScheduler(mWeakRef.get()!!)
            // 通知系统当前任务已完成
            mWeakRef.get()!!.jobFinished(msg.obj as JobParameters, false)
        }
    }
}