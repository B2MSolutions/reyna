package com.b2msolutions.reyna;

import android.content.Intent;
import android.util.Log;

public class ForwardService extends RepositoryService {

	public ForwardService() {
		super(ForwardService.class.getName());
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.i(this.getApplicationContext().getString(R.string.library_name), "ForwardService: onHandleIntent");
		
		Message message = getStore().getNext();
		while(message != null) {
			Log.i(this.getApplicationContext().getString(R.string.library_name), "ForwardService: processing message " + message.getId());
			if(!sendMessage(message)) break;
			
			getStore().delete(message);
			message = getStore().getNext();
		}
	}
	
	protected boolean sendMessage(Message message) {
		return true;
	}
}
