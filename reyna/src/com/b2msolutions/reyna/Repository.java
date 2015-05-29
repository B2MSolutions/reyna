package com.b2msolutions.reyna;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class Repository extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "reyna.db";

    public static final int DATABASE_VERSION = 1;

    private static final String TAG = "Repository";

    public Repository(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Logger.v(TAG, "onCreate");
        db.execSQL("CREATE TABLE Message (id INTEGER PRIMARY KEY AUTOINCREMENT, url TEXT, body TEXT);");
        db.execSQL("CREATE TABLE Header (id INTEGER PRIMARY KEY AUTOINCREMENT, messageid INTEGER, key TEXT, value TEXT, FOREIGN KEY(messageid) REFERENCES message(id));");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Logger.v(TAG, "onUpgrade");
    }

    public void insert(Message message) {
        Logger.v(TAG, "insert");
        if (message == null) {
            return;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        this.insertMessage(db, message);
    }

    public void insert(Message message, long dbSizeLimit) {
        Logger.v(TAG, "insert with limit");
        if (message == null) {
            return;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        long dbSize = this.getDbSize(db);

        if (this.dbSizeApproachesLimit(dbSize, dbSizeLimit)) {
            this.clearOldRecords(db, message);
        } else if (dbSize > dbSizeLimit) {
            this.shrinkDb(db, dbSizeLimit, dbSize);
        }

        this.insertMessage(db, message);
    }

    private boolean dbSizeApproachesLimit(long dbSize, long limit) {
        return (limit > dbSize) && (limit - dbSize) < (limit * 0.2); // less than 20% is considered as close to threshold
    }

    private void shrinkDb(SQLiteDatabase db, long limit, long dbSizeBeforeShrink) {
        long dbSize = dbSizeBeforeShrink;
        do {
            this.shrink(db, limit, dbSize);
            dbSize = this.getDbSize(db);
        }
        while (dbSize > limit);

        if (this.shouldVacuum(dbSizeBeforeShrink, limit)) {
            this.vacuum(db);
        }
    }

    private void shrink(SQLiteDatabase db, long limit, long dbSize) {
        double limitPercentage = 1 - (double)limit / dbSize;
        long numberOfMessages = this.getNumberOfMessages(db);
        long numberOfMessagesToRemove = Math.round(numberOfMessages * limitPercentage);
        numberOfMessagesToRemove = numberOfMessagesToRemove == 0 ? 1 : numberOfMessagesToRemove;

        try {
            db.beginTransaction();

            long thresholdId = this.getMessageIdToWhichShrink(db, numberOfMessagesToRemove);

            db.execSQL("delete from Message where id < " + thresholdId);
            db.execSQL("delete from Header where messageid < " + thresholdId);

            db.setTransactionSuccessful();
        }
        finally {
            if (db.inTransaction()) {
                db.endTransaction();
            }
        }
    }

    private boolean shouldVacuum(long dbSize, long limit) {
        long difference = dbSize - limit;

        // perform vacuuming if difference size exceeds 10% of the db size
        return ((double)difference / dbSize) * 100 > 10;
    }

    private void vacuum(SQLiteDatabase db) {
        db.execSQL("vacuum");
    }

    private long getMessageIdToWhichShrink(SQLiteDatabase db, long numberOfMessagesToRemove) {
        Cursor cursor = db.rawQuery("select id from Message limit 1 offset " + numberOfMessagesToRemove, null);
        cursor.moveToFirst();
        long id = cursor.getLong(0);
        cursor.close();

        return id;
    }

    private long getNumberOfMessages(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("select count(*) from Message", null);
        cursor.moveToFirst();
        long numberOfMessages = cursor.getLong(0);
        cursor.close();

        return numberOfMessages;
    }

    private long getDbSize(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("pragma page_count", null);
        cursor.moveToNext();
        long pageCount = cursor.getLong(0);
        cursor.close();

        return pageCount * db.getPageSize();
    }

    private void insertMessage(SQLiteDatabase db, Message message) {
        try {
            db.beginTransaction();
            ContentValues values = new ContentValues();
            values.put("url", message.getUrl());
            values.put("body", message.getBody());

            long messageId = db.insert("Message", null, values);
            this.addHeaders(db, messageId, message.getHeaders());
            db.setTransactionSuccessful();

            Logger.v("reyna", "Repository: inserted message " + messageId);
        }
        finally {
            if (db != null && db.inTransaction()) {
                db.endTransaction();
            }
        }
    }

    private void clearOldRecords(SQLiteDatabase db, Message message) {
        Long oldestMessageId = findOldestMessageIdWithType(db, message.getUrl());

        if (oldestMessageId == null) {
            return;
        }

        Message messageToRemove = new Message(oldestMessageId, message.getURI(), null, null);
        this.deleteExistingMessage(db, messageToRemove);
    }

    private Long findOldestMessageIdWithType(SQLiteDatabase db, String type) {
        Cursor cursor = db.query("Message", new String[]{"min(id)"}, "url=?", new String[]{type}, null, null, null);

        if (cursor.moveToNext()) {
            long result = cursor.getLong(0);
            cursor.close();
            return result;
        }

        cursor.close();
        return null;
    }

    public Message getNext() throws URISyntaxException {
        Logger.v(TAG, "getNext");
        Cursor messageCursor = null;
        Cursor headersCursor = null;

        try {
            SQLiteDatabase db = this.getReadableDatabase();
            messageCursor = db.query("Message", new String[] { "id", "url",
                    "body" }, null, null, null, null, "id", "1");
            if (!messageCursor.moveToFirst())
                return null;

            long messageid = messageCursor.getLong(0);
            String url = messageCursor.getString(1);
            String body = messageCursor.getString(2);

            headersCursor = db.query("Header", new String[] { "id", "key",
                            "value" }, "messageid = " + messageid, null, null, null,
                    null);

            ArrayList<Header> headers = new ArrayList<Header>();
            while (headersCursor.moveToNext()) {
                headers.add(new Header(headersCursor.getLong(0), headersCursor
                        .getString(1), headersCursor.getString(2)));
            }

            Header[] headersForMessage = new Header[headers.size()];
            headers.toArray(headersForMessage);

            return new Message(messageid, new URI(url), body,
                    (Header[]) headersForMessage);
        } finally {
            if (messageCursor != null)
                messageCursor.close();
            if (headersCursor != null)
                headersCursor.close();
        }
    }

    public void delete(Message message) {
        Logger.v(TAG, "delete");
        if (message == null)
            return;
        if (message.getId() == null)
            return;

        SQLiteDatabase db = this.getReadableDatabase();
        if (!this.doesMessageExist(db, message))
            return;

        this.deleteExistingMessage(db, message);
    }

    private void deleteExistingMessage(SQLiteDatabase db, Message message) {
        Logger.v(TAG, "deleteExistingMessage");
        db.beginTransaction();
        try {
            String[] args = new String[] { message.getId().toString() };
            db.delete("Header", "messageid = ?", args);
            db.delete("Message", "id = ?", args);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private boolean doesMessageExist(SQLiteDatabase db, Message message) {
        Logger.v(TAG, "doesMessageExist");

        Cursor cursor = null;
        try {
            cursor = db.query("Message", new String[] { "id" }, "id = ?",
                    new String[] { message.getId().toString() }, null, null,
                    null);
            return cursor.moveToFirst();
        } finally {
            if (cursor != null)
                cursor.close();
        }
    }

    private void addHeaders(SQLiteDatabase db, long messageid, Header[] headers) {
        Logger.v(TAG, "addHeaders");

        for (Header header : headers) {
            ContentValues headerValues = new ContentValues();
            headerValues.put("messageid", messageid);
            headerValues.put("key", header.getKey());
            headerValues.put("value", header.getValue());

            db.insert("Header", null, headerValues);
        }
    }
}