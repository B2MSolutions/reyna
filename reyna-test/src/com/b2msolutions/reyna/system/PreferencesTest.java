package com.b2msolutions.reyna.system;

import android.content.Context;
import android.content.SharedPreferences;
import com.b2msolutions.reyna.blackout.Time;
import com.b2msolutions.reyna.blackout.TimeRange;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.net.URI;

import static junit.framework.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class PreferencesTest {

    @Test
    public void canStoreCellularDataBlackout() {
        TimeRange range = new TimeRange(new Time(1, 12), new Time(19, 19));
        Preferences preferences = new Preferences(RuntimeEnvironment.application.getApplicationContext());
        preferences.saveCellularDataBlackout(range);
        TimeRange other = preferences.getCellularDataBlackout();
        assertEquals(other.getFrom().getMinuteOfDay(), range.getFrom().getMinuteOfDay());
        assertEquals(other.getTo().getMinuteOfDay(), range.getTo().getMinuteOfDay());
    }

    @Test
    public void whenTimeRangeIsNullShouldNotThrow() {
        Preferences preferences = new Preferences(RuntimeEnvironment.application.getApplicationContext());
        preferences.saveCellularDataBlackout(null);
    }

    @Test
    public void whenNoBlackoutStoredShouldReturnNull() {
        SharedPreferences sp = RuntimeEnvironment.application.getSharedPreferences(Preferences.class.getName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.commit();

        Preferences preferences = new Preferences(RuntimeEnvironment.application.getApplicationContext());
        assertNull(preferences.getCellularDataBlackout());
    }

    @Test
    public void resetCellularDataBlackoutShouldRemoveKeys() {
        TimeRange range = new TimeRange(new Time(1, 12), new Time(19, 19));
        Preferences preferences = new Preferences(RuntimeEnvironment.application.getApplicationContext());
        preferences.saveCellularDataBlackout(range);
        TimeRange timeRange = preferences.getCellularDataBlackout();
        assertNotNull(timeRange);

        preferences.resetCellularDataBlackout();
        timeRange = preferences.getCellularDataBlackout();

        assertNull(timeRange);
    }

    @Test
    public void canStoreStorageSize() {
        Preferences preferences = new Preferences(RuntimeEnvironment.application.getApplicationContext());
        preferences.saveStorageSize(100);
        long storageSize = preferences.getStorageSize();
        assertEquals(100, storageSize);
    }

    @Test
    public void shouldReturnDefaultWhenNoStorageSize() {
        Preferences preferences = new Preferences(RuntimeEnvironment.application.getApplicationContext());
        long storageSize = preferences.getStorageSize();
        assertEquals(-1, storageSize);
    }

    @Test
    public void shouldResetStorageSizeByRemovingTheKeyFromPreferences() {
        Preferences preferences = new Preferences(RuntimeEnvironment.application.getApplicationContext());
        preferences.saveStorageSize(42);
        preferences.resetStorageSize();
        assertEquals(-1, preferences.getStorageSize());
    }

    @Test
    public void getWlanRangeShouldReturnExpectedOnceWlanRangeIsSaved() {
        Preferences preferences = new Preferences(RuntimeEnvironment.application.getApplicationContext());
        preferences.saveWlanBlackout("01:00-09:00");
        assertEquals("01:00-09:00", preferences.getWlanBlackout());
    }

    @Test
    public void getWlanRangeReturnsEmptyStringIfWlanRangeIsNotSaved() {
        Preferences preferences = new Preferences(RuntimeEnvironment.application.getApplicationContext());
        assertEquals("", preferences.getWlanBlackout());
    }

    @Test
    public void getWlanRangeReturnsEmptyStringIfNewWlanRangeIsNotValid() {
        Preferences preferences = new Preferences(RuntimeEnvironment.application.getApplicationContext());
        preferences.saveWwanBlackout("01:00-09:00");//existing preference to overwrite
        preferences.saveWlanBlackout("");
        assertEquals("", preferences.getWlanBlackout());
    }

    @Test
    public void getWwanRangeReturnsEmptyStringIfNewWwanRangeIsNotValid() {
        Preferences preferences = new Preferences(RuntimeEnvironment.application.getApplicationContext());
        preferences.saveWwanBlackout("01:00-09:00");//existing preference to overwrite
        preferences.saveWwanBlackout("");
        assertEquals("", preferences.getWwanBlackout());
    }

    @Test
    public void getWwanRangeShouldReturnExpectedOnceWwanRangeIsSaved() {
        Preferences preferences = new Preferences(RuntimeEnvironment.application.getApplicationContext());
        preferences.saveWwanBlackout("01:00-09:00");
        assertEquals("01:00-09:00", preferences.getWwanBlackout());
    }

    @Test
    public void whenSavingWlanRangeShouldValidateInput() {
        Preferences preferences = new Preferences(RuntimeEnvironment.application.getApplicationContext());
        preferences.saveWlanBlackout("01:00");
        assertEquals("", preferences.getWlanBlackout());
    }

    @Test
    public void whenSavingWwanRangeShouldValidateInput() {
        Preferences preferences = new Preferences(RuntimeEnvironment.application.getApplicationContext());
        preferences.saveWwanBlackout("01:00");
        assertEquals("", preferences.getWwanBlackout());
    }

    @Test
    public void getWwanRangeReturnsEmptyStringIfWwanRangeIsNotSaved() {
        Preferences preferences = new Preferences(RuntimeEnvironment.application.getApplicationContext());
        assertEquals("", preferences.getWwanBlackout());
    }

    @Test
    public void canSendOnRoamingShouldReturnExpected() {
        Preferences preferences = new Preferences(RuntimeEnvironment.application.getApplicationContext());
        preferences.saveWwanRoamingBlackout(true);
        assertFalse(preferences.canSendOnRoaming());
    }

    @Test
    public void canSendOnRoamingReturnsFalseIfWwanRoamingIsNotSaved() {
        Preferences preferences = new Preferences(RuntimeEnvironment.application.getApplicationContext());
        assertFalse(preferences.canSendOnRoaming());
    }

    @Test
    public void canSendOnChargeShouldReturnExpected() {
        Preferences preferences = new Preferences(RuntimeEnvironment.application.getApplicationContext());
        preferences.saveOnChargeBlackout(true);
        assertFalse(preferences.canSendOnCharge());
    }

    @Test
    public void canSendOnChargeReturnsTrueIfOnChargeIsNotSaved() {
        Preferences preferences = new Preferences(RuntimeEnvironment.application.getApplicationContext());
        assertTrue(preferences.canSendOnCharge());
    }

    @Test
    public void canSendOffChargeShouldReturnExpected() {
        Preferences preferences = new Preferences(RuntimeEnvironment.application.getApplicationContext());
        preferences.saveOffChargeBlackout(true);
        assertFalse(preferences.canSendOffCharge());
    }

    @Test
    public void canSendOffChargeReturnsTrueIfOffChargeIsNotSaved() {
        Preferences preferences = new Preferences(RuntimeEnvironment.application.getApplicationContext());
        assertTrue(preferences.canSendOffCharge());
    }

    @Test
    public void isBlackoutRangeValidShouldReturnBoolean() {
        Preferences preferences = new Preferences(RuntimeEnvironment.application.getApplicationContext());
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

    @Test
    public void whenNoBatchUploadStoredShouldReturnTrue() {
        this.clear();

        Preferences preferences = new Preferences(RuntimeEnvironment.application.getApplicationContext());
        assertFalse(preferences.getBatchUpload());
    }

    @Test
    public void whenSavingBatchUploadStoredShouldReturnTrue() {
        Preferences preferences = new Preferences(RuntimeEnvironment.application.getApplicationContext());

        preferences.saveBatchUpload(true);
        assertTrue(preferences.getBatchUpload());

        preferences.saveBatchUpload(false);
        assertFalse(preferences.getBatchUpload());
    }

    @Test
    public void whenSavingBatchUploadUrlShouldGetItCorrectly() {
        Preferences preferences = new Preferences(RuntimeEnvironment.application.getApplicationContext());
        preferences.saveBatchUploadUrl(URI.create("www.URL.com"));
        assertEquals(URI.create("www.URL.com"), preferences.getBatchUploadUrl());
    }

    @Test
    public void whenGetBatchUploadCheckIntervalAndNotPreviouslySavedShouldReturnSixHours() {
        this.clear();

        Preferences preferences = new Preferences(RuntimeEnvironment.application.getApplicationContext());
        assertEquals(6 * 60 * 60 * 1000, preferences.getBatchUploadCheckInterval());
    }

    @Test
    public void whenSavingBatchUploadCheckIntervalShouldGetItCorrectly() {
        Preferences preferences = new Preferences(RuntimeEnvironment.application.getApplicationContext());
        preferences.saveBatchUploadCheckInterval(100);
        assertEquals(100, preferences.getBatchUploadCheckInterval());
    }

    @Test
    public void whenGetBatchUploadUrlAndNotPreviouslySavedReturnNull() {
        this.clear();

        Preferences preferences = new Preferences(RuntimeEnvironment.application.getApplicationContext());
        assertNull(preferences.getBatchUploadUrl());
    }

    @Test
    public void whenCallingGetNonRecurringWwanBlackoutStartTimeAndNoValueSavedShouldReturnSpecifiedDefaul() {
        Preferences preferences = new Preferences(RuntimeEnvironment.application.getApplicationContext());
        assertEquals(-1,  preferences.getNonRecurringWwanBlackoutStartTime(-1));
    }

    @Test
    public void whenCallingGetNonRecurringWwanBlackoutEndTimeAndNoValueSavedShouldReturnSpecifiedDefault() {
        Preferences preferences = new Preferences(RuntimeEnvironment.application.getApplicationContext());
        assertEquals(-1,  preferences.getNonRecurringWwanBlackoutEndTime(-1));
    }

    @Test
    public void whenCallingSaveNonRecurringWwanBlackoutStartTimeShouldRecordStartTimeOfBlackout() {
        Preferences preferences = new Preferences(RuntimeEnvironment.application.getApplicationContext());
        preferences.saveNonRecurringWwanBlackoutStartTime(42L);
        assertEquals(42L,  preferences.getNonRecurringWwanBlackoutStartTime(-1));
    }

    @Test
    public void whenCallingSaveNonRecurringWwanBlackoutEndTimeShouldRecordEndTimeOfBlackout() {
        Preferences preferences = new Preferences(RuntimeEnvironment.application.getApplicationContext());
        preferences.saveNonRecurringWwanBlackoutEndTime(42L);
        assertEquals(42L,  preferences.getNonRecurringWwanBlackoutEndTime(-1));
    }

    @Test
    public void whenCallingResetNonRecurringWwanBlackoutShouldRemoveStartAndEndValuesFromPrefs() {
        Preferences preferences = new Preferences(RuntimeEnvironment.application.getApplicationContext());
        preferences.resetNonRecurringWwanBlackout();
        assertEquals(-1L,  preferences.getNonRecurringWwanBlackoutStartTime(-1));
        assertEquals(-1L,  preferences.getNonRecurringWwanBlackoutEndTime(-1));
    }

    @Test
    public void whenCallingSaveNonRecurringWwanBlackoutStartTimeShouldRecordStartTimeOfBlackoutAsString() {
        Preferences preferences = new Preferences(RuntimeEnvironment.application.getApplicationContext());
        preferences.saveNonRecurringWwanBlackoutStartTime(42L);
        assertEquals("42",  preferences.getNonRecurringWwanBlackoutStartTimeAsString());
    }

    @Test
    public void whenCallingSaveNonRecurringWwanBlackoutEndTimeShouldRecordEndTimeOfBlackoutAsString() {
        Preferences preferences = new Preferences(RuntimeEnvironment.application.getApplicationContext());
        preferences.saveNonRecurringWwanBlackoutEndTime(42L);
        assertEquals("42",  preferences.getNonRecurringWwanBlackoutEndTimeAsString());
    }

    @Test
    public void whenCallingGetNonRecurringWwanBlackoutStartTimeAsStringShouldDefaultToNull() {
        Preferences preferences = new Preferences(RuntimeEnvironment.application.getApplicationContext());
        assertEquals(null,  preferences.getNonRecurringWwanBlackoutStartTimeAsString());
    }

    @Test
    public void whenCallingGetNonRecurringWwanBlackoutEndTimeAsStringShouldDefaultToNull() {
        Preferences preferences = new Preferences(RuntimeEnvironment.application.getApplicationContext());
        assertEquals(null,  preferences.getNonRecurringWwanBlackoutEndTimeAsString());
    }

    @Test
    public void whenCallingResetNonRecurringWwanBlackoutShouldRemoveStartAndEndValuesFromPrefsAndGetValuesAsStringShouldReturnNull() {
        Preferences preferences = new Preferences(RuntimeEnvironment.application.getApplicationContext());
        preferences.resetNonRecurringWwanBlackout();
        assertEquals(null,  preferences.getNonRecurringWwanBlackoutStartTimeAsString());
        assertEquals(null,  preferences.getNonRecurringWwanBlackoutEndTimeAsString());
    }

    private void clear() {
        SharedPreferences sp = RuntimeEnvironment.application.getSharedPreferences(Preferences.class.getName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.commit();
    }
}
