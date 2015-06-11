package com.b2msolutions.reyna;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.*;

@RunWith(ReynaTestRunner.class)
public class RepositoryTest {
	
	private Repository repository;

	@Before
	public void setup() {
		Context context = Robolectric.application.getApplicationContext();
        this.repository = new Repository(context);
	}
	
	@Test
	public void constructionShouldNotThrow() {		
        assertNotNull(this.repository);
    }	

	@Test
	public void onCreateShouldNotThrow() {		
        SQLiteDatabase db = this.repository.getReadableDatabase();
        assertNotNull(db);
    }
	
	@Test
	public void insertWithNullMessageShouldNotThrow() {
		this.repository.insert(null);
	}

	@Test
	public void insertShouldNotThrow() throws URISyntaxException {
		Message message = new Message(new URI("https://www.google.com"), "body", null);
		this.repository.insert(message);
	}
	
	@Test
	public void insertWithoutHeadersShouldSave() throws URISyntaxException {
		Message message = new Message(new URI("https://www.google.com"), "body", null);
		this.repository.insert(message);
		SQLiteDatabase db = this.repository.getReadableDatabase();
		Cursor messageCursor = db.query("Message", new String[] {"url", "body"}, null, null, null, null, null);
		assertTrue(messageCursor.moveToFirst());
		assertEquals(message.getUrl(), messageCursor.getString(0));
		assertEquals(message.getBody(), messageCursor.getString(1));
		assertTrue(messageCursor.isFirst());
		assertTrue(messageCursor.isLast());
		messageCursor.close();
		
		Cursor headerCursor = db.query("Header", new String[] {"messageid", "key", "value"}, null, null, null, null, null);
		assertFalse(headerCursor.moveToFirst());
		headerCursor.close();
	}
	
	@Test
	public void getNextWithNoMessagesShouldReturnNull() throws URISyntaxException {
		assertNull(this.repository.getNext());
	}

	@Test
	public void getNextWithMessageShouldReturnIt() throws URISyntaxException {
		Message message = getMessageWithHeaders();
		this.repository.insert(message);
		
		Message nextMessage = this.repository.getNext();
		
		assertNotNull(nextMessage);			
		assertNotNull(nextMessage.getId());	
		assertEquals(message.getUrl(), nextMessage.getUrl());
		assertEquals(message.getBody(), nextMessage.getBody());
		assertEquals("h1", nextMessage.getHeaders()[0].getKey());
		assertEquals("v1", nextMessage.getHeaders()[0].getValue());
		assertEquals("h2", nextMessage.getHeaders()[1].getKey());
		assertEquals("v2", nextMessage.getHeaders()[1].getValue());
	}
	
	@Test
	public void deleteWithNullMessageShouldNotThrow() {
		this.repository.delete(null);
	}

	@Test
	public void deleteWithMessageThatHasNullIdShouldNotThrow() throws URISyntaxException {
		this.repository.delete(getMessageWithHeadersAndNonNullId());
	}
	
	@Test
	public void deleteWithMissingMessageShouldNotThrow() throws URISyntaxException {
		this.repository.delete(getMessageWithHeaders());
	}
	
	@Test
	public void deleteWithMessageShouldDelete() throws URISyntaxException {
		Message message = getMessageWithHeaders();
		this.repository.insert(message);
		
		Message nextMessage = this.repository.getNext();
		this.repository.delete(nextMessage);
		
		assertNull(this.repository.getNext());
	}
		
	@Test
	public void insertWithHeadersShouldSave() throws URISyntaxException {		
		Message message = getMessageWithHeaders();
		this.repository.insert(message);
		assertMessage(this.repository, message);		
	}

	public static Message getMessageWithHeaders() throws URISyntaxException {
		return getMessageWithHeaders("body");
	}

    public static Message getMessageWithHeaders(String body) throws URISyntaxException {
        return new Message(new URI("https://www.google.com"), body, new Header[] { new Header("h1", "v1"), new Header("h2", "v2") });
    }

    public static Message getMessageWithGzipHeaders(String body) throws URISyntaxException {
        return new Message(new URI("https://www.google.com"), body, new Header[] { new Header("h1", "v1"), new Header("h2", "v2"), new Header("content-encoding", "gzip") });
    }

	public static Message getMessageWithHeadersAndNonNullId() throws URISyntaxException {
		return new Message(new Long(1), new URI("https://www.google.com"), "body", new Header[] { new Header("h1", "v1"), new Header("h2", "v2") });		
	}
	
	public static void assertMessage(Repository repository, Message message) {
		SQLiteDatabase db = repository.getReadableDatabase();
		Cursor messageCursor = db.query("Message", new String[] {"id", "url", "body"}, null, null, null, null, null);
		assertTrue(messageCursor.moveToFirst());
		assertEquals(message.getUrl(), messageCursor.getString(1));
		assertEquals(message.getBody(), messageCursor.getString(2));
		
		assertTrue(messageCursor.isFirst());
		assertTrue(messageCursor.isLast());
		
		Cursor headerCursor = db.query("Header", new String[] {"messageid", "key", "value"}, null, null, null, null, "key");
		assertTrue(headerCursor.moveToFirst());
		assertEquals(messageCursor.getLong(0), headerCursor.getLong(0));
		assertEquals("h1", headerCursor.getString(1));
		assertEquals("v1", headerCursor.getString(2));
		assertTrue(headerCursor.isFirst());
		
		assertTrue(headerCursor.moveToNext());
		assertEquals(messageCursor.getLong(0), headerCursor.getLong(0));
		assertEquals("h2", headerCursor.getString(1));
		assertEquals("v2", headerCursor.getString(2));
		assertTrue(headerCursor.isLast());
		
		messageCursor.close();
		headerCursor.close();
	}
}
