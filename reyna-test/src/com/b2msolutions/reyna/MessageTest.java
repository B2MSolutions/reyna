package com.b2msolutions.reyna;

import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertNotNull;

public class MessageTest {

	@Test
	public void whenConstructingWithHTTPSShouldNotThrow() throws URISyntaxException {
		assertNotNull(new Message(new URI("https://google.com"), "", null));
	}

	@Test
	public void whenConstructingWithHTTPShouldNotThrow() throws URISyntaxException {
        assertNotNull(new Message(new URI("http://google.com"), "", null));
	}
}
