package com.ysyy.rxdownloader.data.utils;

import com.orhanobut.logger.Logger;
import com.ysyy.rxdownloader.BuildConfig;

/**
 * Auther: RyanLi
 * Data: 2018-06-06 17:47
 * Description:  日志打印工具类
 */
public class LogUtils {
    public static void i(String msg) {
        if (BuildConfig.DEBUG)
            Logger.i(msg);
    }

    public static void d(String msg) {
        if (BuildConfig.DEBUG)
            Logger.d(msg);
    }

    public static void e(String msg) {
        if (BuildConfig.DEBUG)
            Logger.e(msg);
    }

    public static void json(String jsonStr) {
        if (BuildConfig.DEBUG)
            Logger.json(jsonStr);
    }

}
