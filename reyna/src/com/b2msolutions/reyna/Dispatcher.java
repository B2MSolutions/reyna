package com.b2msolutions.reyna;

import java.net.URI;

import org.apache.http.HttpResponse;

import android.util.Log;

import com.b2msolutions.reyna.http.HttpPost;
import com.b2msolutions.reyna.http.IgnoreCertsHttpClient;

public class Dispatcher {

	private static final String TAG = "Dispatcher";
	
	public enum Result {
		OK, PERMANENT_ERROR, TEMPORARY_ERROR
	}

	public Result sendMessage(Message message) {
		Log.v(TAG, "sendMessage");
		
		return this.sendMessage(message, new HttpPost(),
				new IgnoreCertsHttpClient());
	}

	protected Result sendMessage(Message message, HttpPost httpPost, IgnoreCertsHttpClient httpClient) {
		Log.v(TAG, "sendMessage: injected");
		
		Result parseHttpPostResult = this.parseHttpPost(message, httpPost, httpClient);
		if(parseHttpPostResult != Result.OK) return parseHttpPostResult;
		
		return this.tryToExecute(httpPost, httpClient);
	}
	
	private Result parseHttpPost(Message message, HttpPost httpPost, IgnoreCertsHttpClient httpClient) {
		Log.v(TAG, "parseHttpPost");
		
		try {
			for (Header header : message.getHeaders()) {
				httpPost.setHeader(header.getKey(), header.getValue());
			}

			URI uri = message.getURI();
			this.setPort(httpClient, uri);
			httpPost.setURI(uri);
			httpPost.setEntity(message.getBody());
			return Result.OK;
		} catch (Exception e) {
			Log.e(TAG, "parseHttpPost", e);
			return Result.PERMANENT_ERROR;
		}
	}

	private Result tryToExecute(HttpPost httpPost, IgnoreCertsHttpClient httpClient) {
		Log.v(TAG, "tryToExecute");
		
		try {
			HttpResponse response = httpClient.execute(httpPost);
			return getResult(response.getStatusLine().getStatusCode());
		} catch (Exception e) {
			Log.i(TAG, "tryToExecute", e);
			return Result.TEMPORARY_ERROR;
		}
	}

	private void setPort(IgnoreCertsHttpClient httpClient, URI uri) {
		Log.v(TAG, "setPort");
		
		try {
			int port = uri.getPort();
			if (port == -1) {
				Log.v(TAG, "setPort: no port specified, defaulting to 443");
				port = 443;
			}

			httpClient.setPort(port);
		} catch (Exception e) {
			Log.e(TAG, "setPort", e);
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
