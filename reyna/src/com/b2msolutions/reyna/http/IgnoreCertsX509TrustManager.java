package com.b2msolutions.reyna.http;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

public class IgnoreCertsX509TrustManager implements X509TrustManager{

	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {	
	}

	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {	
	}

	public X509Certificate[] getAcceptedIssuers() {
		return null;
	}
}
