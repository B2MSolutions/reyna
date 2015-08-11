package com.b2msolutions.reyna.services;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class Power {

    public boolean isCharging(Context context) {
        Intent batteryStatus = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        Integer plugged = batteryStatus.getIntExtra(android.os.BatteryManager.EXTRA_PLUGGED, -1);
        return plugged == android.os.BatteryManager.BATTERY_PLUGGED_AC ||
                plugged == android.os.BatteryManager.BATTERY_PLUGGED_USB ||
                // wireless!
                plugged == 4 ||
                // unknown
                plugged == 3;
    }
}
