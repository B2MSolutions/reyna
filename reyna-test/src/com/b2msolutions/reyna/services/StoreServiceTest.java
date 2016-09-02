package com.b2msolutions.reyna.services;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.b2msolutions.reyna.*;
import com.b2msolutions.reyna.blackout.Time;
import com.b2msolutions.reyna.blackout.TimeRange;
import com.b2msolutions.reyna.system.Logger;
import com.b2msolutions.reyna.system.Message;
import com.b2msolutions.reyna.system.Preferences;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.net.URI;
import java.net.URISyntaxException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static test.Assert.assertServiceNotStartedOrgRobolectric;
import static test.Assert.assertServiceStartedOrgRobolectric;

@RunWith(RobolectricTestRunner.class)
public class StoreServiceTest {

    @Mock Repository repository;

    private StoreService storeService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.storeService = org.robolectric.Robolectric.setupService(StoreService.class);
        this.storeService.repository = repository;

        StoreService.setBatchUploadConfiguration(this.storeService.getApplicationContext(), false, URI.create("http://google.com"), 1);
    }

    @Test
    public void testConstruction() {
        assertNotNull(this.storeService);
        assertNotNull(this.storeService.repository);
    }

    @Test
    public void onHandleIntentWithNullIntentShouldNotThrow() {
        this.storeService.onHandleIntent(null);
    }

    @Test
    public void onHandleIntentWithoutMessageShouldNotThrow() {
        Intent intent = new Intent();
        this.storeService.onHandleIntent(intent);
    }

    @Test
    public void onHandleIntentWithMessageShouldStoreAndStartForwardService() throws URISyntaxException {
        Message message = RepositoryTest.getMessageWithHeaders();
        Intent intent = new Intent();
        intent.putExtra("com.b2msolutions.reyna.MESSAGE", message);

        this.storeService.onHandleIntent(intent);

        ArgumentCaptor<Message> argument = ArgumentCaptor.forClass(Message.class);
        verify(this.repository).insert(argument.capture());
        Message other = argument.getValue();
        assertNotNull(other);
        assertEquals(message.getUrl(), other.getUrl());
        assertEquals(message.getBody(), other.getBody());
        assertEquals("h1", message.getHeaders()[0].getKey());
        assertEquals("v1", message.getHeaders()[0].getValue());
        assertEquals("h2", message.getHeaders()[1].getKey());
        assertEquals("v2", message.getHeaders()[1].getValue());

        assertServiceStartedOrgRobolectric(ForwardService.class);
    }

    @Test
    public void onHandleIntentWithMessageAndBatchUploadEnabledShouldStoreAndStartForwardService() throws URISyntaxException {
        Message message = RepositoryTest.getMessageWithHeaders();
        Intent intent = new Intent();
        intent.putExtra("com.b2msolutions.reyna.MESSAGE", message);
        StoreService.setBatchUploadConfiguration(this.storeService.getApplicationContext(), true, URI.create("http://google.com"), 1);

        this.storeService.onHandleIntent(intent);

        ArgumentCaptor<Message> argument = ArgumentCaptor.forClass(Message.class);
        verify(this.repository).insert(argument.capture());
        Message other = argument.getValue();
        assertNotNull(other);
        assertEquals(message.getUrl(), other.getUrl());
        assertEquals(message.getBody(), other.getBody());
        assertEquals("h1", message.getHeaders()[0].getKey());
        assertEquals("v1", message.getHeaders()[0].getValue());
        assertEquals("h2", message.getHeaders()[1].getKey());
        assertEquals("v2", message.getHeaders()[1].getValue());

        assertServiceStartedOrgRobolectric(ForwardService.class);
    }

    @Test
    public void onHandleIntentWithMessageShouldStoreWithDbSizeLimitAndStartForwardService() throws URISyntaxException {
        Context context = Robolectric.application.getApplicationContext();
        Message message = RepositoryTest.getMessageWithHeaders();
        Intent intent = new Intent();
        intent.putExtra("com.b2msolutions.reyna.MESSAGE", message);

        StoreService.setStorageSizeLimit(context, 2100042);
        this.storeService.onHandleIntent(intent);

        ArgumentCaptor<Message> argument = ArgumentCaptor.forClass(Message.class);
        ArgumentCaptor<Long> argumentLimit = ArgumentCaptor.forClass(Long.class);

        verify(this.repository).insert(argument.capture(), argumentLimit.capture());
        Message other = argument.getValue();
        assertNotNull(other);
        assertEquals(message.getUrl(), other.getUrl());
        assertEquals(message.getBody(), other.getBody());
        assertEquals("h1", message.getHeaders()[0].getKey());
        assertEquals("v1", message.getHeaders()[0].getValue());
        assertEquals("h2", message.getHeaders()[1].getKey());
        assertEquals("v2", message.getHeaders()[1].getValue());

        long limit = argumentLimit.getValue();
        assertEquals(2100042, limit);

        assertServiceStartedOrgRobolectric(ForwardService.class);
    }

    @Test
    public void setLogLevelShouldChangeLogLevel() {
        StoreService.setLogLevel(Log.DEBUG);
        assertEquals(Log.DEBUG, Logger.getLevel());
    }

    @Test
    public void setCellularDataBlackoutShouldSave() {
        TimeRange range = new TimeRange(new Time(3, 00), new Time(19, 00));
        Context context = Robolectric.getShadowApplication().getApplicationContext();
        StoreService.setCellularDataBlackout(context, range);
        Preferences preferences = new Preferences(context);
        TimeRange saved = preferences.getCellularDataBlackout();
        assertEquals(range.getFrom().getMinuteOfDay(), saved.getFrom().getMinuteOfDay());
        assertEquals(range.getTo().getMinuteOfDay(), saved.getTo().getMinuteOfDay());
    }

    @Test
    public void resetCellularDataBlackoutShouldRemoveKeys() {
        TimeRange range = new TimeRange(new Time(3, 00), new Time(19, 00));
        Context context = Robolectric.getShadowApplication().getApplicationContext();
        StoreService.setCellularDataBlackout(context, range);
        Preferences preferences = new Preferences(context);
        TimeRange saved = preferences.getCellularDataBlackout();
        assertNotNull(saved);

        StoreService.resetCellularDataBlackout(context);
        saved = preferences.getCellularDataBlackout();
        assertNull(saved);
    }

    @Test
    public void setWlanBlackoutShouldSave() {
        String ranges = "06:00-07:00,08:30-10:00,12:19-13:50";
        Context context = Robolectric.getShadowApplication().getApplicationContext();
        StoreService.setWlanBlackout(context, ranges);
        Preferences preferences = new Preferences(context);
        String saved = preferences.getWlanBlackout();
        assertEquals(saved, ranges);
    }

    @Test
    public void setWwanBlackoutShouldSave() {
        String ranges = "06:00-07:00,08:30-10:00,12:19-13:50";
        Context context = Robolectric.getShadowApplication().getApplicationContext();
        StoreService.setWwanBlackout(context, ranges);
        Preferences preferences = new Preferences(context);
        String saved = preferences.getWwanBlackout();
        assertEquals(saved, ranges);
    }

    @Test
    public void setWwanRoamingBlackoutShouldSave() {
        Context context = Robolectric.getShadowApplication().getApplicationContext();
        StoreService.setWwanRoamingBlackout(context, false);
        Preferences preferences = new Preferences(context);
        assertTrue(preferences.canSendOnRoaming());
    }

    @Test
    public void setOnChargeBlackoutShouldSave() {
        Context context = Robolectric.getShadowApplication().getApplicationContext();
        StoreService.setOnChargeBlackout(context, false);
        Preferences preferences = new Preferences(context);
        assertTrue(preferences.canSendOnCharge());
    }

    @Test
    public void setOffChargeBlackoutShouldSave() {
        Context context = Robolectric.getShadowApplication().getApplicationContext();
        StoreService.setOffChargeBlackout(context, false);
        Preferences preferences = new Preferences(context);
        assertTrue(preferences.canSendOffCharge());
    }

    @Test
    public void setStorageLimitShouldSaveTheLimit() {
        Context context = Robolectric.getShadowApplication().getApplicationContext();
        StoreService.setStorageSizeLimit(context, 2100042);
        assertEquals(2100042, StoreService.getStorageSizeLimit(context));
    }

    @Test
    public void setStorageLimitShouldSetLimitToMinValueIfItLessThan0() {
        Context context = Robolectric.getShadowApplication().getApplicationContext();
        StoreService.setStorageSizeLimit(context, 2100042);
        StoreService.setStorageSizeLimit(context, -100);
        assertEquals(1867776, StoreService.getStorageSizeLimit(context));
    }

    @Test
    public void setStorageLimitShouldSetLimitToMinValueIfItLessEqual0() {
        Context context = Robolectric.getShadowApplication().getApplicationContext();
        StoreService.setStorageSizeLimit(context, 2100042);
        StoreService.setStorageSizeLimit(context, 0);
        assertEquals(1867776, StoreService.getStorageSizeLimit(context));
    }

    @Test
    public void setStorageLimitShouldSetSizeToMinValueIfPassedValueIsLessThanMinValue() {
        Context context = Robolectric.getShadowApplication().getApplicationContext();
        StoreService.setStorageSizeLimit(context, 42);
        assertEquals(1867776, StoreService.getStorageSizeLimit(context));
    }

    @Test
    public void resetStorageLimitShouldSetItToDefault() {
        Context context = Robolectric.getShadowApplication().getApplicationContext();
        StoreService.resetStorageSizeLimit(context);
        assertEquals(-1, StoreService.getStorageSizeLimit(context));
    }

    @Test
    public void setBatchUploadToTrueShouldSave() {
        Context context = Robolectric.getShadowApplication().getApplicationContext();
        StoreService.setBatchUploadConfiguration(context, true, URI.create("http://msn.com"), 1000);
        Preferences preferences = new Preferences(context);
        assertTrue(preferences.getBatchUpload());
        assertEquals(URI.create("http://msn.com"), preferences.getBatchUploadUrl());
        assertEquals(1000, preferences.getBatchUploadCheckInterval());
    }

    @Test
    public void setBatchUploadToFalseShouldSave() {
        Context context = Robolectric.getShadowApplication().getApplicationContext();
        StoreService.setBatchUploadConfiguration(context, false, URI.create("http://google.com"), 100);
        Preferences preferences = new Preferences(context);
        assertFalse(preferences.getBatchUpload());
        assertEquals(URI.create("http://google.com"), preferences.getBatchUploadUrl());
        assertEquals(100, preferences.getBatchUploadCheckInterval());
    }

    @Test
    public void whenCallingStartShouldAcquirePowerLock() {
        StoreService.start(Robolectric.application.getApplicationContext(), null);

        Intent intent = assertServiceStartedOrgRobolectric(StoreService.class);
        int lockId = intent.getIntExtra("android.support.content.wakelockid", -1);
        assertTrue(lockId != -1);
    }

    @Test
    public void whenCallingSaveNonRecurringWwanBlackoutStartTimeShouldRecordStartTimeOfBlackout() {
        Context context = Robolectric.getShadowApplication().getApplicationContext();
        Preferences preferences = new Preferences(context);

        StoreService.setNonRecurringWwanBlackoutStartTime(context, 42L);

        assertEquals(42L, preferences.getNonRecurringWwanBlackoutStartTime(-1));
    }

    @Test
    public void whenCallingSaveNonRecurringWwanBlackoutEndTimeShouldRecordEndTimeOfBlackout() {
        Context context = Robolectric.getShadowApplication().getApplicationContext();
        Preferences preferences = new Preferences(context);

        StoreService.setNonRecurringWwanBlackoutEndTime(context, 42L);

        assertEquals(42L,  preferences.getNonRecurringWwanBlackoutEndTime(-1));
    }

    @Test
    public void whenCallingResetNonRecurringWwanBlackoutShouldRemoveStartAndEndValuesFromPrefsAndGetValuesAsStringShouldReturnNull() {
        Context context = Robolectric.getShadowApplication().getApplicationContext();
        Preferences preferences = new Preferences(context);

        StoreService.resetNonRecurringWwanBlackout(context);

        assertEquals(null, preferences.getNonRecurringWwanBlackoutStartTimeAsString());
        assertEquals(null,  preferences.getNonRecurringWwanBlackoutEndTimeAsString());
    }

}
