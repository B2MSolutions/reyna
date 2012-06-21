package com.b2msolutions.reyna.services;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.b2msolutions.reyna.Message;
import com.b2msolutions.reyna.R;

public class StoreService extends RepositoryService {

	public static final String MESSAGE = "com.b2msolutions.reyna.MESSAGE";
		
	public StoreService() {
		super(StoreService.class.getName());
	}
	
	public static void start(Context context, Message message) {
		Intent service = new Intent(context, StoreService.class);
		service.putExtra(StoreService.MESSAGE, message);
		context.startService(service);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.i(this.getApplicationContext().getString(R.string.library_name), "StoreService:onHandleIntent");
		
		if(intent == null) {
			return;
		}
		
		Message message = (Message)intent.getSerializableExtra(MESSAGE);
		if(message != null) {
			this.repository.insert(message);
			this.startService(new Intent(this, ForwardService.class));
		}
	}
}
