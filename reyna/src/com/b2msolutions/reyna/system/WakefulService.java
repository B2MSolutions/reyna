package com.b2msolutions.reyna.system;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public abstract class WakefulService extends IntentService {

    private String tag;

    public WakefulService(String tag) {
        super(tag);
        this.tag = tag;
    }

    @Override
    public void onHandleIntent(Intent intent) {
        Logger.v(this.tag, "onHandleIntent");

        if (intent == null) {
            return;
        }

        try {
            this.processIntent(intent);
        } catch (Exception e) {
            Logger.e(this.tag, "onHandleIntent", e);
        } finally {
            WakefulBroadcastReceiver.completeWakefulIntent(intent);
        }
    }

    protected abstract void processIntent(Intent intent) throws Exception;
}