package com.b2msolutions.reyna.http;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

public class HttpResultReceiver extends ResultReceiver {

    public HttpResultReceiver(Handler handler) {
        super(handler);
    }

    private Listener listener;

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (listener != null)
            listener.onReceiveResult(resultCode, resultData);
    }

    public static interface Listener {
        void onReceiveResult(int resultCode, Bundle resultData);
    }
}
