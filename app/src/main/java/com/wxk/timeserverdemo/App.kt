package com.wxk.timeserverdemo

import android.app.Application
import com.fanjun.keeplive.KeepLive
import com.fanjun.keeplive.config.ForegroundNotification
import com.fanjun.keeplive.config.KeepLiveService
import com.wxk.timeserverdemo.server.ServiceManager
import com.wxk.timeserverdemo.util.Saver

/**
 * Date: 2020/1/11
 *Author: wangxiankai
 *Description:
 */
class App : Application(){

    override fun onCreate() {
        super.onCreate()
        val application = this
        Saver.init(this)
        KeepLive.startWork(this, KeepLive.RunMode.ROGUE,
            ForegroundNotification("闹钟", "闹钟", R.mipmap.ic_launcher),
            object : KeepLiveService{
                override fun onWorking() {
                    ServiceManager.instance.startAlarmService(application)
                }

                override fun onStop() {

                }

            }
        )
    }
}