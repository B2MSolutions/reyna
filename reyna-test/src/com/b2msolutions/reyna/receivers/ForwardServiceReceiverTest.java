package com.b2msolutions.reyna.receivers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Context;
import android.content.Intent;

import com.b2msolutions.reyna.services.ForwardService;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ForwardServiceReceiverTest {

	private ForwardServiceReceiver receiver;

	@Before
	public void setup() {
		this.receiver = new ForwardServiceReceiver();
	}

	@Test
	public void testConstruction() {
		assertNotNull(this.receiver);
	}

	@Test
	public void receiveShouldStartForwardService() {
		Context context = Robolectric.application.getApplicationContext();
		this.receiver.onReceive(context, null);	
		Intent service = Robolectric.getShadowApplication().getNextStartedService();
		assertNotNull(service);
		assertEquals(ForwardService.class.getName(), service.getComponent().getClassName());
	}
}
