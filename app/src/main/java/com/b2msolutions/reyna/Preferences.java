package com.b2msolutions.reyna;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {

    private final Context context;
    private final String FROM = "CELLULAR_DATA_BLACKOUT_FROM";
    private final String TO = "CELLULAR_DATA_BLACKOUT_TO";

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
}
