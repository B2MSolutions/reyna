package com.b2msolutions.reyna;

import org.junit.Test;

import java.lang.*;
import java.security.InvalidParameterException;

import static junit.framework.Assert.*;

public class ThreadTest {
    @Test
    public void callingSleepShouldNotThrow() throws InterruptedException {
        new Thread().sleep(1);
    }
}
