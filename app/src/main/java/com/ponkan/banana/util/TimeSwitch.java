package com.ponkan.banana.util;

import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 时间转换操作
 */

public class TimeSwitch {

    /**
     * 日期转换
     *
     * @param time 毫秒时间
     * @return 日期
     */
    public static String ToDate1(long time) {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date(time));
    }

    public static String ToDate2(long time) {
        return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(time));
    }

    /**
     * 时间转换
     *
     * @param time 毫秒时间
     * @return 日期时间
     */
    public static String ToDateAccurateToMinute(long time) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(time));
    }

    /**
     * 日期转换成星期
     *
     * @param date 日期
     * @param week 星期
     * @return 星期
     */
    public static String DateChangeToWeek(String date) {
        String week = null;
        try {
            week = new SimpleDateFormat("E").format(new SimpleDateFormat("yyyy-MM-dd").parse(date));
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return week;
    }


    /**
     * 时间转为秒和毫秒
     *
     * @param time 时间
     * @return 秒:毫秒(00:00)
     */
    public static String toSecondMillisecond(long time) {
        String date = new SimpleDateFormat("ss.SS", Locale.ENGLISH).format(new Date(time));
        if (TextUtils.isEmpty(date)) {
            return "00.00";
        }

        if (date.length() > 5) {
            return date.substring(0, 5);
        }
        return date;
    }
}
