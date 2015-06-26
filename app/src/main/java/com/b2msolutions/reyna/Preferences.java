package com.b2msolutions.reyna;

import android.content.Context;
import android.content.SharedPreferences;

import com.b2msolutions.reyna.services.DispatcherService;

public class Preferences {

    private final Context context;
    private final String FROM = "CELLULAR_DATA_BLACKOUT_FROM";
    private final String TO = "CELLULAR_DATA_BLACKOUT_TO";
    private final String TEMPORARY_ERROR_TIMEOUT_SETTING = "TEMPORARY_ERROR_TIMEOUT_SETTING";
    private final String DISPATCHER_SERVICE_NAME = "DISPATCHER_SERVICE_NAME";

    protected static final long DEFAULT_TEMPORARY_ERROR_TIMEOUT_SETTING = 300000; // 5 minutes

    public Preferences(Context context) {
        this.context = context;
    }

    public void saveCellularDataBlackout(TimeRange timeRange) {
        if (timeRange == null) {
            return;
        }

        int from = timeRange.getFrom().getMinuteOfDay();
        int to = timeRange.getTo().getMinuteOfDay();
        SharedPreferences sp = getSharedPreferences();
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(FROM, from);
        editor.putInt(TO, to);
        editor.apply();
    }

    public TimeRange getCellularDataBlackout() {
        SharedPreferences sp = getSharedPreferences();
        int from = sp.getInt(FROM, -1);
        int to = sp.getInt(TO, -1);

        if (from == -1 || to == -1) {
            return null;
        }

        return new TimeRange(new Time(from), new Time(to));
    }

    private SharedPreferences getSharedPreferences() {
        return this.context.getSharedPreferences(Preferences.class.getName(), Context.MODE_PRIVATE);
    }

    public void saveTemporaryErrorTimeout(long timeoutMilliseconds){
        SharedPreferences sp = getSharedPreferences();
        sp.edit().putLong(TEMPORARY_ERROR_TIMEOUT_SETTING, timeoutMilliseconds).apply();
    }

    public long getTemporaryErrorTimeout(){
        SharedPreferences sp = getSharedPreferences();
        return sp.getLong(TEMPORARY_ERROR_TIMEOUT_SETTING, DEFAULT_TEMPORARY_ERROR_TIMEOUT_SETTING);
    }

    public void saveDispatcherServiceName(String dispatcherServiceName){
        SharedPreferences sp = getSharedPreferences();
        sp.edit().putString(DISPATCHER_SERVICE_NAME, dispatcherServiceName).apply();
    }

    public String getDispatcherServiceName(){
        SharedPreferences sp = getSharedPreferences();
        return sp.getString(DISPATCHER_SERVICE_NAME, null);
    }
}
