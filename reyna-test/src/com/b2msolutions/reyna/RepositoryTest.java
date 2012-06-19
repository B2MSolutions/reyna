package com.b2msolutions.reyna;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class RepositoryTest {
	
	@Test
	public void constructionShouldNotThrow() {		
		Context context = Robolectric.application.getApplicationContext();
        Repository r = new Repository(context);
        assertNotNull(r);
    }	

	@Test
	public void onCreateShouldNotThrow() {		
		Context context = Robolectric.application.getApplicationContext();
        Repository r = new Repository(context);
        SQLiteDatabase db = r.getReadableDatabase();
        assertNotNull(db);
    }
}
