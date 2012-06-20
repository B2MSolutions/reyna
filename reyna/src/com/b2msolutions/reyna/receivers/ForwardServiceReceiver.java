package com.b2msolutions.reyna.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.b2msolutions.reyna.services.ForwardService;

public class ForwardServiceReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		context.startService(new Intent(context, ForwardService.class));
	}
}
