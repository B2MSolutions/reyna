package com.b2msolutions.reyna.shadows;

import android.content.ContentResolver;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import android.content.Context;
import android.net.http.AndroidHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

@Implements(AndroidHttpClient.class)
public class ShadowAndroidHttpClient {
    @RealObject private AndroidHttpClient client;

    private HttpClient httpClient = new DefaultHttpClient();

    @Implementation
    public static AndroidHttpClient newInstance(String userAgent) {
        return Robolectric.newInstanceOf(AndroidHttpClient.class);
    }

    @Implementation
    public static AndroidHttpClient newInstance(String userAgent, Context context) {
        return Robolectric.newInstanceOf(AndroidHttpClient.class);
    }

    @Implementation
    public static AbstractHttpEntity getCompressedEntity(byte[] data, android.content.ContentResolver resolver) throws java.io.IOException
    {
       AbstractHttpEntity entity;
        if (data.length < getMinGzipSize(resolver)) {
            entity = new ByteArrayEntity(data);
        } else {
            ByteArrayOutputStream arr = new ByteArrayOutputStream();
            OutputStream zipper = new GZIPOutputStream(arr);
            zipper.write(data);
            zipper.close();
            entity = new ByteArrayEntity(arr.toByteArray());
            entity.setContentEncoding("gzip");
        }
        return entity;
    }

    @Implementation
    public HttpParams getParams() {
        return httpClient.getParams();
    }

    @Implementation
    public ClientConnectionManager getConnectionManager() {
        return httpClient.getConnectionManager();
    }

    @Implementation
    public HttpResponse execute(HttpUriRequest httpUriRequest) throws IOException, ClientProtocolException {
        return httpClient.execute(httpUriRequest);
    }

    @Implementation
    public HttpResponse execute(HttpUriRequest httpUriRequest, HttpContext httpContext) throws IOException, ClientProtocolException {
        return httpClient.execute(httpUriRequest, httpContext);
    }

    @Implementation
    public HttpResponse execute(HttpHost httpHost, HttpRequest httpRequest) throws IOException, ClientProtocolException {
        return httpClient.execute(httpHost, httpRequest);
    }

    @Implementation
    public HttpResponse execute(HttpHost httpHost, HttpRequest httpRequest, HttpContext httpContext) throws IOException, ClientProtocolException {
        return httpClient.execute(httpHost, httpRequest, httpContext);
    }

    @Implementation
    public <T> T execute(HttpUriRequest httpUriRequest, ResponseHandler<? extends T> responseHandler) throws IOException, ClientProtocolException {
        return httpClient.execute(httpUriRequest, responseHandler);
    }

    @Implementation
    public <T> T execute(HttpUriRequest httpUriRequest, ResponseHandler<? extends T> responseHandler, HttpContext httpContext) throws IOException, ClientProtocolException {
        return httpClient.execute(httpUriRequest, responseHandler, httpContext);
    }

    @Implementation
    public <T> T execute(HttpHost httpHost, HttpRequest httpRequest, ResponseHandler<? extends T> responseHandler) throws IOException, ClientProtocolException {
        return httpClient.execute(httpHost, httpRequest, responseHandler);
    }

    @Implementation
    public <T> T execute(HttpHost httpHost, HttpRequest httpRequest, ResponseHandler<? extends T> responseHandler, HttpContext httpContext) throws IOException, ClientProtocolException {
        return httpClient.execute(httpHost, httpRequest, responseHandler, httpContext);
    }

    @Implementation
    public static long getMinGzipSize(ContentResolver resolver) {
        return 10;
    }
}