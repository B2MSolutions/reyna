package com.b2msolutions.reyna.system;

import android.content.Context;

public class PeriodicBackoutCheck {
    private static String TAG = "com.b2msolutions.reyna.system.PeriodicBackoutCheck";

    protected Preferences preferences;

    public PeriodicBackoutCheck(Context context) {
        Logger.v(TAG, "PeriodicBackoutCheck");

        this.preferences = new Preferences(context);
    }

    public void record(String task) {
        Logger.v(TAG, String.format("record, task %s", task));

        this.preferences.putLong(task, System.currentTimeMillis());
    }

    public boolean timeElapsed(String task, long interval) {
        Logger.v(TAG, "timeElapsed");

        long lastRun = this.preferences.getLong(task, -1);

        if (lastRun > System.currentTimeMillis()) {
            Logger.w(TAG, String.format("lastRun in future, %d, current %d", lastRun, System.currentTimeMillis()));

            this.record(task);
            return true;
        }

        return System.currentTimeMillis() - lastRun >= interval;
    }

    public long getLastRecordedTime(String task) {
        Logger.v(TAG, String.format("getLastRecorded, task %s", task));

        return this.preferences.getLong(task, -1);
    }
}
