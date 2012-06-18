package com.b2msolutions.reyna;

import android.app.IntentService;
import android.content.Intent;

public class StoreService extends IntentService {

	public StoreService() {
		super(StoreService.class.getName());
	}

	@Override
	protected void onHandleIntent(Intent intent) {
	}
}
