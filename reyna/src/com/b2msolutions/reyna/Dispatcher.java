package com.b2msolutions.reyna;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.http.AndroidHttpClient;
import android.text.TextUtils;
import com.b2msolutions.reyna.blackout.TimeRange;
import com.b2msolutions.reyna.http.HttpPost;
import com.b2msolutions.reyna.blackout.BlackoutTime;
import com.b2msolutions.reyna.system.Header;
import com.b2msolutions.reyna.system.Logger;
import com.b2msolutions.reyna.system.Message;
import com.b2msolutions.reyna.system.Preferences;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;

import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Locale;

public class Dispatcher {

    private static final String TAG = "com.b2msolutions.reyna.Dispatcher";

    public enum Result {
        OK, PERMANENT_ERROR, TEMPORARY_ERROR, BLACKOUT, NOTCONNECTED
    }

    public Result sendMessage(Context context, Message message) {
        Logger.v(TAG, "sendMessage");

        AndroidHttpClient httpClient = AndroidHttpClient.newInstance("Reyna", context);
        try {
            return this.sendMessage(message, new HttpPost(), httpClient, context);
        } finally {
            httpClient.close();
        }
    }

    protected Result sendMessage(Message message, HttpPost httpPost, HttpClient httpClient, Context context) {
        Logger.v(TAG, "sendMessage: injected");

        Result result = Dispatcher.canSend(context);
        if(result != Result.OK) {
            return result;
        }

        result = this.parseHttpPost(message, httpPost, context);
        if (result != Result.OK) {
            return result;
        }

        return this.tryToExecute(httpPost, httpClient);
    }

    public static Result canSend(Context context) {
        return canSend(context, new GregorianCalendar());
    }

    protected static Result canSend(Context context, GregorianCalendar now) {
        Logger.v(TAG, "canSend start");
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info == null || !info.isConnectedOrConnecting()) {
            Logger.v(TAG, "not connected");
            return Result.NOTCONNECTED;
        }

        Preferences preferences = new Preferences(context);
        BlackoutTime blackoutTime = new BlackoutTime();

        String startTime = preferences.getNonRecurringWwanBlackoutStartTimeAsString();
        String endTime = preferences.getNonRecurringWwanBlackoutEndTimeAsString();
        if (isMobile(info) && isNonRecurringBlackout(startTime, endTime, now.getTimeInMillis())) {
            Logger.v(TAG, "blackout because mobile and current time is within non recurring WWAN blackout period");
            return Result.BLACKOUT;
        }

        if (TextUtils.isEmpty(preferences.getWwanBlackout())) {
            Logger.v(TAG, "save cellular data backward compatibility");
            saveCellularDataAsWwanForBackwardCompatibility(preferences);
        }

        if (Dispatcher.isBatteryCharging(context) && !preferences.canSendOnCharge()) {
            Logger.v(TAG, "blackout because charging and cant send on charge");
            return Result.BLACKOUT;
        }
        if (!Dispatcher.isBatteryCharging(context) && !preferences.canSendOffCharge()) {
            Logger.v(TAG, "blackout because not charging and cant send off charge");
            return Result.BLACKOUT;
        }
        if (isRoaming(info) && !preferences.canSendOnRoaming()) {
            Logger.v(TAG, "blackout because roaming and cant send on roaming");
            return Result.BLACKOUT;
        }
        try {
            if (isWifi(info) && !canSendNow(blackoutTime, preferences.getWlanBlackout(), now)) {
                Logger.v(TAG, "blackout because wifi and cant send at " + preferences.getWlanBlackout());
                return Result.BLACKOUT;
            }
            if (isMobile(info) && !canSendNow(blackoutTime, preferences.getWwanBlackout(), now)) {
                Logger.v(TAG, "blackout because mobile and cant send at " + preferences.getWwanBlackout());
                return Result.BLACKOUT;
            }
        } catch (ParseException e) {
            Logger.w(TAG, "canSend", e);
            return Result.OK;
        }

