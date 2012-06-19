package com.b2msolutions.reyna;

import java.util.Enumeration;
import java.util.Hashtable;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Repository extends SQLiteOpenHelper implements IStore {

	public static final String DATABASE_NAME = "reyna.db";

	public static final int DATABASE_VERSION = 1;

	public Repository(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE Message (id INTEGER PRIMARY KEY AUTOINCREMENT, url TEXT, body TEXT);");
		db.execSQL("CREATE TABLE Header (id INTEGER PRIMARY KEY AUTOINCREMENT, messageid INTEGER, key TEXT, value TEXT, FOREIGN KEY(messageid) REFERENCES message(id));");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	public void store(String url, String body, Hashtable<String, String> headers) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.beginTransaction();
		try {
			ContentValues values = new ContentValues();
			values.put("url", url);
			values.put("body", body);
			
			long messageid = db.insert("Message", null, values);
			this.addHeaders(headers, db, messageid);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	private void addHeaders(Hashtable<String, String> headers,
			SQLiteDatabase db, long messageid) {

		if (headers == null)
			return;

		Enumeration<String> e = headers.keys();

		while (e.hasMoreElements()) {
			String key = e.nextElement();

			ContentValues headerValues = new ContentValues();
			headerValues.put("messageid", messageid);
			headerValues.put("key", key);
			headerValues.put("value", headers.get(key));

			db.insert("Header", null, headerValues);
		}
	}
}
