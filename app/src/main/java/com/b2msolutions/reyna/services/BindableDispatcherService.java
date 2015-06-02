package com.b2msolutions.reyna.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.b2msolutions.reyna.Message;
import com.b2msolutions.reyna.http.Result;

public abstract class BindableDispatcherService extends Service implements IDispatcherService {

    protected Context context;

    protected IBinder mBinder = new LocalBinder();

    @Override
    public void onCreate() {
        this.context = this;
    }

    @Override
    public void onDestroy() {
        this.context = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public abstract Result sendMessage(Message message);

    public class LocalBinder extends Binder {
        IDispatcherService getService() {
            return BindableDispatcherService.this;
        }
    }
}

