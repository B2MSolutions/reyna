package com.b2msolutions.reyna;

import java.security.InvalidParameterException;
import java.util.Calendar;

public class Time {

    private final int minuteOfDay;

    public Time(int hour, int minute) {
        if(hour >= 24 || minute >= 60 || hour < 0 || minute < 0) {
            throw new InvalidParameterException("Invalid time");
        }

        this.minuteOfDay = (hour * 60) + minute;
    }

    public Time(int minuteOfDay) {
        if(minuteOfDay < 0 || minuteOfDay >= 1440) {
            throw new InvalidParameterException("Invalid minute of day");
        }

        this.minuteOfDay = minuteOfDay;
    }

    public Time() {
        Calendar cal = Calendar.getInstance();
        this.minuteOfDay = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
    }

    public int getMinuteOfDay() {
        return minuteOfDay;
    }

    public boolean isBeforeOrEqualTo(Time other) {
        return this.minuteOfDay <= other.minuteOfDay;
    }

    public boolean isAfterOrEqualTo(Time other) {
        return this.minuteOfDay >= other.minuteOfDay;
    }
}
