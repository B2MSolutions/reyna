package com.b2msolutions.reyna;

import java.io.Serializable;

public class Message implements Serializable {
	private static final long serialVersionUID = 6230786319646630263L;
	
	private String url;

	private String body;
	
	private Header[] headers;
	
	public Message(String url, String body, Header[] headers) {
		this.url = url;
		this.body = body;
		this.headers = headers;
		if(this.headers == null) {
			this.headers = new Header[0];
		}
	}
	
	public String getUrl() {
		return this.url;
	}

	public String getBody() {
		return this.body;
	}

	public Header[] getHeaders() {
		return this.headers;
	}
}
