package com.b2msolutions.reyna;

import java.util.Calendar;

public class Time {

    private final int minuteOfDay;

    public Time(int hour, int minute) {
        this.minuteOfDay = (hour * 60) + minute;
    }

    public Time(int minuteOfDay) {
        this.minuteOfDay = minuteOfDay;
    }

    public Time() {
        Calendar cal = Calendar.getInstance();
        this.minuteOfDay = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
    }

    public int getMinuteOfDay() {
        return minuteOfDay;
    }

    public boolean isBefore(Time other) {
        return this.minuteOfDay < other.minuteOfDay;
    }

    public boolean isAfter(Time other) {
        return this.minuteOfDay > other.minuteOfDay;
    }
}
