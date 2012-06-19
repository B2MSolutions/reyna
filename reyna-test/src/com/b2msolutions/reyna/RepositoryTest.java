package com.b2msolutions.reyna;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
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
	public void storeShouldNotThrow() {
		Message message = new Message("url", "body", null);
		this.repository.store(message);
	}
	
	@Test
	public void storeWithoutHeadersShouldSave() {
		Message message = new Message("url", "body", null);
		this.repository.store(message);
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
	public void storeWithHeadersShouldSave() {		
		Message message = getMessageWithHeaders();
		this.repository.store(message);
		assertMessage(this.repository, message);		
	}

	public static Message getMessageWithHeaders() {
		return new Message("url", "body", new Header[] { new Header("h1", "v1"), new Header("h2", "v2") });		
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
