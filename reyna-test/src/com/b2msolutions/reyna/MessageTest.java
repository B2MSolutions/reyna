package com.b2msolutions.reyna;

import static org.junit.Assert.assertNotNull;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

public class MessageTest {

	@Test
	public void whenConstructingWithHTTPSShouldNotThrow()
			throws URISyntaxException {
		assertNotNull(new Message(new URI("https://google.com"), "", null));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void whenConstructingWithHTTPShouldThrow() throws URISyntaxException {
		new Message(new URI("http://google.com"), "", null);
	}
}
