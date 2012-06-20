package com.b2msolutions.reyna;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
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
	public void insertShouldNotThrow() {
		Message message = new Message("url", "body", null);
		this.repository.insert(message);
	}
	
	@Test
	public void insertWithoutHeadersShouldSave() {
		Message message = new Message("url", "body", null);
		this.repository.insert(message);
		SQLiteDatabase db = this.repository.getReadableDatabase();
		Cursor messageCursor = db.query("Message", new String[] {"url", "body"}, null, null, null, null, null);
		assertTrue(messageCursor.moveToFirst());
		assertEquals(message.getUrl(), messageCursor.getString(0));
		assertEquals(message.getBody(), messageCursor.getString(1));
		assertTrue(messageCursor.isFirst());
		assertTrue(messageCursor.isLast());
		
		Cursor headerCursor = db.query("Header", new String[] {"messageid", "key", "value"}, null, null, null, null, null);
		assertFalse(headerCursor.moveToFirst());
	}
	
	@Test
	public void getNextWithNoMessagesShouldReturnNull() {
		assertNull(this.repository.getNext());
	}

	@Test
	public void getNextWithMessageShouldReturnIt() {
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
	public void deleteWithMessageThatHasNullIdShouldNotThrow() {
		this.repository.delete(getMessageWithHeadersAndNonNullId());
	}
	
	@Test
	public void deleteWithMissingMessageShouldNotThrow() {
		this.repository.delete(getMessageWithHeaders());
	}
	
	@Test
	public void deleteWithMessageShouldDelete() {
		Message message = getMessageWithHeaders();
		this.repository.insert(message);
		
		Message nextMessage = this.repository.getNext();
		this.repository.delete(nextMessage);
		
		assertNull(this.repository.getNext());
	}
		
	@Test
	public void insertWithHeadersShouldSave() {		
		Message message = getMessageWithHeaders();
		this.repository.insert(message);
		assertMessage(this.repository, message);		
	}

	public static Message getMessageWithHeaders() {
		return new Message("url", "body", new Header[] { new Header("h1", "v1"), new Header("h2", "v2") });		
	}
	
	public static Message getMessageWithHeadersAndNonNullId() {
		return new Message(new Long(1), "url", "body", new Header[] { new Header("h1", "v1"), new Header("h2", "v2") });		
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
	}
}
