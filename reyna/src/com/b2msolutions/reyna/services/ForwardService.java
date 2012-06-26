package com.b2msolutions.reyna.services;

import android.content.Intent;
import android.util.Log;

import com.b2msolutions.reyna.Dispatcher;
import com.b2msolutions.reyna.Dispatcher.Result;
import com.b2msolutions.reyna.Message;
import com.b2msolutions.reyna.R;

public class ForwardService extends RepositoryService {	
	
	private static final String TAG = "ForwardService";
	
	protected Dispatcher dispatcher;
	
	public ForwardService() {
		super(ForwardService.class.getName());

		Log.v(TAG, "ForwardService()");
		this.dispatcher = new Dispatcher();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.v(TAG, "onHandleIntent");
		
		try {					
			Message message = this.repository.getNext();
			while(message != null) {
				Log.i(this.getApplicationContext().getString(R.string.library_name), "ForwardService: processing message " + message.getId());
				Result result = dispatcher.sendMessage(message);
				
				Log.i(this.getApplicationContext().getString(R.string.library_name), "ForwardService: send message result: " + result.toString());
				
				if(result == Result.TEMPORARY_ERROR) return;

				this.repository.delete(message);
				message = this.repository.getNext();
			}		
		} catch(Exception e) {
			Log.e(TAG, "onHandleIntent", e);
		} finally {
			this.repository.close();		
		}		
	}	
}
