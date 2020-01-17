package com.wxk.timeserverdemo.util

import java.util.*


/**
 * Date: 2020/1/10
 *Author: wangxiankai
 *Description: 时间工具类
 */
class TimeUtils {

    companion object {

        //星期数据列表
        private val DAY = arrayOf<Int>(-1, 7, 1, 2, 3, 4, 5, 6)

        /**
         * 获取年
         * @return
         */
        fun getYear(): Int {
            val cd = getCalendar()
            return cd.get(Calendar.YEAR)
        }

        /**
         * 获取月
         * @return
         */
        fun getMonth(): Int {
            val cd = getCalendar()
            return cd.get(Calendar.MONTH) + 1
        }

        /**
         * 获取日
         * @return
         */
        fun getDay(): Int {
            val cd = getCalendar()
            return cd.get(Calendar.DATE)
        }

        /**
         * 获取时
         * @return
         */
        fun getHour(): Int {
            val cd = getCalendar()
            return cd.get(Calendar.HOUR_OF_DAY)
        }

        /**
         * 获取分
         * @return
         */
        fun getMinute(): Int {
            val cd = getCalendar()
            return cd.get(Calendar.MINUTE)
        }

        /**
         * 获取今天是周几
         */
        fun getWeekDay() : Int {
            val date = Date()
            val cd = getCalendar()
            cd.time = date
            return DAY[cd.get(Calendar.DAY_OF_WEEK)]
        }

        /**
         * 日历
         */
        fun getCalendar(): Calendar{
            val cd = Calendar.getInstance()
            cd.timeZone = TimeZone.getTimeZone("GMT+8")
            return cd
        }
    }
}