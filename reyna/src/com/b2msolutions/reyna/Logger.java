package com.b2msolutions.reyna;

import android.util.Log;

public class Logger {

    private int level = Log.INFO;

    public Logger(int level) {
        this.level = level;
    }

    public int v(String tag, String msg) {

        if (!this.IsValid(Log.VERBOSE, tag, msg)) {
            return 0;
        }

        return Log.v(tag, msg);
    }

    public int v(String tag, String msg, Throwable tr) {

        if (!this.IsValid(Log.VERBOSE, tag, msg)) {
            return 0;
        }

        return Log.v(tag, msg, tr);
    }

    public int d(String tag, String msg) {

        if (!this.IsValid(Log.DEBUG, tag, msg)) {
            return 0;
        }

        return Log.d(tag, msg);
    }

    public int d(String tag, String msg, Throwable tr) {
        if (!this.IsValid(Log.DEBUG, tag, msg)) {
            return 0;
        }

        return Log.d(tag, msg, tr);
    }

    public int i(String tag, String msg) {
        if (!this.IsValid(Log.INFO, tag, msg)) {
            return 0;
        }

        return Log.i(tag, msg);
    }

    public int i(String tag, String msg, Throwable tr) {
        if (!this.IsValid(Log.INFO, tag, msg)) {
            return 0;
        }

        return Log.i(tag, msg, tr);
    }

    public int w(String tag, String msg) {
        if (!this.IsValid(Log.WARN, tag, msg)) {
            return 0;
        }

        return Log.w(tag, msg);
    }

    public int w(String tag, String msg, Throwable tr) {
        if (!this.IsValid(Log.WARN, tag, msg)) {
            return 0;
        }

        return Log.w(tag, msg, tr);
    }

    public int w(String tag, Throwable tr) {
        if (!this.IsValid(Log.WARN, tag)) {
            return 0;
        }

        return Log.w(tag, tr);
    }

    public int e(String tag, String msg) {
        if (!this.IsValid(Log.ERROR, tag, msg)) {
            return 0;
        }

        return Log.e(tag, msg);
    }

    public int e(String tag, String msg, Throwable tr) {
        if (!this.IsValid(Log.ERROR, tag, msg)) {
            return 0;
        }

        return Log.e(tag, msg, tr);
    }

    private boolean IsValid(int level, String tag, String msg) {

        if (level < this.level) {
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

    private boolean IsValid(int level, String tag) {

        if (level < this.level) {
            return false;
        }

        if (tag == null) {
            return false;
        }

        return true;
    }
}
