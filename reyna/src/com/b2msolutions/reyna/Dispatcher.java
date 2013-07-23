package com.b2msolutions.reyna;

import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.util.Log;
import com.b2msolutions.reyna.http.HttpPost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;

import java.net.URI;

public class Dispatcher {

	private static final String TAG = "Dispatcher";

    public enum Result {
		OK, PERMANENT_ERROR, TEMPORARY_ERROR
	}

	public Result sendMessage(Context context, Message message) {
		Log.v(TAG, "sendMessage");

        AndroidHttpClient httpClient = AndroidHttpClient.newInstance("Reyna", context);
        try {
		    return this.sendMessage(message, new HttpPost(), httpClient);
        } finally {
            httpClient.close();
        }
	}

	protected Result sendMessage(Message message, HttpPost httpPost, HttpClient httpClient) {
		Log.v(TAG, "sendMessage: injected");
		
		Result parseHttpPostResult = this.parseHttpPost(message, httpPost, httpClient);
		if(parseHttpPostResult != Result.OK) return parseHttpPostResult;
		
		return this.tryToExecute(httpPost, httpClient);
	}
	
	private Result parseHttpPost(Message message, HttpPost httpPost, HttpClient httpClient) {
		Log.v(TAG, "parseHttpPost");
		
		try {
			for (Header header : message.getHeaders()) {
				httpPost.setHeader(header.getKey(), header.getValue());
			}

			URI uri = message.getURI();
			httpPost.setURI(uri);
            httpPost.setEntity(new StringEntity(message.getBody(), HTTP.UTF_8));
			return Result.OK;
		} catch (Exception e) {
			Log.e(TAG, "parseHttpPost", e);
			return Result.PERMANENT_ERROR;
		}
	}

	private Result tryToExecute(HttpPost httpPost, HttpClient httpClient) {
		Log.v(TAG, "tryToExecute");
		
		try {
			HttpResponse response = httpClient.execute(httpPost);
			return getResult(response.getStatusLine().getStatusCode());
		} catch (Exception e) {
            Log.d(TAG, "tryToExecute", e);
			Log.i(TAG, "tryToExecute: temporary error");
			return Result.TEMPORARY_ERROR;
		}
	}

	protected static Result getResult(int statusCode) {
		Log.v(TAG, "getResult: " + statusCode);
		
		if (statusCode >= 200 && statusCode < 300)
			return Result.OK;
		if (statusCode >= 300 && statusCode < 500)
			return Result.PERMANENT_ERROR;
		if (statusCode >= 500 && statusCode < 600)
			return Result.TEMPORARY_ERROR;

		return Result.PERMANENT_ERROR;
	}
}
