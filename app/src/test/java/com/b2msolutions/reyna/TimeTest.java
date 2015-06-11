package com.b2msolutions.reyna;

import org.junit.Test;

import java.security.InvalidParameterException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class TimeTest {
    @Test
    public void isBeforeShouldReturnExpected() {
        assertTrue(new Time(12, 00).isBeforeOrEqualTo(new Time(12, 01)));
        assertTrue(new Time(11, 00).isBeforeOrEqualTo(new Time(12, 00)));
        assertFalse(new Time(12, 01).isBeforeOrEqualTo(new Time(12, 00)));
        assertTrue(new Time(12, 01).isBeforeOrEqualTo(new Time(12, 01)));
    }

    @Test
    public void isAfterShouldReturnExpected() {
        assertFalse(new Time(12, 00).isAfterOrEqualTo(new Time(12, 01)));
        assertFalse(new Time(11, 00).isAfterOrEqualTo(new Time(12, 00)));
        assertTrue(new Time(12, 01).isAfterOrEqualTo(new Time(12, 00)));
        assertTrue(new Time(12, 01).isAfterOrEqualTo(new Time(12, 01)));
    }

    @Test
    public void shouldSetMinuteOfDay() {
        assertEquals(12 * 60 + 1, new Time(12, 01).getMinuteOfDay());
        assertEquals(1000, new Time(1000).getMinuteOfDay());
    }

    @Test(expected = InvalidParameterException.class)
    public void shouldThrowOnConstructionIfHourTooLarge() {
        new Time(24, 00);
    }

    @Test(expected = InvalidParameterException.class)
    public void shouldThrowOnConstructionIfMinutesTooLarge() {
        new Time(23, 60);
    }

    @Test(expected = InvalidParameterException.class)
    public void shouldThrowOnConstructionIfHourTooSmall() {
        new Time(-1, 00);
    }

    @Test(expected = InvalidParameterException.class)
    public void shouldThrowOnConstructionIfMinutesTooSmall() {
        new Time(23, -1);
    }

    @Test(expected = InvalidParameterException.class)
    public void shouldThrowOnConstructionIfMinuteOfDayTooSmall() {
        new Time(-1);
    }

    @Test(expected = InvalidParameterException.class)
    public void shouldThrowOnConstructionIfMinuteOfDayTooLarge() {
        new Time(24 * 60);
    }
}
