package com.b2msolutions.reyna.services;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.b2msolutions.reyna.*;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowApplication;
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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(ReynaTestRunner.class)
public class StoreServiceTest {
    
    @Mock Repository repository;
    
    private StoreService storeService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.storeService = new StoreService();
        this.storeService.repository = repository;
    }
    
    @Test
    public void testConstruction() {        
        assertNotNull(this.storeService);
    }   

    @Test
    public void onHandleIntentWithoutMessageShouldNotThrow() {
        Intent intent = new Intent();       
        this.storeService.onHandleIntent(intent);
    }

    @Test
    public void onCalllingStartShouldStartStoreServiceWithProvidedMessage() throws URISyntaxException {
        ShadowApplication shadowApplication = Robolectric.getShadowApplication();

        Message message = ReynaSqlHelperTest.getMessageWithHeaders();
        StoreService.start(shadowApplication.getApplicationContext(), message);

        Intent service = shadowApplication.getNextStartedService();
        assertNotNull(service);
        assertEquals(StoreService.class.getName(), service.getComponent().getClassName());
        assertEquals(StoreService.ACTION_STORE_MESSAGE, service.getAction());

        Message receivedMessage = (Message) service.getSerializableExtra(StoreService.MESSAGE);
        assertNotNull(receivedMessage);
        assertEquals(message.getUrl(), receivedMessage.getUrl());
        assertEquals(message.getBody(), receivedMessage.getBody());
        assertEquals("h1", receivedMessage.getHeaders()[0].getKey());
        assertEquals("v1", receivedMessage.getHeaders()[0].getValue());
        assertEquals("h2", receivedMessage.getHeaders()[1].getKey());
        assertEquals("v2", receivedMessage.getHeaders()[1].getValue());
    }
    
    @Test
    public void onHandleIntentWithMessageShouldStoreAndStartForwardService() throws URISyntaxException {
        Message message = ReynaSqlHelperTest.getMessageWithHeaders();
        Intent intent = new Intent();
        intent.setAction(StoreService.ACTION_STORE_MESSAGE);
        intent.putExtra(StoreService.MESSAGE, message);
                
        this.storeService.onHandleIntent(intent);       
        
        ArgumentCaptor<Message> argument = ArgumentCaptor.forClass(Message.class);
        verify(this.repository).insert(argument.capture());
        Message other = argument.getValue();
        assertNotNull(other);
        assertEquals(message.getUrl(), other.getUrl());
        assertEquals(message.getBody(), other.getBody());
        assertEquals("h1", other.getHeaders()[0].getKey());
        assertEquals("v1", other.getHeaders()[0].getValue());
        assertEquals("h2", other.getHeaders()[1].getKey());
        assertEquals("v2", other.getHeaders()[1].getValue());

        ShadowApplication shadowApplication = Robolectric.getShadowApplication();
        Intent service = shadowApplication.getNextStartedService();
        assertNotNull(service);
        assertEquals(ForwardService.class.getName(), service.getComponent().getClassName());
    }

    @Test
    public void onHandleIntentWithMissingMessageShouldNotStore() throws URISyntaxException {
        Intent intent = new Intent();
        intent.setAction(StoreService.ACTION_STORE_MESSAGE);
        intent.putExtra(StoreService.MESSAGE, (Message)null);

        this.storeService.onHandleIntent(intent);

        verify(this.repository, never()).insert(any(Message.class));
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
    public void onCalllingChangeDatabaseLocationShouldStartStoreServiceWithProvidedLocation() throws URISyntaxException {
        ShadowApplication shadowApplication = Robolectric.getShadowApplication();
        String location = "/databasepath";

        StoreService.changeDatabaseLocation(shadowApplication.getApplicationContext(), location);

        Intent service = shadowApplication.getNextStartedService();
        assertNotNull(service);
        assertEquals(StoreService.class.getName(), service.getComponent().getClassName());
        assertEquals(StoreService.ACTION_MOVE, service.getAction());

        String newLocation = service.getStringExtra(StoreService.LOCATION);
        assertEquals(location + "/reyna.db", newLocation);
    }

    @Test
    public void onCalllingChangeDatabaseLocationShouldNotDependOnTrailingSlash() throws URISyntaxException {
        ShadowApplication shadowApplication = Robolectric.getShadowApplication();
        String location = "/databasepath/";

        StoreService.changeDatabaseLocation(shadowApplication.getApplicationContext(), location);

        Intent service = shadowApplication.getNextStartedService();
        assertNotNull(service);
        assertEquals(StoreService.class.getName(), service.getComponent().getClassName());
        assertEquals(StoreService.ACTION_MOVE, service.getAction());

        String newLocation = service.getStringExtra(StoreService.LOCATION);
        assertEquals(location + "reyna.db", newLocation);
    }

    @Test
    public void onHandleIntentWithValidLocationShouldCallMoveDatabase() throws Exception {
        ShadowApplication shadowApplication = Robolectric.getShadowApplication();
        String location = "/databasepath/reyna.db";

        Intent intent = new Intent(shadowApplication.getApplicationContext(), StoreService.class);
        intent.setAction(StoreService.ACTION_MOVE);
        intent.putExtra(StoreService.LOCATION, location);

        this.storeService.onHandleIntent(intent);

        verify(this.repository).moveDatabase(location);
    }

    @Test
    public void onHandleIntentWithEmptyLocationShouldNotCallMoveDatabase() throws Exception {
        ShadowApplication shadowApplication = Robolectric.getShadowApplication();
        String location = "";

        Intent intent = new Intent(shadowApplication.getApplicationContext(), StoreService.class);
        intent.setAction(StoreService.ACTION_MOVE);
        intent.putExtra(StoreService.LOCATION, location);

        this.storeService.onHandleIntent(intent);

        verify(this.repository, never()).moveDatabase(anyString());
    }

    @Test
    public void onHandleIntentWithLocationAndMoveDatabaseThrowsShouldNotThrow() throws Exception {
        ShadowApplication shadowApplication = Robolectric.getShadowApplication();
        String location = "/databasepath/reyna.db";

        Intent intent = new Intent(shadowApplication.getApplicationContext(), StoreService.class);
        intent.setAction(StoreService.ACTION_MOVE);
        intent.putExtra(StoreService.LOCATION, location);

        doThrow(Exception.class).when(this.repository).moveDatabase(anyString());

        this.storeService.onHandleIntent(intent);
    }

    @Test
    public void onHandleIntentWithLocationShouldNotStartForwardService() throws Exception {
        ShadowApplication shadowApplication = Robolectric.getShadowApplication();
        String location = "/databasepath/reyna.db";

        Intent intent = new Intent(shadowApplication.getApplicationContext(), StoreService.class);
        intent.setAction(StoreService.ACTION_MOVE);
        intent.putExtra(StoreService.LOCATION, location);

        this.storeService.onHandleIntent(intent);

        assertNull(shadowApplication.getNextStartedService());
    }

    @Test
    public void whenCallingOnHandleIntentWithNullIntentShouldNotThrow(){
        this.storeService.onHandleIntent(null);
    }
}
