package com.b2msolutions.reyna;

import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;

import android.util.Log;

import com.b2msolutions.reyna.http.IgnoreCertsHttpClient;

public class Dispatcher {

	public enum Result {
		OK, PERMANENT_ERROR, TEMPORARY_ERROR
	}	

	public Result sendMessage(Message message) {
		return this.sendMessage(message, new HttpPost(), new IgnoreCertsHttpClient());
	}
	
	protected Result sendMessage(Message message, HttpPost httpPost, IgnoreCertsHttpClient httpClient) {
		for (Header header : message.getHeaders()) {
			httpPost.setHeader(header.getKey(), header.getValue());
		}

		URI uri = message.getURI();
		this.setPort(httpClient, uri);
 		
		httpPost.setURI(uri);
		
		try {
			HttpResponse response = httpClient.execute(httpPost);
			return getResult(response.getStatusLine().getStatusCode());
		} catch (Exception e) {
			Log.i("reyna", "Dispatcher: " + e.getMessage());
			return Result.TEMPORARY_ERROR;
		}		
	}

	private void setPort(IgnoreCertsHttpClient httpClient, URI uri) {
		
		try {
			int port = uri.getPort();
			if(port != -1) {
				httpClient.setPort(port);
			}
		} catch (Exception e) {
			Log.e("reyna", e.getMessage());
		}
	}
	
	protected static Result getResult(int statusCode) {
		if(statusCode >= 200 && statusCode < 300) return Result.OK;
		if(statusCode >= 300 && statusCode < 500) return Result.PERMANENT_ERROR;
		if(statusCode >= 500 && statusCode < 600) return Result.TEMPORARY_ERROR;
		
		return Result.PERMANENT_ERROR;		
	}
}
