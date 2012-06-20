package com.b2msolutions.reyna.http;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.params.HttpParams;

public class IgnoreCertsSSLSocketFactory extends SSLSocketFactory {
	
	SSLContext context = SSLContext.getInstance("TLS");
	public IgnoreCertsSSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {	
		super(truststore);
		context.init(null, new TrustManager[] { new IgnoreCertsX509TrustManager()}, null);
	}

	@Override
	public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
		return context.getSocketFactory().createSocket(socket, host, port, autoClose);
	}
	
	@Override
	public boolean isSecure(Socket sock) throws IllegalArgumentException {
		return true;
	}

	@Override
	public void setHostnameVerifier(X509HostnameVerifier hostnameVerifier) {
		super.setHostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
	}

	@Override
	public Socket connectSocket(Socket sock, String host, int port, InetAddress localAddress, int localPort, HttpParams params) throws IOException {
		return context.getSocketFactory().createSocket(host, port, localAddress, localPort);
	}

	@Override
	public Socket createSocket() throws IOException {
		return context.getSocketFactory().createSocket();
	}
}