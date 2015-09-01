package com.b2msolutions.reyna.blackout;

import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class TimeRangeTest {
    @Test
    public void whenTimeIsWithinRangeContainsShouldReturnTrue() {
        com.b2msolutions.reyna.blackout.Time time = new com.b2msolutions.reyna.blackout.Time(12, 00);
        com.b2msolutions.reyna.blackout.TimeRange range = new com.b2msolutions.reyna.blackout.TimeRange(new com.b2msolutions.reyna.blackout.Time(11, 00), new com.b2msolutions.reyna.blackout.Time(12, 01));
        assertTrue(range.contains(time));

        time = new com.b2msolutions.reyna.blackout.Time(11, 02);
        assertTrue(range.contains(time));

        time = new com.b2msolutions.reyna.blackout.Time(04, 00);
        range = new com.b2msolutions.reyna.blackout.TimeRange(new com.b2msolutions.reyna.blackout.Time(21, 00), new com.b2msolutions.reyna.blackout.Time(05, 00));
        assertTrue(range.contains(time));

        range = new com.b2msolutions.reyna.blackout.TimeRange(new com.b2msolutions.reyna.blackout.Time(00, 00), new com.b2msolutions.reyna.blackout.Time(23, 59));
        time = new com.b2msolutions.reyna.blackout.Time(23, 59);
        assertTrue(range.contains(time));
        time = new com.b2msolutions.reyna.blackout.Time(00, 00);
        assertTrue(range.contains(time));
        time = new com.b2msolutions.reyna.blackout.Time(00, 01);
        assertTrue(range.contains(time));
        time = new com.b2msolutions.reyna.blackout.Time(12, 00);
        assertTrue(range.contains(time));
    }

    @Test
    public void whenTimeIsNotWithinRangeContainsShouldReturnFalse() {
        com.b2msolutions.reyna.blackout.Time time = new com.b2msolutions.reyna.blackout.Time(12, 02);
        com.b2msolutions.reyna.blackout.TimeRange range = new com.b2msolutions.reyna.blackout.TimeRange(new com.b2msolutions.reyna.blackout.Time(11, 00), new com.b2msolutions.reyna.blackout.Time(12, 01));
        assertFalse(range.contains(time));

        time = new com.b2msolutions.reyna.blackout.Time(19, 58);
        range = new TimeRange(new com.b2msolutions.reyna.blackout.Time(19, 59), new com.b2msolutions.reyna.blackout.Time(19, 57));
        assertFalse(range.contains(time));
    }
}
