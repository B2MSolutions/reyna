package com.b2msolutions.reyna;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Repository extends SQLiteOpenHelper {
	
	public static final String DATABASE_NAME = "reyna.db";
    
	public static final int DATABASE_VERSION = 1;
        
	public Repository(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE Message (ID INTEGER PRIMARY KEY AUTOINCREMENT,URL TEXT, BODY TEXT);");            
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}
}
