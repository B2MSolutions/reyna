package com.b2msolutions.reyna;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.http.AndroidHttpClient;
import com.b2msolutions.reyna.http.HttpPost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;

import java.net.URI;
import java.util.ArrayList;

public class Dispatcher {

    private static final String TAG = "Dispatcher";

    public enum Result {
        OK, PERMANENT_ERROR, TEMPORARY_ERROR, BLACKOUT
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

        TimeRange range = new Preferences(context).getCellularDataBlackout();
        if(Dispatcher.isInBlackout(context, range)) {
            return Result.BLACKOUT;
        }

        Result parseHttpPostResult = this.parseHttpPost(message, httpPost, httpClient, context);
        if (parseHttpPostResult != Result.OK) return parseHttpPostResult;

        return this.tryToExecute(httpPost, httpClient);
    }

    public static boolean isInBlackout(Context context, TimeRange range) {
        if (range == null) {
            return false;
        }

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();

        int type = info.getType();

        if (type != ConnectivityManager.TYPE_MOBILE &&
            type != ConnectivityManager.TYPE_MOBILE_DUN &&
            type != ConnectivityManager.TYPE_MOBILE_HIPRI &&
            type != ConnectivityManager.TYPE_MOBILE_MMS &&
            type != ConnectivityManager.TYPE_MOBILE_SUPL &&
            type != ConnectivityManager.TYPE_WIMAX) {
            return false;
        }

        return range.contains(new Time());
    }

    private Result parseHttpPost(Message message, HttpPost httpPost, HttpClient httpClient, Context context) {
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
        for (int i = 0; i < headers.length; i++) {
            Header header = headers[i];

            if (header.getKey().equalsIgnoreCase("content-encoding")
                    && header.getValue().equalsIgnoreCase("gzip")) {
                return true;
            }
        }

        return false;
    }

    private static Header[] removeGzipEncodingHeader(Header[] headers) {
        ArrayList<Header> filteredHeaders = new ArrayList<Header>();

        for (int i = 0; i < headers.length; i++) {
            Header header = headers[i];

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
}
