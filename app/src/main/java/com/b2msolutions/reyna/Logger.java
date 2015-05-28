package com.b2msolutions.reyna;

import android.util.Log;

public class Logger {

    protected static int level = Log.INFO;

    public static void setLevel(int level)
    {
        Logger.level = level;
    }

    public static int getLevel()
    {
        return Logger.level;
    }

    public static int v(String tag, String msg) {

        if (!Logger.IsValid(Log.VERBOSE, tag, msg)) {
            return 0;
        }

        return Log.v(tag, msg);
    }

    public static int v(String tag, String msg, Throwable tr) {

        if (!Logger.IsValid(Log.VERBOSE, tag, msg)) {
            return 0;
        }

        return Log.v(tag, msg, tr);
    }

    public static int d(String tag, String msg) {

        if (!Logger.IsValid(Log.DEBUG, tag, msg)) {
            return 0;
        }

        return Log.d(tag, msg);
    }

    public static int d(String tag, String msg, Throwable tr) {
        if (!Logger.IsValid(Log.DEBUG, tag, msg)) {
            return 0;
        }

        return Log.d(tag, msg, tr);
    }

    public static int i(String tag, String msg) {
        if (!Logger.IsValid(Log.INFO, tag, msg)) {
            return 0;
        }

        return Log.i(tag, msg);
    }

    public static int i(String tag, String msg, Throwable tr) {
        if (!Logger.IsValid(Log.INFO, tag, msg)) {
            return 0;
        }

        return Log.i(tag, msg, tr);
    }

    public static int w(String tag, String msg) {
        if (!Logger.IsValid(Log.WARN, tag, msg)) {
            return 0;
        }

        return Log.w(tag, msg);
    }

    public static int w(String tag, String msg, Throwable tr) {
        if (!Logger.IsValid(Log.WARN, tag, msg)) {
            return 0;
        }

        return Log.w(tag, msg, tr);
    }

    public static int w(String tag, Throwable tr) {
        if (!Logger.IsValid(Log.WARN, tag)) {
            return 0;
        }

        return Log.w(tag, tr);
    }

    public static int e(String tag, String msg) {
        if (!Logger.IsValid(Log.ERROR, tag, msg)) {
            return 0;
        }

        return Log.e(tag, msg);
    }

    public static int e(String tag, String msg, Throwable tr) {
        if (!Logger.IsValid(Log.ERROR, tag, msg)) {
            return 0;
        }

        return Log.e(tag, msg, tr);
    }

    private static boolean IsValid(int level, String tag, String msg) {

        if (level < Logger.getLevel()) {
            return false;
        }

        if (msg == null) {
            return false;
        }

        if (tag == null) {
            return false;
        }

        return true;
    }

    private static boolean IsValid(int level, String tag) {

        if (level < Logger.getLevel()) {
            return false;
        }

        if (tag == null) {
            return false;
        }

        return true;
    }
}
