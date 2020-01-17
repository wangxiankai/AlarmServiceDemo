package com.wxk.timeserverdemo.util

import android.content.Context
import android.content.SharedPreferences
import com.wxk.timeserverdemo.server.Alarm
import com.wxk.timeserverdemo.sp.PreferenceUtil

/**
 * Date: 2020/1/11
 *Author: wangxiankai
 *Description:
 */
object Saver {

    lateinit var preferences: SharedPreferences

    /**
     * 可跨进程的存储SP
     */
    val PREFERENCES_NAME = "preferences_name"

    fun init(context: Context) {
        //需要在Application里初始化
        preferences = PreferenceUtil.getSharedPreference(context, PREFERENCES_NAME)
    }


    /**
     *  存储后台运行权限状态  0未给  1已给
     */
    fun saveBackgroundRunPermissionStatus(status: Int){
        val edit = preferences.edit()
        edit.putInt("background_run_permission_status", status)
        edit.apply()
    }

    /**
     * 读取sp存储后台运行权限状态  0未给  1已给
     */
    fun getBackgroundRunPermissionStatus() : Int{
        return preferences.getInt("background_run_permission_status", 0)
    }

    /**
     * 存储闹钟手机同步提醒状态    0未开启  1已开启
     */
    fun saveAlarmPhoneSynStatus(status: Int){
        val edit = preferences.edit()
        edit.putInt("alarm_phone_syn_status", status)
        edit.apply()
    }

    /**
     * 读取sp存储闹钟手机同步提醒状态    0未开启  1已开启
     */
    fun getAlarmPhoneSynStatus() : Int{
        return preferences.getInt("alarm_phone_syn_status", 0)
    }

    /**
     * 存储闹钟数据
     */
    fun saveAlarmClockList(data: List<Alarm>) {
        val edit = preferences.edit()
        edit.putString("alarm_clock_list", EnCodeUtil.objectEncode(data))
        edit.apply()
    }

    /**
     * 获取存储闹钟数据
     */
    fun getAlarmClockList(): List<Alarm>? {
        val base64Publish = preferences.getString("alarm_clock_list", "")
        return EnCodeUtil.objectDecode(base64Publish)
    }
}