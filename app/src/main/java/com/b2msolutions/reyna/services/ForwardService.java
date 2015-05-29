package com.b2msolutions.reyna.services;

import android.content.Context;
import android.content.Intent;
import com.b2msolutions.reyna.*;
import com.b2msolutions.reyna.Dispatcher.Result;
import com.b2msolutions.reyna.Thread;

public class ForwardService extends RepositoryService {

    protected static final String TAG = "ForwardService";

    protected static final long SLEEP_MILLISECONDS = 1000; // 1 second

    protected Dispatcher dispatcher;

    protected Thread thread;

    protected Preferences preferences;

    public static void setErrorTimeout(Context context, long timeoutMilliseconds){
        new Preferences(context).saveTemporaryErrorTimeout(timeoutMilliseconds);
    }

	public ForwardService() {
		super(ForwardService.class.getName());

        Logger.v(TAG, "ForwardService()");

        this.preferences = new Preferences(this);
        this.dispatcher = new Dispatcher();
        this.thread = new Thread();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Logger.v(TAG, "onHandleIntent");
		
		try {
			Message message = this.repository.getNext();
			while(message != null) {
                this.thread.sleep(SLEEP_MILLISECONDS);
				Logger.i(TAG, "ForwardService: processing message " + message.getId());

                this.addReynaSpecificHeaders(message);

				Result result = dispatcher.sendMessage(this, message);

                Logger.i(TAG, "ForwardService: send message result: " + result.toString());
				
				if(result == Result.TEMPORARY_ERROR) {
                    Logger.i(TAG, "ForwardService: temporary error, backing off...");
                    this.thread.sleep(preferences.getTemporaryErrorTimeout());
                    return;
                }

                if(result == Result.BLACKOUT || result == Result.NOTCONNECTED) {
                    return;
                }

				this.repository.delete(message);
				message = this.repository.getNext();
			}		
		} catch(Exception e) {
            Logger.e(TAG, "onHandleIntent", e);
		} finally {
			this.repository.close();		
		}		
	}

    private void addReynaSpecificHeaders(Message message) {
        Header header = new Header("reyna-id", message.getId().toString());
        message.addHeader(header);
    }
}
