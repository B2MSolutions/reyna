package com.b2msolutions.reyna;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.xtremelabs.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class SerializationTest {
	
	@Test
	public void whenSerializingMessagesShouldDeSerializeCorrectly() throws IOException, ClassNotFoundException {
		Message message = new Message("url", "body", new Header[] { new Header("h1", "v1"), new Header("h2", "v2") });
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
		assertEquals("url", message2.getUrl());
		assertEquals("body", message2.getBody());
		assertEquals(2, message2.getHeaders().length);
		assertEquals("h1", message2.getHeaders()[0].getKey());
		assertEquals("v1", message2.getHeaders()[0].getValue());
		assertEquals("h2", message2.getHeaders()[1].getKey());
		assertEquals("v2", message2.getHeaders()[1].getValue());
    }	
}

