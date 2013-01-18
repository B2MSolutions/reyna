package com.b2msolutions.reyna.services;

import android.content.Intent;
import com.b2msolutions.reyna.Message;
import com.b2msolutions.reyna.Repository;
import com.b2msolutions.reyna.RepositoryTest;
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
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
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
}
