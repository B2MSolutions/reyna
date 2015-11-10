package com.b2msolutions.reyna.system;

import android.app.AlarmManager;
import android.content.Context;
import com.b2msolutions.reyna.system.PeriodicBackoutCheck;
import com.b2msolutions.reyna.system.Preferences;
import com.xtremelabs.robolectric.RobolectricTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static junit.framework.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class PeriodicBackoutCheckTest {
    @Mock
    Preferences preferences;

    @Mock
    Context context;

    private PeriodicBackoutCheck periodicBackoutCheck;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        this.periodicBackoutCheck = new PeriodicBackoutCheck(context);
        this.periodicBackoutCheck.preferences = this.preferences;
    }

    @Test
    public void construction() {
        PeriodicBackoutCheck periodicBackoutCheck = new PeriodicBackoutCheck(context);
        assertNotNull(periodicBackoutCheck);
        assertNotNull(periodicBackoutCheck.preferences);
    }

    @Test
    public void recordShouldUpdateLastTime() {
        ArgumentCaptor<Long> argumentCaptor = ArgumentCaptor.forClass(Long.class);

        this.periodicBackoutCheck.record("task");

        verify(this.preferences).putLong(eq("task"), argumentCaptor.capture());
        Long value = argumentCaptor.getValue();
        assertTrue(value.longValue() > System.currentTimeMillis() - 2000 && value.longValue() <= System.currentTimeMillis());
    }

    @Test
    public void timeElapsedForTaskLastRunBeforeIntervalShouldReturnFalse() {
        doReturn(System.currentTimeMillis() - android.app.AlarmManager.INTERVAL_FIFTEEN_MINUTES)
                .when(this.preferences).getLong("task", -1);

        boolean result = this.periodicBackoutCheck.timeElapsed("task", AlarmManager.INTERVAL_HOUR);

        assertFalse(result);
    }

    @Test
    public void timeElapsedForTaskLastRunAfterIntervalShouldReturnTrue() {
        doReturn(System.currentTimeMillis() - AlarmManager.INTERVAL_HOUR - android.app.AlarmManager.INTERVAL_FIFTEEN_MINUTES)
                .when(this.preferences).getLong("task", -1);

        boolean result = this.periodicBackoutCheck.timeElapsed("task", AlarmManager.INTERVAL_HOUR);

        assertTrue(result);
    }

    @Test
    public void timeElapsedForLastRunInFutureShouldReturnTrue() {
        ArgumentCaptor<Long> argumentCaptor = ArgumentCaptor.forClass(Long.class);

        doReturn(System.currentTimeMillis() + AlarmManager.INTERVAL_HOUR)
                .when(this.preferences).getLong("task", -1);

        boolean result = this.periodicBackoutCheck.timeElapsed("task", AlarmManager.INTERVAL_HOUR);

        assertTrue(result);
        verify(this.preferences).putLong(eq("task"), argumentCaptor.capture());
        Long value = argumentCaptor.getValue();
        assertTrue(value.longValue() > System.currentTimeMillis() - 2000 && value.longValue() <= System.currentTimeMillis());
    }

    @Test
    public void timeElapsedForTaskForFirstTimeShouldReturnTrue() {
        doReturn(-1l).when(this.preferences).getLong("task", AlarmManager.INTERVAL_HOUR);

        boolean result = this.periodicBackoutCheck.timeElapsed("task", AlarmManager.INTERVAL_HOUR);

        assertTrue(result);
    }

    @Test
    public void timeElapsedForTaskForFirstTimeShouldReturnTrueWhenReturn0() {
        doReturn(0l).when(this.preferences).getLong("task", AlarmManager.INTERVAL_HOUR);

        boolean result = this.periodicBackoutCheck.timeElapsed("task", AlarmManager.INTERVAL_HOUR);

        assertTrue(result);
    }

    @Test
    public void whenCallingGetLastRecordedTimeShouldReturnExpected() {
        long time = System.currentTimeMillis() - AlarmManager.INTERVAL_HOUR;
        doReturn(time).when(this.preferences).getLong("task", -1);

        long actual = this.periodicBackoutCheck.getLastRecordedTime("task");

        assertEquals(time, actual);
    }
}
