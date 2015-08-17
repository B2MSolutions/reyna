package com.b2msolutions.reyna.services;

import com.b2msolutions.reyna.Time;
import com.xtremelabs.robolectric.RobolectricTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;

import java.text.ParseException;
import java.util.*;

import static junit.framework.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class BlackoutTimeTest {

    private BlackoutTime blackoutTime;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        blackoutTime = new BlackoutTime();
    }

    @Test
    public void whenRangeStoredShouldReturnFalseIfInsideRange() throws ParseException {
        Calendar now = new GregorianCalendar();
        now.set(Calendar.HOUR_OF_DAY, 6);
        now.set(Calendar.MINUTE, 30);
        assertFalse(blackoutTime.canSendAtTime(now, "01:00-09:00"));
    }

    @Test
    public void whenRangeStoredShouldReturnFalseIfInsideRangeForPM() throws ParseException {
        Calendar now = new GregorianCalendar();
        now.set(Calendar.HOUR_OF_DAY, 13);
        now.set(Calendar.MINUTE, 30);
        assertFalse(blackoutTime.canSendAtTime(now, "13:00-14:00"));
    }

    @Test
    public void whenRangeStoredShouldReturnTrueIfOutsideRange() throws ParseException {
        Calendar now = new GregorianCalendar();
        now.set(Calendar.HOUR_OF_DAY, 9);
        now.set(Calendar.MINUTE, 45);
        assertTrue(blackoutTime.canSendAtTime(now, "01:00-09:00"));
    }

    @Test
    public void parseTimeWorksWithAmDates() throws ParseException {
        List<Time> actual = blackoutTime.parseTime("01:00-09:00");
        assertEquals(actual.get(0).getMinuteOfDay(), new Time(1, 0).getMinuteOfDay());
        assertEquals(actual.get(1).getMinuteOfDay(), new Time(9, 0).getMinuteOfDay());
    }

    @Test
    public void parseTimeWorksWithAmDatesAndMinutes() throws ParseException {
        List<Time> actual = blackoutTime.parseTime("01:30-09:15");
        assertEquals(actual.get(0).getMinuteOfDay(), new Time(1, 30).getMinuteOfDay());
        assertEquals(actual.get(1).getMinuteOfDay(), new Time(9, 15).getMinuteOfDay());
    }

    @Test
    public void parseTimeWorksWithPmDatesAndMinutes() throws ParseException {
        List<Time> actual = blackoutTime.parseTime("13:30-21:15");
        assertEquals(actual.get(0).getMinuteOfDay(), new Time(13, 30).getMinuteOfDay());
        assertEquals(actual.get(1).getMinuteOfDay(), new Time(21, 15).getMinuteOfDay());
    }

    @Test
    public void parseTimeWorksWithPmDates() throws ParseException {
        List<Time> actual = blackoutTime.parseTime("13:00-21:00");
        assertEquals(actual.get(0).getMinuteOfDay(), new Time(13, 0).getMinuteOfDay());
        assertEquals(actual.get(1).getMinuteOfDay(), new Time(21, 0).getMinuteOfDay());
    }

    @Test
    public void whenTimeRangeHasSameTimesShouldReturnTrue() throws ParseException {
        Calendar now = new GregorianCalendar();
        now.set(Calendar.HOUR_OF_DAY, 1);
        now.set(Calendar.MINUTE, 0);
        assertTrue(blackoutTime.canSendAtTime(now, "01:00-01:00"));
    }

    @Test
    public void parseTimeParsesWhenMalformedRangeTimeWithSpaces() {
        try {
            List<Time> actual = blackoutTime.parseTime("13:00 - 21:00");
            assertEquals(actual.get(0).getMinuteOfDay(), new Time(13, 0).getMinuteOfDay());
            assertEquals(actual.get(1).getMinuteOfDay(), new Time(21, 0).getMinuteOfDay());
        } catch (ParseException e) {
            assertNull(e);
        }
    }

    @Test
    public void whenRangeFromIsGreaterThanToShouldReturnFalse() throws ParseException {
        Calendar now = new GregorianCalendar();
        now.set(Calendar.HOUR_OF_DAY, 18);
        now.set(Calendar.MINUTE, 10);
        assertFalse(blackoutTime.canSendAtTime(now, "17:30-09:00"));
    }

    @Test
    public void whenRangeFromIsGreaterThanToAndWeAreOutsideRangeShouldReturnTrue() throws ParseException {
        Calendar now = new GregorianCalendar();
        now.set(Calendar.HOUR_OF_DAY, 10);
        now.set(Calendar.MINUTE, 10);
        assertTrue(blackoutTime.canSendAtTime(now, "17:30-09:00"));
    }

    @Test
    public void whenRangeStoredShouldReturnFalseIfInsideMultipleRanges() throws ParseException {
        Calendar now = new GregorianCalendar();
        now.set(Calendar.HOUR_OF_DAY, 18);
        now.set(Calendar.MINUTE, 10);
        assertFalse(blackoutTime.canSendAtTime(now, "02:00-03:00,05:00-07:30,18:00-18:15"));
    }

    @Test
    public void canSendOnShouldAlwaysReturnFalseWhenInTotalBlackout() throws ParseException {
        Calendar now = new GregorianCalendar();
        now.set(Calendar.HOUR_OF_DAY, 0);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);
        assertFalse(blackoutTime.canSendAtTime(now, "00:00-23:59"));

        now = new GregorianCalendar();
        now.set(Calendar.HOUR_OF_DAY, 23);
        now.set(Calendar.MINUTE, 59);
        now.set(Calendar.SECOND, 30);
        assertFalse(blackoutTime.canSendAtTime(now, "00:00-23:59"));

        now = new GregorianCalendar();
        now.set(Calendar.HOUR_OF_DAY, 10);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);
        assertFalse(blackoutTime.canSendAtTime(now, "00:00-23:59"));
    }

    @Test
    public void canSendOnShouldAlwaysReturnTrueWhenNoConfigurationPresent() throws ParseException {
        Calendar now = new GregorianCalendar();
        assertTrue(blackoutTime.canSendAtTime(now, ""));
    }
}
