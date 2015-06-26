package com.b2msolutions.reyna;

import android.content.Context;
import android.content.SharedPreferences;

import com.b2msolutions.reyna.services.DispatcherService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

@RunWith(ReynaTestRunner.class)
public class PreferencesTest {

    @Test
    public void canStoreCellularDataBlackout() {
        TimeRange range = new TimeRange(new Time(1, 12), new Time(19, 19));
        Preferences preferences = new Preferences(Robolectric.getShadowApplication().getApplicationContext());
        preferences.saveCellularDataBlackout(range);
        TimeRange other = preferences.getCellularDataBlackout();
        assertEquals(other.getFrom().getMinuteOfDay(), range.getFrom().getMinuteOfDay());
        assertEquals(other.getTo().getMinuteOfDay(), range.getTo().getMinuteOfDay());
    }

    @Test
    public void whenTimeRangeIsNullShouldNotThrow() {
        Preferences preferences = new Preferences(Robolectric.getShadowApplication().getApplicationContext());
        preferences.saveCellularDataBlackout(null);
    }

    @Test
    public void whenNoBlackoutStoredShouldReturnNull() {
        SharedPreferences sp = Robolectric.getShadowApplication().getSharedPreferences(Preferences.class.getName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.commit();

        Preferences preferences = new Preferences(Robolectric.getShadowApplication().getApplicationContext());
        assertNull(preferences.getCellularDataBlackout());
    }

    @Test
    public void canStoreTemporaryErrorTimeout(){
        long timeout = 42l;
        Preferences preferences = new Preferences(Robolectric.getShadowApplication().getApplicationContext());
        preferences.saveTemporaryErrorTimeout(timeout);
        long result = preferences.getTemporaryErrorTimeout();
        assertEquals(timeout, result);
    }

    @Test
    public void whenCallingGetTemporaryErrorTimeoutWithoutSettigDefaultValueIsReturned(){
        Preferences preferences = new Preferences(Robolectric.getShadowApplication().getApplicationContext());
        long result = preferences.getTemporaryErrorTimeout();
        assertEquals(preferences.DEFAULT_TEMPORARY_ERROR_TIMEOUT_SETTING, result);
    }

    @Test
    public void canStoreDispatcherServiceName(){
        String dispatcherName = "dipatcher";
        Preferences preferences = new Preferences(Robolectric.getShadowApplication().getApplicationContext());
        preferences.saveDispatcherServiceName(dispatcherName);
        String result = preferences.getDispatcherServiceName();
        assertEquals(dispatcherName, result);
    }

    @Test
    public void whenCallingGetDispatcherServiceNameWithoutSettigDefaultValueIsReturned(){
        Preferences preferences = new Preferences(Robolectric.getShadowApplication().getApplicationContext());
        String result = preferences.getDispatcherServiceName();
        assertEquals(null, result);
    }
}
