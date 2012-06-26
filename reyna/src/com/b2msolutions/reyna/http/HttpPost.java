package com.b2msolutions.reyna.http;

import java.io.UnsupportedEncodingException;

import org.apache.http.entity.StringEntity;

public class HttpPost extends org.apache.http.client.methods.HttpPost {
	public void setEntity(String content) throws UnsupportedEncodingException {
		super.setEntity(new StringEntity(content));
	}
}
