package com.b2msolutions.reyna;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class TimeTest {
    @Test
    public void isBeforeShouldReturnExpected() {
        assertTrue(new Time(12, 00).isBefore(new Time(12, 01)));
        assertTrue(new Time(11, 00).isBefore(new Time(12, 00)));
        assertFalse(new Time(12, 01).isBefore(new Time(12, 00)));
    }

    @Test
    public void isAfterShouldReturnExpected() {
        assertFalse(new Time(12, 00).isAfter(new Time(12, 01)));
        assertFalse(new Time(11, 00).isAfter(new Time(12, 00)));
        assertTrue(new Time(12, 01).isAfter(new Time(12, 00)));
    }

    @Test
    public void shouldSetMinuteOfDay() {
        assertEquals(12 * 60 + 1, new Time(12, 01).getMinuteOfDay());
        assertEquals(10000, new Time(10000).getMinuteOfDay());
    }
}
