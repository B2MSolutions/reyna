package com.b2msolutions.reyna;

import org.junit.Test;

public class ThreadTest {
    @Test
    public void callingSleepShouldNotThrow() throws InterruptedException {
        new Thread().sleep(1);
    }
}
