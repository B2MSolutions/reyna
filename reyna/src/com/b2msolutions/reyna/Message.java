package com.b2msolutions.reyna;

import java.io.Serializable;

public class Message implements Serializable {
	private static final long serialVersionUID = 6230786319646630263L;
	
	private Long id;
	
	private String url;

	private String body;
	
	private Header[] headers;
	
	public Message(String url, String body, Header[] headers) {
		this(null, url, body, headers);
	}

	public Message(Long id, String url, String body, Header[] headers) {
		this.id = id;
		this.url = url;
		this.body = body;
		this.headers = headers;
		if(this.headers == null) {
			this.headers = new Header[0];
		}
	}

	public Long getId() {
		return this.id;
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
