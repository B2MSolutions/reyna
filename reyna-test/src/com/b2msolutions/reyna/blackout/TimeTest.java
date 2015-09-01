package com.b2msolutions.reyna.blackout;

import org.junit.Test;

import java.security.InvalidParameterException;

import static junit.framework.Assert.*;

public class TimeTest {
    @Test
    public void isBeforeShouldReturnExpected() {
        assertTrue(new com.b2msolutions.reyna.blackout.Time(12, 00).isBeforeOrEqualTo(new com.b2msolutions.reyna.blackout.Time(12, 01)));
        assertTrue(new com.b2msolutions.reyna.blackout.Time(11, 00).isBeforeOrEqualTo(new com.b2msolutions.reyna.blackout.Time(12, 00)));
        assertFalse(new com.b2msolutions.reyna.blackout.Time(12, 01).isBeforeOrEqualTo(new com.b2msolutions.reyna.blackout.Time(12, 00)));
        assertTrue(new com.b2msolutions.reyna.blackout.Time(12, 01).isBeforeOrEqualTo(new com.b2msolutions.reyna.blackout.Time(12, 01)));
    }

    @Test
    public void isAfterShouldReturnExpected() {
        assertFalse(new com.b2msolutions.reyna.blackout.Time(12, 00).isAfterOrEqualTo(new com.b2msolutions.reyna.blackout.Time(12, 01)));
        assertFalse(new com.b2msolutions.reyna.blackout.Time(11, 00).isAfterOrEqualTo(new com.b2msolutions.reyna.blackout.Time(12, 00)));
        assertTrue(new com.b2msolutions.reyna.blackout.Time(12, 01).isAfterOrEqualTo(new com.b2msolutions.reyna.blackout.Time(12, 00)));
        assertTrue(new com.b2msolutions.reyna.blackout.Time(12, 01).isAfterOrEqualTo(new com.b2msolutions.reyna.blackout.Time(12, 01)));
    }

    @Test
    public void shouldSetMinuteOfDay() {
        assertEquals(12 * 60 + 1, new com.b2msolutions.reyna.blackout.Time(12, 01).getMinuteOfDay());
        assertEquals(1000, new com.b2msolutions.reyna.blackout.Time(1000).getMinuteOfDay());
    }

    @Test(expected = InvalidParameterException.class)
    public void shouldThrowOnConstructionIfHourTooLarge() {
        new com.b2msolutions.reyna.blackout.Time(24, 00);
    }

    @Test(expected = InvalidParameterException.class)
    public void shouldThrowOnConstructionIfMinutesTooLarge() {
        new com.b2msolutions.reyna.blackout.Time(23, 60);
    }

    @Test(expected = InvalidParameterException.class)
    public void shouldThrowOnConstructionIfHourTooSmall() {
        new com.b2msolutions.reyna.blackout.Time(-1, 00);
    }

    @Test(expected = InvalidParameterException.class)
    public void shouldThrowOnConstructionIfMinutesTooSmall() {
        new com.b2msolutions.reyna.blackout.Time(23, -1);
    }

    @Test(expected = InvalidParameterException.class)
    public void shouldThrowOnConstructionIfMinuteOfDayTooSmall() {
        new com.b2msolutions.reyna.blackout.Time(-1);
    }

    @Test(expected = InvalidParameterException.class)
    public void shouldThrowOnConstructionIfMinuteOfDayTooLarge() {
        new com.b2msolutions.reyna.blackout.Time(24 * 60);
    }
}
