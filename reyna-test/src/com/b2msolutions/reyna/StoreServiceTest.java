package com.b2msolutions.reyna;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.xtremelabs.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class StoreServiceTest {
	
	@Test
	public void testConstruction() {
        StoreService ss = new StoreService();
        assertNotNull(ss);
    }	
}
