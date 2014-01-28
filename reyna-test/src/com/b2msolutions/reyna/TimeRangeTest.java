package com.b2msolutions.reyna;

import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class TimeRangeTest {
    @Test
    public void whenTimeIsWithinRangeContainsShouldReturnTrue() {
        Time time = new Time(12, 00);
        TimeRange range = new TimeRange(new Time(11, 00), new Time(12, 01));
        assertTrue(range.contains(time));

        time = new Time(11, 02);
        assertTrue(range.contains(time));

        time = new Time(04, 00);
        range = new TimeRange(new Time(21, 00), new Time(05, 00));
        assertTrue(range.contains(time));

        range = new TimeRange(new Time(00, 00), new Time(23, 59));
        time = new Time(23, 59);
        assertTrue(range.contains(time));
        time = new Time(00, 00);
        assertTrue(range.contains(time));
        time = new Time(00, 01);
        assertTrue(range.contains(time));
        time = new Time(12, 00);
        assertTrue(range.contains(time));
    }

    @Test
    public void whenTimeIsNotWithinRangeContainsShouldReturnFalse() {
        Time time = new Time(12, 02);
        TimeRange range = new TimeRange(new Time(11, 00), new Time(12, 01));
        assertFalse(range.contains(time));

        time = new Time(19, 58);
        range = new TimeRange(new Time(19, 59), new Time(19, 57));
        assertFalse(range.contains(time));
    }
}
