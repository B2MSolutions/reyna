package com.b2msolutions.reyna;

public class TimeRange {

    private final Time from;

    private final Time to;

    public TimeRange(Time from, Time to) {
        this.from = from;
        this.to = to;
    }

    public Time getTo() {
        return to;
    }

    public Time getFrom() {
        return from;
    }

    public boolean contains(Time time) {
        return time.isAfter(this.from) && time.isBefore(this.to);
    }
}
