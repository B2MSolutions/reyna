package com.b2msolutions.reyna.services;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.b2msolutions.reyna.*;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.RobolectricTestRunner;
import com.xtremelabs.robolectric.shadows.ShadowApplication;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class StoreServiceTest {

    @Mock Repository repository;

    private StoreService storeService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        StoreService.resetStorageSizeLimit();
        this.storeService = new StoreService();
        this.storeService.repository = repository;
    }

    @Test
    public void testConstruction() {
        assertNotNull(this.storeService);
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
        intent.putExtra(StoreService.MESSAGE, message);

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

        ShadowApplication shadowApplication = Robolectric.getShadowApplication();
        Intent service = shadowApplication.getNextStartedService();
        assertNotNull(service);
        assertEquals(ForwardService.class.getName(), service.getComponent().getClassName());
    }

    @Test
    public void onHandleIntentWithMessageShouldStoreWithDbSizeLimitAndStartForwardService() throws URISyntaxException {
        Message message = RepositoryTest.getMessageWithHeaders();
        Intent intent = new Intent();
        intent.putExtra(StoreService.MESSAGE, message);

        StoreService.setStorageSizeLimit(42);
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
        assertEquals(42, limit);

        ShadowApplication shadowApplication = Robolectric.getShadowApplication();
        Intent service = shadowApplication.getNextStartedService();
        assertNotNull(service);
        assertEquals(ForwardService.class.getName(), service.getComponent().getClassName());
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
    public void setStorageLimitShouldSaveTheLimit() {
        StoreService.setStorageSizeLimit(42);
        assertEquals(new Long(42), StoreService.getStorageSizeLimit());
    }

    @Test
    public void resetStorageLimitShouldSetItToNull() {
        StoreService.resetStorageSizeLimit();
        assertNull(StoreService.getStorageSizeLimit());
    }
}
