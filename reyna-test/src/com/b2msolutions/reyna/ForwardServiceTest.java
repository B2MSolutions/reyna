package com.b2msolutions.reyna;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Intent;

import com.xtremelabs.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ForwardServiceTest {
	
	private ForwardService forwardService;

	@Before
	public void setup() {
		this.forwardService = new ForwardService();
	}
	
	@Test
	public void testConstruction() {        
        assertNotNull(this.forwardService);
    }	

	@Test
	public void onHandleIntentWithNullIntentShouldNotThrow() {
		this.forwardService.onHandleIntent(null);
	}
		
	@Test
	public void onHandleIntentAndMessagesSentShouldHaveNoMessagesInRepository() {
		Repository repository = new Repository(this.forwardService);		

		Message message = RepositoryTest.getMessageWithHeaders();
		repository.insert(message);
		repository.insert(message);
		
		this.forwardService.store = repository;
		
		this.forwardService.onHandleIntent(new Intent());
		
		assertNull(repository.getNext());		
	}
}
