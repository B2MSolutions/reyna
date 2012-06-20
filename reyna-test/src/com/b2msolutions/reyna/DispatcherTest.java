package com.b2msolutions.reyna;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

import com.b2msolutions.reyna.Dispatcher.Result;
import com.b2msolutions.reyna.http.IgnoreCertsHttpClient;
import com.xtremelabs.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class DispatcherTest {

	@Test
	public void sendMessageHappyPathShouldSetExecuteCorrectHttpPostAndReturnOK() throws URISyntaxException, ClientProtocolException, IOException {
		Message message = RepositoryTest.getMessageWithHeaders(); 
		
		StatusLine statusLine = mock(StatusLine.class);
		when(statusLine.getStatusCode()).thenReturn(200);
		HttpResponse httpResponse = mock(HttpResponse.class);
		when(httpResponse.getStatusLine()).thenReturn(statusLine);
		
		HttpPost httpPost = mock(HttpPost.class);
		IgnoreCertsHttpClient httpClient = mock(IgnoreCertsHttpClient.class);
		when(httpClient.execute(httpPost)).thenReturn(httpResponse);
		
		assertEquals(Result.OK, new Dispatcher().sendMessage(message, httpPost, httpClient));
		
		this.verifyHttpPost(message, httpPost);
	}

	@Test
	public void whenExecuteThrowsReturnTemporaryError() throws URISyntaxException, ClientProtocolException, IOException {
		Message message = RepositoryTest.getMessageWithHeaders(); 
				
		HttpPost httpPost = mock(HttpPost.class);
		IgnoreCertsHttpClient httpClient = mock(IgnoreCertsHttpClient.class);
		when(httpClient.execute(httpPost)).thenThrow(new RuntimeException(""));
		
		assertEquals(Result.TEMPORARY_ERROR, new Dispatcher().sendMessage(message, httpPost, httpClient));
		
		this.verifyHttpPost(message, httpPost);
	}
	
	@Test
	public void getResultShouldReturnExpected() {
		assertEquals(Result.PERMANENT_ERROR, Dispatcher.getResult(100));
		assertEquals(Result.OK, Dispatcher.getResult(200));
		assertEquals(Result.PERMANENT_ERROR, Dispatcher.getResult(300));
		assertEquals(Result.PERMANENT_ERROR, Dispatcher.getResult(400));
		assertEquals(Result.TEMPORARY_ERROR, Dispatcher.getResult(500));
		assertEquals(Result.PERMANENT_ERROR, Dispatcher.getResult(600));
	}

	private void verifyHttpPost(Message message, HttpPost httpPost) {
		ArgumentCaptor<URI> argument = ArgumentCaptor.forClass(URI.class);		
		verify(httpPost).setURI(argument.capture());
		assertEquals(message.getUrl(), argument.getValue().toString());
		
		verify(httpPost).setHeader(message.getHeaders()[0].getKey(), message.getHeaders()[0].getValue());
		verify(httpPost).setHeader(message.getHeaders()[1].getKey(), message.getHeaders()[1].getValue());
	}
}
