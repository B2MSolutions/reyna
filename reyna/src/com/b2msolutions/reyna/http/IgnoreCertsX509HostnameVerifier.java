package com.b2msolutions.reyna.http;

import java.io.IOException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.apache.http.conn.ssl.X509HostnameVerifier;

class IgnoreCertsX509HostnameVerifier implements X509HostnameVerifier {

	public boolean verify(String host, SSLSession session) {
		return true;
	}

	public void verify(String host, SSLSocket ssl) throws IOException {
	}

	public void verify(String host, X509Certificate cert) throws SSLException {
	}

	public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
	}
}
