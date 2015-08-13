package com.b2msolutions.reyna.services;

import android.content.Context;
import com.b2msolutions.reyna.Preferences;
import com.b2msolutions.reyna.Time;
import com.xtremelabs.robolectric.Robolectric;
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
        Context context = Robolectric.getShadowApplication().getApplicationContext();
        blackoutTime = new BlackoutTime(context);
    }

    @Test
    public void whenWlanRangeStoredShouldReturnFalseIfInsideRange() throws ParseException {
        Preferences preferences = new Preferences(Robolectric.getShadowApplication().getApplicationContext());
        preferences.saveWlanRange("01:00-09:00");
        Calendar now = new GregorianCalendar();
        now.set(Calendar.HOUR_OF_DAY, 6);
        now.set(Calendar.MINUTE, 30);
        assertFalse(blackoutTime.canSendOnWlan(now));
    }

    @Test
    public void whenWlanRangeStoredShouldReturnFalseIfInsideRangeForPM() throws ParseException {
        Preferences preferences = new Preferences(Robolectric.getShadowApplication().getApplicationContext());
        preferences.saveWlanRange("13:00-14:00");
        Calendar now = new GregorianCalendar();
        now.set(Calendar.HOUR_OF_DAY, 13);
        now.set(Calendar.MINUTE, 30);
        assertFalse(blackoutTime.canSendOnWlan(now));
    }

    @Test
    public void whenWlanRangeStoredShouldReturnTrueIfOutsideRange() throws ParseException {
        Preferences preferences = new Preferences(Robolectric.getShadowApplication().getApplicationContext());
        preferences.saveWlanRange("01:00-09:00");
        Calendar now = new GregorianCalendar();
        now.set(Calendar.HOUR_OF_DAY, 9);
        now.set(Calendar.MINUTE, 45);
        assertTrue(blackoutTime.canSendOnWlan(now));
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
    public void whenWlanTimeRangeHasSameTimesShouldReturnTrue() throws ParseException {
        Preferences preferences = new Preferences(Robolectric.getShadowApplication().getApplicationContext());
        preferences.saveWlanRange("01:00-01:00");
        Calendar now = new GregorianCalendar();
        now.set(Calendar.HOUR_OF_DAY, 1);
        now.set(Calendar.MINUTE, 0);
        assertTrue(blackoutTime.canSendOnWlan(now));
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
    public void whenWlanRangeFromIsGreaterThanToShouldReturnFalse() throws ParseException {
        Preferences preferences = new Preferences(Robolectric.getShadowApplication().getApplicationContext());
        preferences.saveWlanRange("17:30-09:00");
        Calendar now = new GregorianCalendar();
        now.set(Calendar.HOUR_OF_DAY, 18);
        now.set(Calendar.MINUTE, 10);
        assertFalse(blackoutTime.canSendOnWlan(now));
    }

    @Test
    public void whenWlanRangeFromIsGreaterThanToAndWeAreOutsideRangeShouldReturnTrue() throws ParseException {
        Preferences preferences = new Preferences(Robolectric.getShadowApplication().getApplicationContext());
        preferences.saveWlanRange("17:30-09:00");
        Calendar now = new GregorianCalendar();
        now.set(Calendar.HOUR_OF_DAY, 10);
        now.set(Calendar.MINUTE, 10);
        assertTrue(blackoutTime.canSendOnWlan(now));
    }

    @Test
    public void whenWlanRangeStoredShouldReturnFalseIfInsideMultipleRanges() throws ParseException {
        Preferences preferences = new Preferences(Robolectric.getShadowApplication().getApplicationContext());
        preferences.saveWlanRange("02:00-03:00,05:00-07:30,18:00-18:15");
        Calendar now = new GregorianCalendar();
        now.set(Calendar.HOUR_OF_DAY, 18);
        now.set(Calendar.MINUTE, 10);
        assertFalse(blackoutTime.canSendOnWlan(now));
    }

    @Test
    public void canSendOnWlanShouldAlwaysReturnFalseWhenInTotalBlackout() throws ParseException {
        Preferences preferences = new Preferences(Robolectric.getShadowApplication().getApplicationContext());
        preferences.saveWlanRange("00:00-23:59");
        Calendar now = new GregorianCalendar();
        now.set(Calendar.HOUR_OF_DAY, 0);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);
        assertFalse(blackoutTime.canSendOnWlan(now));

        now = new GregorianCalendar();
        now.set(Calendar.HOUR_OF_DAY, 23);
        now.set(Calendar.MINUTE, 59);
        now.set(Calendar.SECOND, 30);
        assertFalse(blackoutTime.canSendOnWlan(now));

        now = new GregorianCalendar();
        now.set(Calendar.HOUR_OF_DAY, 10);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);
        assertFalse(blackoutTime.canSendOnWlan(now));
    }

    @Test
    public void canSendOnWlanShouldAlwaysReturnTrueWhenNoConfigurationPresent() throws ParseException {
        Calendar now = new GregorianCalendar();
        assertTrue(blackoutTime.canSendOnWlan(now));
    }

    @Test
    public void canSendOnWwanShouldReturnFalseWhenInsideTheRange() throws ParseException {
        Preferences preferences = new Preferences(Robolectric.getShadowApplication().getApplicationContext());
        preferences.saveWwanRange("01:00-09:00");
        Calendar now = new GregorianCalendar();
        now.set(Calendar.HOUR_OF_DAY, 6);
        now.set(Calendar.MINUTE, 30);
        assertFalse(blackoutTime.canSendOnWwan(now));
    }
}
