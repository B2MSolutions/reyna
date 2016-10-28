package com.b2msolutions.reyna;

import com.b2msolutions.reyna.system.Header;
import com.b2msolutions.reyna.system.Message;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class SerializationTest {

    @Test
    public void whenSerializingMessagesShouldDeSerializeCorrectly() throws IOException, ClassNotFoundException, URISyntaxException {
        Message message = new Message(new URI("https://google.com"), "body", new Header[] { new Header("h1", "v1"), new Header("h2", "v2") });
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(message);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object outputObj = ois.readObject();
        assertNotNull(outputObj);
        Message message2 = (Message)outputObj;

        assertNull(message2.getId());
        assertEquals("https://google.com", message2.getUrl());
        assertEquals("body", message2.getBody());
        assertEquals(2, message2.getHeaders().length);
        assertEquals("h1", message2.getHeaders()[0].getKey());
        assertEquals("v1", message2.getHeaders()[0].getValue());
        assertEquals("h2", message2.getHeaders()[1].getKey());
        assertEquals("v2", message2.getHeaders()[1].getValue());
    }	
}

