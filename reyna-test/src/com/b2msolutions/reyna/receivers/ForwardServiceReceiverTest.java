package com.b2msolutions.reyna.receivers;

import android.content.Context;
import android.content.Intent;
import com.b2msolutions.reyna.services.ForwardService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.robolectric.Shadows.shadowOf;

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
        Context context = RuntimeEnvironment.application.getApplicationContext();
        this.receiver.onReceive(context, null);
        Intent service = shadowOf(RuntimeEnvironment.application).getNextStartedService();
        assertNotNull(service);
        assertEquals(ForwardService.class.getName(), service.getComponent().getClassName());
    }
}
