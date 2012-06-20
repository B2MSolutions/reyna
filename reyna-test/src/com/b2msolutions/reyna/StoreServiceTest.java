package com.b2msolutions.reyna;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Intent;

import com.xtremelabs.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class StoreServiceTest {
	
	private StoreService storeService;

	@Before
	public void setup() {
		this.storeService = new StoreService();
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
	public void onHandleIntentWithMessageShouldStore() {
		Message message = RepositoryTest.getMessageWithHeaders();
		Intent intent = new Intent();
		intent.putExtra(StoreService.MESSAGE, message);
		
		Repository repository = new Repository(this.storeService);
		this.storeService.store = repository;
		
		this.storeService.onHandleIntent(intent);
		
		RepositoryTest.assertMessage(repository, message);
	}
}
