package com.b2msolutions.reyna;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {

    private final Context context;
    private final String FROM = "CELLULAR_DATA_BLACKOUT_FROM";
    private final String TO = "CELLULAR_DATA_BLACKOUT_TO";
    private final String STORAGE_SIZE = "STORAGE_SIZE";
    private final String WLAN_RANGE = "WLAN_RANGE";
    private final String WWAN_RANGE = "WWAN_RANGE";

    public Preferences(Context context) {
        this.context = context;
    }

    public void saveCellularDataBlackout(TimeRange timeRange) {
        if (timeRange == null) {
            return;
        }

        int from = timeRange.getFrom().getMinuteOfDay();
        int to = timeRange.getTo().getMinuteOfDay();
        SharedPreferences sp = this.context.getSharedPreferences(Preferences.class.getName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(FROM, from);
        editor.putInt(TO, to);
        editor.apply();
    }

    public TimeRange getCellularDataBlackout() {
        SharedPreferences sp = this.context.getSharedPreferences(Preferences.class.getName(), Context.MODE_PRIVATE);
        int from = sp.getInt(FROM, -1);
        int to = sp.getInt(TO, -1);

        if (from == -1 || to == -1) {
            return null;
        }

        return new TimeRange(new Time(from), new Time(to));
    }

    public long getStorageSize() {
        SharedPreferences sp = this.context.getSharedPreferences(Preferences.class.getName(), Context.MODE_PRIVATE);
        return sp.getLong(STORAGE_SIZE, -1);
    }

    public void saveStorageSize(long value) {
        SharedPreferences sp = this.context.getSharedPreferences(Preferences.class.getName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putLong(STORAGE_SIZE, value);
        edit.apply();
    }

    public void resetStorageSize() {
        SharedPreferences sp = this.context.getSharedPreferences(Preferences.class.getName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.remove(STORAGE_SIZE);
        edit.apply();
    }

    public void saveWlanRange(String value) {
        SharedPreferences sp = this.context.getSharedPreferences(Preferences.class.getName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString(WLAN_RANGE, value);
        edit.apply();
    }

    public String getWlanRange() {
        SharedPreferences sp = this.context.getSharedPreferences(Preferences.class.getName(), Context.MODE_PRIVATE);
        return sp.getString(WLAN_RANGE, "");
    }

    public void saveWwanRange(String value) {
        SharedPreferences sp = this.context.getSharedPreferences(Preferences.class.getName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString(WWAN_RANGE, value);
        edit.apply();
    }

    public String getWwanRange() {
        SharedPreferences sp = this.context.getSharedPreferences(Preferences.class.getName(), Context.MODE_PRIVATE);
        return sp.getString(WWAN_RANGE, "");
    }
}
