package com.b2msolutions.reyna;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.RobolectricTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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
    public void insertWithHeadersShouldSave() throws URISyntaxException {
        Message message = getMessageWithHeaders();
        this.repository.insert(message);
        assertMessage(this.repository, message);
    }

    @Test
    public void insertWithNullMessageAndDbSizeShouldNotThrow() {
        this.repository.insert(null, 4096);
    }

    @Test
    public void insertWithDbSizeShouldNotThrow() throws URISyntaxException {
        Message message = new Message(new URI("https://www.google.com"), "body", null);
        this.repository.insert(message, 4096);
    }

    @Test
    public void insertWithoutHeadersWithDbSizeShouldSave() throws URISyntaxException {
        Message message = new Message(new URI("https://www.google.com"), "body", null);
        this.repository.insert(message, 4096);
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
    public void insertWithHeadersAndDbSizeShouldSave() throws URISyntaxException {
        Message message = getMessageWithHeaders();
        this.repository.insert(message, 4096);
        assertMessage(this.repository, message);
    }

    @Test
    public void insertShouldDeleteOldRecordsIfLimitCrossed() throws URISyntaxException {
        SQLiteDatabase db = mock(SQLiteDatabase.class);

        // get db size
        when(db.insert(anyString(), anyString(), any(ContentValues.class))).thenReturn(42l);
        when(db.getPageSize()).thenReturn(4096l);

        //get page count
        Cursor cursor = mock(Cursor.class);
        when(cursor.getLong(0))
                .thenReturn(10l)
                .thenReturn(10l)
                .thenReturn(10l)
                .thenReturn(10l)
                .thenReturn(1l);
        when(db.rawQuery("pragma page_count", null)).thenReturn(cursor);

        //get number of messages
        Cursor numberOfMessagesCursor = mock(Cursor.class);
        when(numberOfMessagesCursor.getLong(0)).thenReturn(42l);
        when(db.rawQuery("select count(*) from Message", null)).thenReturn(numberOfMessagesCursor);

        //get message id to which to shrink
        Cursor idToShrinkCursor = mock(Cursor.class);
        when(idToShrinkCursor.getLong(0)).thenReturn(101l);
        when(db.rawQuery("select id from Message limit 1 offset 2", null)).thenReturn(idToShrinkCursor);

        this.repository = spy(this.repository);
        when(this.repository.getWritableDatabase()).thenReturn(db);

        Message message = getMessageWithHeaders();
        this.repository.insert(message, 39000);

        verify(db, times(1)).execSQL("delete from Message where id < 101");
        verify(db, times(1)).execSQL("delete from Header where messageid < 101");
        assertMockedMessage(db);
    }

    @Test
    public void insertShouldDeleteOldRecordsIfLimitCrossedAndPerformVacuumIfCrossedMoreThan10Percent() throws URISyntaxException {
        SQLiteDatabase db = mock(SQLiteDatabase.class);

        // get db size
        when(db.insert(anyString(), anyString(), any(ContentValues.class))).thenReturn(42l);
        when(db.getPageSize()).thenReturn(4096l);

        //get page count
        Cursor cursor = mock(Cursor.class);
        when(cursor.getLong(0))
                .thenReturn(10l)
                .thenReturn(10l)
                .thenReturn(10l)
                .thenReturn(10l)
                .thenReturn(1l);
        when(db.rawQuery("pragma page_count", null)).thenReturn(cursor);

        //get number of messages
        Cursor numberOfMessagesCursor = mock(Cursor.class);
        when(numberOfMessagesCursor.getLong(0)).thenReturn(42l);
        when(db.rawQuery("select count(*) from Message", null)).thenReturn(numberOfMessagesCursor);

        //get message id to which to shrink
        Cursor idToShrinkCursor = mock(Cursor.class);
        when(idToShrinkCursor.getLong(0)).thenReturn(101l);
        when(db.rawQuery("select id from Message limit 1 offset 11", null)).thenReturn(idToShrinkCursor);

        this.repository = spy(this.repository);
        when(this.repository.getWritableDatabase()).thenReturn(db);

        Message message = getMessageWithHeaders();
        this.repository.insert(message, 30000);

        verify(db, times(1)).execSQL("delete from Message where id < 101");
        verify(db, times(1)).execSQL("delete from Header where messageid < 101");
        verify(db, times(1)).execSQL("vacuum");
        assertMockedMessage(db);
    }

    @Test
    public void insertShouldDeleteOldRecordsMultipleTimesIfLimitCrossedAndShrinkRunsMoreThanOnce() throws URISyntaxException {
        SQLiteDatabase db = mock(SQLiteDatabase.class);

        // get db size
        when(db.insert(anyString(), anyString(), any(ContentValues.class))).thenReturn(42l);
        when(db.getPageSize()).thenReturn(4096l);

        //get page count
        Cursor cursor = mock(Cursor.class);
        when(cursor.getLong(0))
                .thenReturn(10l)
                .thenReturn(10l)
                .thenReturn(10l)
                .thenReturn(10l)
                .thenReturn(5l)
                .thenReturn(5l)
                .thenReturn(1l);
        when(db.rawQuery("pragma page_count", null)).thenReturn(cursor);

        //get number of messages
        Cursor numberOfMessagesCursor = mock(Cursor.class);
        when(numberOfMessagesCursor.getLong(0)).thenReturn(42l);
        when(db.rawQuery("select count(*) from Message", null)).thenReturn(numberOfMessagesCursor);

        //get message id to which to shrink
        Cursor idToShrinkCursor = mock(Cursor.class);
        when(idToShrinkCursor.getLong(0)).thenReturn(101l).thenReturn(32l);
        when(db.rawQuery("select id from Message limit 1 offset 21", null)).thenReturn(idToShrinkCursor);
        when(db.rawQuery("select id from Message limit 1 offset 1", null)).thenReturn(idToShrinkCursor);

        this.repository = spy(this.repository);
        when(this.repository.getWritableDatabase()).thenReturn(db);

        Message message = getMessageWithHeaders();
        this.repository.insert(message, 20000);

        verify(db, times(1)).execSQL("delete from Message where id < 101");
        verify(db, times(1)).execSQL("delete from Header where messageid < 101");
        verify(db, times(1)).execSQL("delete from Message where id < 32");
        verify(db, times(1)).execSQL("delete from Header where messageid < 32");
        verify(db, times(1)).execSQL("vacuum");
        assertMockedMessage(db);
    }

    @Test
    public void insertShouldRemoveOldMessageIfApproachingLimit() throws URISyntaxException {
        SQLiteDatabase db = mock(SQLiteDatabase.class);

        // get db size
        when(db.insert(anyString(), anyString(), any(ContentValues.class))).thenReturn(42l);
        when(db.getPageSize()).thenReturn(4096l);

        //get page count
        Cursor cursor = mock(Cursor.class);
        when(cursor.getLong(0)).thenReturn(10l);
        when(db.rawQuery("pragma page_count", null)).thenReturn(cursor);

        //get oldest message with same type
        Cursor oldestMessageCursor = mock(Cursor.class);
        when(oldestMessageCursor.moveToNext()).thenReturn(true);
        when(oldestMessageCursor.getLong(0)).thenReturn(100l);
        when(db.query("Message", new String[]{"min(id)"}, "url=?", new String[]{"https://www.google.com"}, null, null, null))
                .thenReturn(oldestMessageCursor);

        this.repository = spy(this.repository);
        when(this.repository.getWritableDatabase()).thenReturn(db);

        Message message = getMessageWithHeaders();
        this.repository.insert(message, 41000);

        verify(db, times(1)).delete("Header", "messageid = ?", new String[]{"100"});
        verify(db, times(1)).delete("Message", "id = ?", new String[]{"100"});
        assertMockedMessage(db);
    }

    @Test
    public void insertWithDbSizeShouldNotDeleteIfDbSizeReachesThresholdButNoMessagesWithTheSameType() throws URISyntaxException {
        SQLiteDatabase db = mock(SQLiteDatabase.class);

        // get db size
        when(db.insert(anyString(), anyString(), any(ContentValues.class))).thenReturn(42l);
        when(db.getPageSize()).thenReturn(4096l);

        //get page count
        Cursor cursor = mock(Cursor.class);
        when(cursor.getLong(0)).thenReturn(10l);
        when(db.rawQuery("pragma page_count", null)).thenReturn(cursor);

        //get oldest message with same type
        Cursor oldestMessageCursor = mock(Cursor.class);
        when(oldestMessageCursor.moveToNext()).thenReturn(false);
        when(db.query("Message", new String[]{"min(id)"}, "url=?", new String[]{"https://www.google.com"}, null, null, null))
                .thenReturn(oldestMessageCursor);

        this.repository = spy(this.repository);
        when(this.repository.getWritableDatabase()).thenReturn(db);

        Message message = getMessageWithHeaders();
        this.repository.insert(message, 41000);

        verify(db, times(0)).delete("Header", "messageid = ?", new String[]{"100"});
        verify(db, times(0)).delete("Message", "id = ?", new String[]{"100"});
        assertMockedMessage(db);
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

    public static void assertMockedMessage(SQLiteDatabase mockedDb) {
        verify(mockedDb, times(3)).insert(anyString(), anyString(), any(ContentValues.class));
    }
}