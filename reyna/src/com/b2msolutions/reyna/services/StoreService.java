package com.b2msolutions.reyna.services;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.b2msolutions.reyna.Message;

public class StoreService extends RepositoryService {

	private static final String TAG = "StoreService";

	public static final String MESSAGE = "com.b2msolutions.reyna.MESSAGE";
		
	public StoreService() {
		super(StoreService.class.getName());
		Log.v(TAG, "StoreService()");
	}
	
	public static void start(Context context, Message message) {
		Log.v(TAG, "start");
		Intent service = new Intent(context, StoreService.class);
		service.putExtra(StoreService.MESSAGE, message);
		context.startService(service);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.v(TAG, "onHandleIntent");
		
		if(intent == null) {
			return;
		}
		
		Message message = (Message)intent.getSerializableExtra(MESSAGE);
		if(message != null) {
			this.insert(message);
		}
	}

	private void insert(Message message) {
		Log.v(TAG, "insert");
		
		try {
			this.repository.insert(message);
			this.startService(new Intent(this, ForwardService.class));
		} finally {
			this.repository.close();
		}
	}
}
