package com.b2msolutions.reyna.services;

import android.content.Context;
import com.b2msolutions.reyna.Preferences;
import com.b2msolutions.reyna.Time;
import com.b2msolutions.reyna.TimeRange;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class BlackoutTime {

    private final Context context;

    public BlackoutTime(Context context) {
        this.context = context;
    }

    public boolean canSendOnWlan(Calendar now) throws ParseException {
        Preferences preferences = new Preferences(context);
        String range = preferences.getWlanRange();
        return canSendAtTime(now, range);
    }

    public boolean canSendOnWwan(Calendar now) throws ParseException {
        Preferences preferences = new Preferences(context);
        String range = preferences.getWwanRange();
        return canSendAtTime(now, range);
    }

    private boolean canSendAtTime(Calendar now, String range) throws ParseException {
        if (range.length() == 0) return true;

        String[] rangesSplit = range.split(",");
        for (String rangeSplit : rangesSplit) {
            List<Time> times = parseTime(rangeSplit);
            TimeRange timeRange = new TimeRange(times.get(0), times.get(1));
            if (timeRange.contains(new Time(now.get(Calendar.HOUR_OF_DAY),1))) {
                return false;
            }
        }
        return true;
    }

    public List<Time> parseTime(String time) throws ParseException {
        String[] rangeSplit = time.split("-");
        List<Time> times = new ArrayList<Time>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");

        Calendar from = new GregorianCalendar();
        from.setTime(dateFormat.parse(rangeSplit[0]));
        Calendar to = new GregorianCalendar();
        to.setTime(dateFormat.parse(rangeSplit[1]));

        times.add(new Time(from.get(Calendar.HOUR_OF_DAY), from.get(Calendar.MINUTE)));
        times.add(new Time(to.get(Calendar.HOUR_OF_DAY), to.get(Calendar.MINUTE)));
        return times;
    }
}
