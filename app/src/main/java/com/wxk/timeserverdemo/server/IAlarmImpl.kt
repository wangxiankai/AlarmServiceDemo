package com.wxk.timeserverdemo.server

import com.wxk.timeserverdemo.IAlarmAidlInterface


/**
 * Date: 2019/12/28
 *Author: wangxiankai
 *Description: AIDL服务
 */
class IAlarmImpl : IAlarmAidlInterface.Stub() {

    override fun getServiceName(): String {
        return android.os.Process.myPid().toString()
    }

}