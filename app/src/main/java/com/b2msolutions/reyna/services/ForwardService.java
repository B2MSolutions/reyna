package com.b2msolutions.reyna.services;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.b2msolutions.reyna.Dispatcher;
import com.b2msolutions.reyna.Header;
import com.b2msolutions.reyna.Logger;
import com.b2msolutions.reyna.Message;
import com.b2msolutions.reyna.Preferences;
import com.b2msolutions.reyna.Thread;
import com.b2msolutions.reyna.http.Result;

public class ForwardService extends RepositoryService {

    protected static final String TAG = "ForwardService";
    protected static final long SLEEP_MILLISECONDS = 1000; // 1 second

    protected Dispatcher dispatcher;
    protected Thread thread;
    protected Preferences preferences;
    protected IDispatcherService mService;

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
            this.doBindService();
			Message message = this.repository.getNext();
			while(message != null) {
                this.thread.sleep(SLEEP_MILLISECONDS);
				Logger.i(TAG, "ForwardService: processing message " + message.getId());

                this.addReynaSpecificHeaders(message);

                if(mService==null){
                    return;
                }

				Result result = mService.sendMessage(message);

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
            if(mService!= null) {
                this.doUnbindService();
            }
		}		
	}

    private void doBindService() {
        Intent intent = new Intent("com.b2msolutions.reyna.BindDispatcher");
        this.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private void doUnbindService() {
        if (mService != null) {
            unbindService(mConnection);
            mService = null;
        }
    }

    private void addReynaSpecificHeaders(Message message) {
        Header header = new Header("reyna-id", message.getId().toString());
        message.addHeader(header);
    }

    protected ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            if(service!=null) {
                mService = ((DispatcherService.LocalBinder) service).getService();
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    };
}
