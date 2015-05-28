package com.b2msolutions.reyna.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.b2msolutions.reyna.Logger;
import com.b2msolutions.reyna.services.ForwardService;

public class ForwardServiceReceiver extends BroadcastReceiver {
	private static final String TAG = "ForwardServiceReceiver";
	
	@Override
	public void onReceive(Context context, Intent intent) {
        Logger.v(TAG, "onReceive");
		
		context.startService(new Intent(context, ForwardService.class));
	}
}
