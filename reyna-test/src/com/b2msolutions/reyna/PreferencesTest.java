package com.b2msolutions.reyna;

import android.content.Context;
import android.content.SharedPreferences;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.RobolectricTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.*;

@RunWith(RobolectricTestRunner.class)
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
    public void canStoreStorageSize() {
        Preferences preferences = new Preferences(Robolectric.getShadowApplication().getApplicationContext());
        preferences.saveStorageSize(100);
        long storageSize = preferences.getStorageSize();
        assertEquals(100, storageSize);
    }

    @Test
    public void shouldReturnDefaultWhenNoStorageSize() {
        Preferences preferences = new Preferences(Robolectric.getShadowApplication().getApplicationContext());
        long storageSize = preferences.getStorageSize();
        assertEquals(-1, storageSize);
    }

    @Test
    public void shouldResetStorageSizeByRemovingTheKeyFromPreferences() {
        Preferences preferences = new Preferences(Robolectric.getShadowApplication().getApplicationContext());
        preferences.saveStorageSize(42);
        preferences.resetStorageSize();
        assertEquals(-1, preferences.getStorageSize());
    }

    @Test
    public void getWlanRangeShouldReturnExpectedOnceWlanRangeIsSaved() {
        Preferences preferences = new Preferences(Robolectric.getShadowApplication().getApplicationContext());
        preferences.saveWlanBlackout("01:00-09:00");
        assertEquals("01:00-09:00", preferences.getWlanBlackout());
    }

    @Test
    public void getWlanRangeReturnsEmptyStringIfWlanRangeIsNotSaved() {
        Preferences preferences = new Preferences(Robolectric.getShadowApplication().getApplicationContext());
        assertEquals("", preferences.getWlanBlackout());
    }

    @Test
    public void getWlanRangeReturnsEmptyStringIfNewWlanRangeIsNotValid() {
        Preferences preferences = new Preferences(Robolectric.getShadowApplication().getApplicationContext());
        preferences.saveWwanBlackout("01:00-09:00");//existing preference to overwrite
        preferences.saveWlanBlackout("");
        assertEquals("", preferences.getWlanBlackout());
    }

    @Test
    public void getWwanRangeReturnsEmptyStringIfNewWwanRangeIsNotValid() {
        Preferences preferences = new Preferences(Robolectric.getShadowApplication().getApplicationContext());
        preferences.saveWwanBlackout("01:00-09:00");//existing preference to overwrite
        preferences.saveWwanBlackout("");
        assertEquals("", preferences.getWwanBlackout());
    }

    @Test
    public void getWwanRangeShouldReturnExpectedOnceWwanRangeIsSaved() {
        Preferences preferences = new Preferences(Robolectric.getShadowApplication().getApplicationContext());
        preferences.saveWwanBlackout("01:00-09:00");
        assertEquals("01:00-09:00", preferences.getWwanBlackout());
    }

    @Test
    public void whenSavingWlanRangeShouldValidateInput() {
        Preferences preferences = new Preferences(Robolectric.getShadowApplication().getApplicationContext());
        preferences.saveWlanBlackout("01:00");
        assertEquals("", preferences.getWlanBlackout());
    }

    @Test
    public void whenSavingWwanRangeShouldValidateInput() {
        Preferences preferences = new Preferences(Robolectric.getShadowApplication().getApplicationContext());
        preferences.saveWwanBlackout("01:00");
        assertEquals("", preferences.getWwanBlackout());
    }

    @Test
    public void getWwanRangeReturnsEmptyStringIfWwanRangeIsNotSaved() {
        Preferences preferences = new Preferences(Robolectric.getShadowApplication().getApplicationContext());
        assertEquals("", preferences.getWwanBlackout());
    }

    @Test
    public void canSendOnRoamingShouldReturnExpected() {
        Preferences preferences = new Preferences(Robolectric.getShadowApplication().getApplicationContext());
        preferences.saveWwanRoamingBlackout(true);
        assertTrue(preferences.canSendOnRoaming());
    }

    @Test
    public void canSendOnRoamingReturnsFalseIfWwanRoamingIsNotSaved() {
        Preferences preferences = new Preferences(Robolectric.getShadowApplication().getApplicationContext());
        assertFalse(preferences.canSendOnRoaming());
    }

    @Test
    public void canSendOnChargeShouldReturnExpected() {
        Preferences preferences = new Preferences(Robolectric.getShadowApplication().getApplicationContext());
        preferences.saveOnChargeBlackout(true);
        assertTrue(preferences.canSendOnCharge());
    }

    @Test
    public void canSendOnChargeReturnsTrueIfOnChargeIsNotSaved() {
        Preferences preferences = new Preferences(Robolectric.getShadowApplication().getApplicationContext());
        assertTrue(preferences.canSendOnCharge());
    }

    @Test
    public void canSendOffChargeShouldReturnExpected() {
        Preferences preferences = new Preferences(Robolectric.getShadowApplication().getApplicationContext());
        preferences.saveOffChargeBlackout(true);
        assertTrue(preferences.canSendOffCharge());
    }

    @Test
    public void canSendOffChargeReturnsTrueIfOffChargeIsNotSaved() {
        Preferences preferences = new Preferences(Robolectric.getShadowApplication().getApplicationContext());
        assertTrue(preferences.canSendOffCharge());
    }

    @Test
    public void isBlackoutRangeValidShouldReturnBoolean() {
        Preferences preferences = new Preferences(Robolectric.getShadowApplication().getApplicationContext());
        assertTrue(preferences.isBlackoutRangeValid("00:00-02:30"));
        assertTrue(preferences.isBlackoutRangeValid("00:00-02:30,01:30-06:00"));
        assertTrue(preferences.isBlackoutRangeValid("00:00-02:30,03:30-06:00,07:00-07:01"));

        assertFalse(preferences.isBlackoutRangeValid(""));
        assertFalse(preferences.isBlackoutRangeValid("00:00"));
        assertFalse(preferences.isBlackoutRangeValid("1:00"));
        assertFalse(preferences.isBlackoutRangeValid("1:0002:00"));
        assertFalse(preferences.isBlackoutRangeValid("1"));
        assertFalse(preferences.isBlackoutRangeValid("00:10-"));
        assertFalse(preferences.isBlackoutRangeValid("00:10-1"));
        assertFalse(preferences.isBlackoutRangeValid("00:00-02:30-15:42"));
    }
}
