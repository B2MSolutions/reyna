package com.b2msolutions.reyna.system;

import com.b2msolutions.reyna.system.Header;
import com.b2msolutions.reyna.system.Message;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
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

    @Test
    public void callingAddHeaderShouldAddHeader() throws URISyntaxException {
        Message message = new Message(null, new URI("http://google.com"), "", null);
        assertEquals(0, message.getHeaders().length);

        message.addHeader(new Header("KEY", "VALUE"));
        Header[] headers = message.getHeaders();
        assertEquals(1, message.getHeaders().length);
        assertEquals("KEY", headers[0].getKey());
        assertEquals("VALUE", headers[0].getValue());
    }
}
