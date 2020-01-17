package com.wxk.timeserverdemo.server

import android.app.IntentService
import android.content.Intent

/**
 * Date: 2020/1/10
 *Author: wangxiankai
 *Description: 工作后台
 */
class WordService : IntentService {

    constructor() : super("alarm_clock")

    constructor(name: String) : super("alarm_clock")

    override fun onCreate() {
        super.onCreate()
        //初始化
        AlarmClockManager.instance.init(this)
    }

    override fun onHandleIntent(intent: Intent?) {
        //闹钟处理
        AlarmClockManager.instance.handleAlarm()
    }
}

