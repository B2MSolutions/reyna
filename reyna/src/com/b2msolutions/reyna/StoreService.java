package com.b2msolutions.reyna;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class StoreService extends IntentService {

	public static final String MESSAGE = "com.b2msolutions.reyna.REYNA_MESSAGE";
	
	protected IRepository store;

	public StoreService() {
		super(StoreService.class.getName());
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.i(this.getApplicationContext().getString(R.string.library_name), "StoreService:onHandleIntent");
		
		if(intent == null) {
			return;
		}
		
		Message message = (Message)intent.getSerializableExtra(MESSAGE);
		if(message != null) {
			this.getStore().insert(message);
		}
	}
	
	private IRepository getStore() {
		if(this.store == null) {
			this.store = new Repository(this);
		}
		
		return this.store;
	}	
}
