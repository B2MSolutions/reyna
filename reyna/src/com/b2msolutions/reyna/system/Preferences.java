package com.b2msolutions.reyna.system;

import android.app.AlarmManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import com.b2msolutions.reyna.blackout.Time;
import com.b2msolutions.reyna.blackout.TimeRange;

import java.net.URI;

public class Preferences {

    private final Context context;
    private final String FROM = "CELLULAR_DATA_BLACKOUT_FROM";
    private final String TO = "CELLULAR_DATA_BLACKOUT_TO";
    private final String STORAGE_SIZE = "STORAGE_SIZE";
    private final String WLAN_RANGE = "WLAN_RANGE";
    private final String WWAN_RANGE = "WWAN_RANGE";
    private final String WWAN_ROAMING_BLACKOUT = "WWAN_ROAMING_BLACKOUT";
    private final String ON_CHARGE_BLACKOUT = "ON_CHARGE_BLACKOUT";
    private final String OFF_CHARGE_BLACKOUT = "OFF_CHARGE_BLACKOUT";
    private final String BATCH_UPLOAD = "BATCH_UPLOAD";
    private final String BATCH_UPLOAD_URI = "BATCH_UPLOAD_URI";
    private final String BATCH_UPLOAD_INTERVAL = "BATCH_UPLOAD_INTERVAL";
    private final String WWAN_BLACKOUT_START = "WWAN_BLACKOUT_START";
    private final String WWAN_BLACKOUT_END = "WWAN_BLACKOUT_END";

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

    public void resetCellularDataBlackout() {
        SharedPreferences sp = this.context.getSharedPreferences(Preferences.class.getName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(FROM);
        editor.remove(TO);
        editor.apply();
    }

    public long getStorageSize() {
        return this.getLong(STORAGE_SIZE, -1);
    }

    public void saveStorageSize(long value) {
        this.putLong(STORAGE_SIZE, value);
    }

    public void resetStorageSize() {
        SharedPreferences sp = this.context.getSharedPreferences(Preferences.class.getName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.remove(STORAGE_SIZE);
        edit.apply();
    }

    public void saveWlanBlackout(String value) {
        this.putBlackoutRange(WLAN_RANGE, value);
    }

    public String getWlanBlackout() {
        return this.getString(WLAN_RANGE, "");
    }

    public void saveWwanBlackout(String value) {
        this.putBlackoutRange(WWAN_RANGE, value);
    }

    public String getWwanBlackout() {
        return this.getString(WWAN_RANGE, "");
    }

    public void saveWwanRoamingBlackout(boolean value) {
        this.putBoolean(WWAN_ROAMING_BLACKOUT, value);
    }

    public boolean canSendOnRoaming() {
        return !this.getBoolean(WWAN_ROAMING_BLACKOUT, true);
    }

    public void saveOnChargeBlackout(boolean value) {
        this.putBoolean(ON_CHARGE_BLACKOUT, value);
    }

    public boolean canSendOnCharge() {
        return !this.getBoolean(ON_CHARGE_BLACKOUT, false);
    }

    public void saveOffChargeBlackout(boolean value) {
        this.putBoolean(OFF_CHARGE_BLACKOUT, value);
    }

    public boolean canSendOffCharge() {
        return !this.getBoolean(OFF_CHARGE_BLACKOUT, false);
    }

    public void saveBatchUpload(boolean value) {
        this.putBoolean(BATCH_UPLOAD, value);
    }

    public boolean getBatchUpload() {
        return this.getBoolean(BATCH_UPLOAD, false);
    }

    public void saveBatchUploadUrl(URI value) {
        this.putString(BATCH_UPLOAD_URI, value.toString());
    }

    public URI getBatchUploadUrl() {
        String url = this.getString(BATCH_UPLOAD_URI, "");
        if (TextUtils.isEmpty(url)) {
            return null;
        }

        return URI.create(url);
    }

    public void saveBatchUploadCheckInterval(long value) {
        this.putLong(BATCH_UPLOAD_INTERVAL, value);
    }

    public long getBatchUploadCheckInterval() {
        return this.getLong(BATCH_UPLOAD_INTERVAL, AlarmManager.INTERVAL_HALF_DAY / 2);
    }

    public void putLong(String key, long value) {
        SharedPreferences sp = this.context.getSharedPreferences(Preferences.class.getName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putLong(key, value);
        edit.apply();
    }

    public long getLong(String key, long defaultValue) {
        SharedPreferences sp = this.context.getSharedPreferences(Preferences.class.getName(), Context.MODE_PRIVATE);
        return sp.getLong(key, defaultValue);
    }

    protected boolean isBlackoutRangeValid(String value) {
        String[] splitRanges = value.split(",");
        for (String range : splitRanges) {
            if (!range.matches("[0-9][0-9]:[0-9][0-9]-[0-9][0-9]:[0-9][0-9]")) {
                return false;
            }
        }
        return true;
    }

    private void putBoolean(String key, boolean value) {
        SharedPreferences sp = this.context.getSharedPreferences(Preferences.class.getName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putBoolean(key, value);
        edit.apply();
    }

    private void putString(String key, String value) {
        SharedPreferences sp = this.context.getSharedPreferences(Preferences.class.getName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString(key, value);
        edit.apply();
    }

    private void putBlackoutRange(String key, String blackoutRange) {
        String value = isBlackoutRangeValid(blackoutRange) ? blackoutRange : "";
        putString(key, value);
    }

    private boolean getBoolean(String key, boolean defaultValue) {
        SharedPreferences sp = this.context.getSharedPreferences(Preferences.class.getName(), Context.MODE_PRIVATE);
        return sp.getBoolean(key, defaultValue);
    }

    private String getString(String key, String defaultValue) {
        SharedPreferences sp = this.context.getSharedPreferences(Preferences.class.getName(), Context.MODE_PRIVATE);
        return sp.getString(key, defaultValue);
    }

    public long getNonRecurringWwanBlackoutStartTime() {
        return this.getLong(WWAN_BLACKOUT_START, -1);
    }

    public long getNonRecurringWwanBlackoutEndTime() {
        return this.getLong(WWAN_BLACKOUT_END, -1);
    }

    public void saveNonRecurringWwanBlackout(long startTime, long endTime) {
        this.putLong(WWAN_BLACKOUT_START, startTime);
        this.putLong(WWAN_BLACKOUT_END, endTime);
    }

    public void resetNonRecurringWwanBlackout() {
        SharedPreferences sp = this.context.getSharedPreferences(Preferences.class.getName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.remove(WWAN_BLACKOUT_START);
        edit.remove(WWAN_BLACKOUT_END);
        edit.apply();
    }
}
