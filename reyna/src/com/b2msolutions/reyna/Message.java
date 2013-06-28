package com.b2msolutions.reyna;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

public class Message implements Serializable {
	private static final long serialVersionUID = 6230786319646630263L;
	
	private Long id;
	
	private String url;

	private String body;
	
	private Header[] headers;
	
	public Message(URI uri, String body, Header[] headers) {
		this(null, uri, body, headers);
	}

	public Message(Long id, URI uri, String body, Header[] headers) {

		this.id = id;
		
		this.url = uri.toString();
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

	public URI getURI() {
		try {
			return new URI(this.url);
		} catch (URISyntaxException e) {
			return null;
		}
	}

	public String getBody() {
		return this.body;
	}

	public Header[] getHeaders() {
		return this.headers;
	}
}
