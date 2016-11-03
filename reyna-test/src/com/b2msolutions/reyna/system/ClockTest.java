package com.b2msolutions.reyna.system;

import org.junit.Assert;
import org.junit.Test;

public class ClockTest {
    @Test
    public void whenCallingCurrentTimeMillisShouldReturnCurrentTimeMillis() {
        Clock clock = new Clock();
        Assert.assertTrue(clock.getCurrentTimeMillis() <= System.currentTimeMillis());
    }
}
