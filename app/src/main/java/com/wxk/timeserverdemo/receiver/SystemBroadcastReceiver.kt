package com.wxk.timeserverdemo.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.wxk.timeserverdemo.server.AlarmClockManager
import com.wxk.timeserverdemo.server.ServiceManager

/**
 * Date: 2020/1/10
 *Author: wangxiankai
 *Description: 系统广告通知类
 */
class SystemBroadcastReceiver : BroadcastReceiver(){

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i("LogUtils","系统广告通知类 SystemBroadcastReceiver 调用")
        context?.let {
            if (AlarmClockManager.instance.getAlarmClockStatus() == 1){
                // 开启闹钟服务
                ServiceManager.instance.startAlarmService(it)
                // 开启远程服务
                ServiceManager.instance.startRemoteService(it)
            }
        }
    }
}