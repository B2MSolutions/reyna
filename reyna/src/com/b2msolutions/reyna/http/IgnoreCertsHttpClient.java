package com.b2msolutions.reyna.http;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

public class IgnoreCertsHttpClient {
	
	private DefaultHttpClient httpClient;

	public IgnoreCertsHttpClient() {		
	}

	public void setPort(int port) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, KeyManagementException, UnrecoverableKeyException {
		
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
		
		KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		keyStore.load(null, null);
		IgnoreCertsSSLSocketFactory ignoreCertsSSLSocketFactory = new IgnoreCertsSSLSocketFactory(keyStore);
		ignoreCertsSSLSocketFactory.setHostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		
		SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("https", ignoreCertsSSLSocketFactory, port));
		SingleClientConnManager mgr = new SingleClientConnManager(params, registry);
		this.httpClient = new DefaultHttpClient(mgr, params);
	}
	
	public HttpResponse execute(HttpUriRequest request) throws IOException, ClientProtocolException {
		return this.httpClient.execute(request);
	}

	public HttpResponse execute(HttpUriRequest request, HttpContext context) throws IOException, ClientProtocolException {
		return this.httpClient.execute(request, context);
	}

	public HttpResponse execute(HttpHost target, HttpRequest request) throws IOException, ClientProtocolException {
		return this.httpClient.execute(target, request);
	}

	public <T> T execute(HttpUriRequest arg0, ResponseHandler<? extends T> arg1) throws IOException, ClientProtocolException {
		return this.execute(arg0, arg1);
	}

	public HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context) throws IOException, ClientProtocolException {
		return this.execute(target, request, context);	
	}

	public <T> T execute(HttpUriRequest arg0, ResponseHandler<? extends T> arg1, HttpContext arg2) throws IOException, ClientProtocolException {
		return this.execute(arg0, arg1, arg2);
	}

	public <T> T execute(HttpHost arg0, HttpRequest arg1, ResponseHandler<? extends T> arg2) throws IOException, ClientProtocolException {
		return this.execute(arg0, arg1, arg2);
	}

	public <T> T execute(HttpHost arg0, HttpRequest arg1, ResponseHandler<? extends T> arg2, HttpContext arg3) throws IOException, ClientProtocolException {
		return this.execute(arg0, arg1, arg2, arg3);
	}

	public ClientConnectionManager getConnectionManager() {
		return this.httpClient.getConnectionManager();
	}

	public HttpParams getParams() {
		return this.httpClient.getParams();
	}	
}