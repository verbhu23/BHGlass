package com.biomhope.glass.face.utils;

import android.util.Log;

import com.biomhope.glass.face.BuildConfig;

/**
 * @author $USER_NAME
 * create at : 2018/9/10
 * description :
 */
public class LogUtil {

    private static boolean isDebug = BuildConfig.LOG;

    public static void v(String tag, String msg) {
        if (isDebug) {
            Log.v(tag, getMsgFormat(msg));
        }
    }

    public static void d(String tag, String msg) {
        if (isDebug) {
            Log.d(tag, getMsgFormat(msg));
        }
    }

    public static void i(String tag, String msg) {
        if (isDebug) {
            Log.i(tag, getMsgFormat(msg));
        }
    }

    public static void w(String tag, String msg) {
        if (isDebug) {
            Log.w(tag, getMsgFormat(msg));
        }
    }

    public static void e(String tag, String msg) {
        if (isDebug) {
            Log.e(tag, getMsgFormat(msg));
        }
    }

    /**
     * 输出格式定义
     */
    private static String getMsgFormat(String msg) {
        return msg + "\n" + getFunctionName();
//        return msg;
    }


    private static String getFunctionName() {
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();
        if (sts != null) {
            for (StackTraceElement st : sts) {
                if (st.isNativeMethod()) {
                    continue;
                }
                if (st.getClassName().equals(Thread.class.getName())) {
                    continue;
                }
                if (st.getClassName().equals(LogUtil.class.getName())) {
                    continue;
                }
                return "[ " + st.getClassName() + "." + st.getMethodName()
                        + "(" + st.getFileName() + ":" + st.getLineNumber() + ")" + " ]";
            }
        }
        return null;
    }

}
