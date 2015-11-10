package com.b2msolutions.reyna.messageProvider;

import android.app.AlarmManager;
import android.content.Context;
import com.b2msolutions.reyna.system.Preferences;

import java.net.URI;

public class BatchConfiguration {

    private int batchMessagesCount = 0;

    private long batchMessagesSize = 0;

    private URI batchMessagesUrl = null;

    protected Preferences preferences = null;

    public BatchConfiguration(Context context) {
        this.preferences = new Preferences(context);
    }

    public int getBatchMessageCount() {
        return 100;
    }

    public long getBatchMessagesSize() {
        return 300 * 1024;
    }

    public long getCheckInterval() {
        return this.preferences.getBatchUploadCheckInterval();
    }

    public long getSubmitInterval() {
        return AlarmManager.INTERVAL_DAY;
    }

    public URI getBatchUrl() {
        return this.preferences.getBatchUploadUrl();
    }
}