        Logger.v(TAG, "canSend ok");
        return Result.OK;
    }

    private static boolean canSendNow(BlackoutTime blackoutTime, String window, GregorianCalendar now) throws ParseException {
        return blackoutTime.canSendAtTime(now, window);
    }

    private static boolean isNonRecurringBlackout(String startUtc, String endUtc, long nowUtc) {
        if(startUtc == null || startUtc.isEmpty() || endUtc == null || endUtc.isEmpty()) {
            return false;
        }

        return nowUtc >= Long.parseLong(startUtc) && nowUtc < Long.parseLong(endUtc);
    }

    private static void saveCellularDataAsWwanForBackwardCompatibility(Preferences preferences) {
        TimeRange timeRange = preferences.getCellularDataBlackout();
        if(timeRange != null) {

            int hourFrom = (int) Math.floor(timeRange.getFrom().getMinuteOfDay() / 60);
            int minuteFrom = timeRange.getFrom().getMinuteOfDay() % 60;
            String blackoutFrom = zeroPad(hourFrom) + ":" + zeroPad(minuteFrom);

            int hourTo = (int) Math.floor(timeRange.getTo().getMinuteOfDay() / 60);
            int minuteTo = timeRange.getTo().getMinuteOfDay() % 60;

            String blackoutTo = zeroPad(hourTo) + ":" + zeroPad(minuteTo);
            preferences.saveWwanBlackout(blackoutFrom + "-" + blackoutTo);
        }
    }

    private static String zeroPad(int toBePadded) {
        return String.format(Locale.US,"%02d", toBePadded);
    }

    private static boolean isRoaming(NetworkInfo info) {
        return info.isRoaming();
    }

    private static boolean isWifi(NetworkInfo info) {
        return info.getType() == ConnectivityManager.TYPE_WIFI;
    }

    private static boolean isMobile(NetworkInfo info) {
        int type = info.getType();
        return type == ConnectivityManager.TYPE_MOBILE ||
            type == ConnectivityManager.TYPE_MOBILE_DUN ||
            type == ConnectivityManager.TYPE_MOBILE_HIPRI ||
            type == ConnectivityManager.TYPE_MOBILE_MMS ||
            type == ConnectivityManager.TYPE_MOBILE_SUPL ||
            type == ConnectivityManager.TYPE_WIMAX;
    }

    private Result parseHttpPost(Message message, HttpPost httpPost, Context context) {
        Logger.v(TAG, "parseHttpPost");

        try {
            URI uri = message.getURI();
            httpPost.setURI(uri);

            AbstractHttpEntity entity = Dispatcher.getEntity(message, context);
            httpPost.setEntity(entity);

            Dispatcher.setHeaders(httpPost, message.getHeaders());

            return Result.OK;
        } catch (Exception e) {
            Logger.e(TAG, "parseHttpPost", e);
            return Result.PERMANENT_ERROR;
        }
    }

    private Result tryToExecute(HttpPost httpPost, HttpClient httpClient) {
        Logger.v(TAG, "tryToExecute");

        try {
            HttpResponse response = httpClient.execute(httpPost);
            return Dispatcher.getResult(response.getStatusLine().getStatusCode());
        } catch (Exception e) {
            Logger.d(TAG, "tryToExecute", e);
            Logger.i(TAG, "tryToExecute: temporary error");
            return Result.TEMPORARY_ERROR;
        }
    }

    protected static Result getResult(int statusCode) {
        Logger.v(TAG, "getResult: " + statusCode);

        if (statusCode >= 200 && statusCode < 300)
            return Result.OK;
        if (statusCode >= 300 && statusCode < 500)
            return Result.PERMANENT_ERROR;
        if (statusCode >= 500 && statusCode < 600)
            return Result.TEMPORARY_ERROR;

        return Result.PERMANENT_ERROR;
    }

    private static boolean shouldGzip(Header[] headers) {
        for (Header header : headers) {
            if (header.getKey().equalsIgnoreCase("content-encoding")
                    && header.getValue().equalsIgnoreCase("gzip")) {
                return true;
            }
        }

        return false;
    }

    private static Header[] removeGzipEncodingHeader(Header[] headers) {
        ArrayList<Header> filteredHeaders = new ArrayList<Header>();

        for (Header header : headers) {
            if (header.getKey().equalsIgnoreCase("content-encoding")
                    && header.getValue().equalsIgnoreCase("gzip")) {
                continue;
            }

            filteredHeaders.add(header);
        }

        Header[] returnedHeaders = new Header[filteredHeaders.size()];
        return filteredHeaders.toArray(returnedHeaders);
    }

    private static AbstractHttpEntity getCompressedEntity(String content, Context context) throws Exception {
        byte[] data = content.getBytes();
        return AndroidHttpClient.getCompressedEntity(data, context.getContentResolver());
    }

    private static void setHeaders(HttpPost httpPost, Header[] headers) {
        Header[] filteredHeaders = Dispatcher.removeGzipEncodingHeader(headers);

        for (Header header : filteredHeaders) {
            httpPost.setHeader(header.getKey(), header.getValue());
        }
    }

    private static AbstractHttpEntity getEntity(Message message, Context context) throws Exception {
        String content = message.getBody();
        AbstractHttpEntity entity = new StringEntity(content, HTTP.UTF_8);

        if (Dispatcher.shouldGzip(message.getHeaders())) {
            entity = getCompressedEntity(content, context);
        }

        return entity;
    }

    public static boolean isBatteryCharging(Context context) {
        Intent batteryStatus = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if(batteryStatus == null) return false;
        Integer plugged = batteryStatus.getIntExtra(android.os.BatteryManager.EXTRA_PLUGGED, -1);
        return plugged == android.os.BatteryManager.BATTERY_PLUGGED_AC ||
                plugged == android.os.BatteryManager.BATTERY_PLUGGED_USB ||
                // wireless!
                plugged == 4 ||
                // unknown
                plugged == 3;
    }
}
