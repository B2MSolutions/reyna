package com.b2msolutions.reyna.system;

import org.junit.Test;

public class ThreadTest {
    @Test
    public void callingSleepShouldNotThrow() throws InterruptedException {
        new com.b2msolutions.reyna.system.Thread().sleep(1);
    }
}
