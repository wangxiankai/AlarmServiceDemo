package com.wxk.timeserverdemo.server

import java.io.Serializable

/**
 * Date: 2019/12/28
 *Author: wangxiankai
 *Description:  闹钟对象
 */
data class Alarm(
    var id: Int,       //id
    var hour: Int,     //小时
    var minute: Int,       //分钟
    var time: String,   //时间
    var status: Int     //开启闹钟 0 关闭  1开启
) : Serializable