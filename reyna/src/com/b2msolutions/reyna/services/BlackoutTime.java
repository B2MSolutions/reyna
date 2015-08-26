package com.b2msolutions.reyna.services;

import android.text.TextUtils;
import com.b2msolutions.reyna.Time;
import com.b2msolutions.reyna.TimeRange;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class BlackoutTime {

    public boolean canSendAtTime(Calendar now, String range) throws ParseException {
        if (TextUtils.isEmpty(range)) return true;

        String[] rangesSplit = range.split(",");
        for (String rangeSplit : rangesSplit) {
            TimeRange timeRange = parseTime(rangeSplit);
            Time timeNow = new Time(now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE));
            if (timeRange.contains(timeNow) && !timeRange.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public TimeRange parseTime(String time) throws ParseException {
        String[] rangeSplit = time.split("-");
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");

        Calendar from = new GregorianCalendar();
        from.setTime(dateFormat.parse(rangeSplit[0]));
        Calendar to = new GregorianCalendar();
        to.setTime(dateFormat.parse(rangeSplit[1]));


        Time toTime = new Time(from.get(Calendar.HOUR_OF_DAY), from.get(Calendar.MINUTE));
        Time fromTime = new Time(to.get(Calendar.HOUR_OF_DAY), to.get(Calendar.MINUTE));
        return new TimeRange(toTime, fromTime);
    }
}
