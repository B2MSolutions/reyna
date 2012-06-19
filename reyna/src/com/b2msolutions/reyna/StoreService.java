package com.b2msolutions.reyna;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class StoreService extends IntentService {

	public StoreService() {
		super(StoreService.class.getName());
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.i(this.getApplicationContext().getString(R.string.library_name), "StoreService:onHandleIntent");
	}
}
