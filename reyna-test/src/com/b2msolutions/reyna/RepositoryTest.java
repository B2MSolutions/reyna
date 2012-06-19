package com.b2msolutions.reyna;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Hashtable;

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
		this.repository.store("http://www.zzz.com/q/e", "{ \"some\" : \"json\" }", null);
	}
	
	@Test
	public void storeWithoutHeadersShouldSave() {
		String url = "url";
		String body = "body";
		this.repository.store(url, body, null);
		SQLiteDatabase db = this.repository.getReadableDatabase();
		Cursor messageCursor = db.query("Message", new String[] {"url", "body"}, null, null, null, null, null);
		assertTrue(messageCursor.moveToFirst());
		assertEquals(url, messageCursor.getString(0));
		assertEquals(body, messageCursor.getString(1));
		assertTrue(messageCursor.isFirst());
		assertTrue(messageCursor.isLast());
		
		Cursor headerCursor = db.query("Header", new String[] {"messageid", "key", "value"}, null, null, null, null, null);
		assertFalse(headerCursor.moveToFirst());
	}
	
	@Test
	public void storeWithHeadersShouldSave() {
		String url = "url";
		String body = "body";
				
		Hashtable<String, String> headers = new Hashtable<String, String>();
		headers.put("h1", "v1");
		headers.put("h2", "v2");
		
		this.repository.store(url, body, headers);
		SQLiteDatabase db = this.repository.getReadableDatabase();
		Cursor messageCursor = db.query("Message", new String[] {"id", "url", "body"}, null, null, null, null, null);
		assertTrue(messageCursor.moveToFirst());
		assertEquals(url, messageCursor.getString(1));
		assertEquals(body, messageCursor.getString(2));
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
