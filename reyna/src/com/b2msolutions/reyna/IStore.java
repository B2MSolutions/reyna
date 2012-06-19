package com.b2msolutions.reyna;

import java.util.Hashtable;

public interface IStore {
	public void store(String url, String body, Hashtable<String, String> headers);
}
