package com.b2msolutions.reyna.services;

import android.content.Intent;
import com.b2msolutions.reyna.Dispatcher;
import com.b2msolutions.reyna.Dispatcher.Result;
import com.b2msolutions.reyna.Logger;
import com.b2msolutions.reyna.Message;

public class ForwardService extends RepositoryService {	
	
	private static final String TAG = "ForwardService";

    protected Dispatcher dispatcher;

	public ForwardService() {
		super(ForwardService.class.getName());

        Logger.v(TAG, "ForwardService()");

        this.dispatcher = new Dispatcher();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Logger.v(TAG, "onHandleIntent");
		
		try {					
			Message message = this.repository.getNext();
			while(message != null) {
				Logger.i(TAG, "ForwardService: processing message " + message.getId());
				Result result = dispatcher.sendMessage(this, message);

                Logger.i(TAG, "ForwardService: send message result: " + result.toString());
				
				if(result == Result.TEMPORARY_ERROR) return;

				this.repository.delete(message);
				message = this.repository.getNext();
			}		
		} catch(Exception e) {
            Logger.e(TAG, "onHandleIntent", e);
		} finally {
			this.repository.close();		
		}		
	}	
}
