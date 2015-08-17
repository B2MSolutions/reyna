package com.b2msolutions.reyna.services;

import android.content.Context;
import android.content.Intent;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.RobolectricTestRunner;
import com.xtremelabs.robolectric.shadows.ShadowApplication;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class GivenAPower {

    private ShadowApplication shadowApplication;

    private Context context;
    private Intent batteryStatus;
    private Power power;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        shadowApplication = Robolectric.getShadowApplication();
        context = shadowApplication.getApplicationContext();
        power = new Power();
    }

    private void initBatteryChanged() {
        batteryStatus = new Intent();
        batteryStatus.setAction(Intent.ACTION_BATTERY_CHANGED);
        batteryStatus.putExtra(android.os.BatteryManager.EXTRA_LEVEL, 3);
        batteryStatus.putExtra(android.os.BatteryManager.EXTRA_SCALE, 7);
        batteryStatus.putExtra(android.os.BatteryManager.EXTRA_STATUS, 4);
        shadowApplication.sendStickyBroadcast(batteryStatus);
    }

    @Test
    public void whenCallingIsChargingAndCouldNotGetBatteryStatusShouldReturnFalse() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        shadowApplication.sendStickyBroadcast(intent);
        assertFalse(power.isCharging(context));
    }

    @Test
    public void whenCallingIsChargingShouldReturnExpected() {
        initBatteryChanged();
        batteryStatus.putExtra(android.os.BatteryManager.EXTRA_PLUGGED, android.os.BatteryManager.BATTERY_PLUGGED_AC);
        assertTrue(power.isCharging(context));
    }

    @Test
    public void whenCallingIsChargingUsbShouldReturnExpected() {
        initBatteryChanged();
        batteryStatus.putExtra(android.os.BatteryManager.EXTRA_PLUGGED, android.os.BatteryManager.BATTERY_PLUGGED_USB);
        assertTrue(power.isCharging(context));
    }

    @Test
    public void whenCallingIsChargingWirelessShouldReturnExpected() {
        initBatteryChanged();
        batteryStatus.putExtra(android.os.BatteryManager.EXTRA_PLUGGED, 4);
        assertTrue(power.isCharging(context));
    }

    @Test
    public void whenCallingIsChargingUnknownShouldReturnExpected() {
        initBatteryChanged();
        batteryStatus.putExtra(android.os.BatteryManager.EXTRA_PLUGGED, 3);
        assertTrue(power.isCharging(context));
    }

    @Test
    public void whenCallingGetBatteryChargingOnAndNotChargingShouldReturnExpected() {
        initBatteryChanged();
        batteryStatus.putExtra(android.os.BatteryManager.EXTRA_PLUGGED, -1);
        assertFalse(power.isCharging(context));
    }
}

