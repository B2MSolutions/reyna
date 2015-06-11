package com.b2msolutions.reyna.services;

import com.b2msolutions.reyna.Dispatcher;
import com.b2msolutions.reyna.Logger;
import com.b2msolutions.reyna.Message;
import com.b2msolutions.reyna.http.Result;

public class DispatcherService extends BindableDispatcherService implements IDispatcherService {

    protected static final String TAG = "DispatcherService";
    protected Dispatcher dispatcher;

    @Override
    public void onCreate() {
        super.onCreate();
        this.dispatcher = new Dispatcher();
    }

    @Override
    public Result sendMessage(Message message) {
        Logger.v(TAG, "sendMessage");
        return dispatcher.sendMessage(context, message);
    }
}

