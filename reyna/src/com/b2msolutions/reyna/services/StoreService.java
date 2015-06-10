package com.b2msolutions.reyna.services;

import android.content.Context;
import android.content.Intent;
import com.b2msolutions.reyna.*;

public class StoreService extends RepositoryService {

    private static final String TAG = "StoreService";

    private static final long MINIMUM_STORAGE_LIMIT = 1867776; // 1Mb 800Kb

    public static final String MESSAGE = "com.b2msolutions.reyna.MESSAGE";

    public StoreService() {
        super(StoreService.class.getName());

        Logger.v(TAG, "StoreService()");
    }

    public static void start(Context context, Message message) {
        Logger.v(TAG, "start");

        Intent service = new Intent(context, StoreService.class);
        service.putExtra(StoreService.MESSAGE, message);
        context.startService(service);
    }

    public static void setLogLevel(int level) {
        Logger.v(TAG, "setLogLevel: " + level);

        Logger.setLevel(level);
    }

    public static void setCellularDataBlackout(Context context, TimeRange range) {
        Logger.v(TAG, "setCellularDataBlackout: " + range);
        new Preferences(context).saveCellularDataBlackout(range);
    }

    public static void setStorageSizeLimit(Context context, long limit) {
        if (limit <= 0) {
            return;
        }

        limit = limit < MINIMUM_STORAGE_LIMIT ? MINIMUM_STORAGE_LIMIT : limit;

        Preferences preferences = new Preferences(context);
        preferences.saveStorageSize(limit);

        Repository repo = new Repository(context);
        repo.shrinkDb(limit);
    }

    public static long getStorageSizeLimit(Context context) {
        Preferences preferences = new Preferences(context);
        return preferences.getStorageSize();
    }

    public static void resetStorageSizeLimit(Context context) {
        Preferences preferences = new Preferences(context);
        preferences.resetStorageSize();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Logger.v(TAG, "onHandleIntent");

        if(intent == null) {
            return;
        }

        Message message = (Message)intent.getSerializableExtra(MESSAGE);
        if(message != null) {
            this.insert(message);
        }
    }

    private void insert(Message message) {
        Logger.v(TAG, "insert");

        long limit = getStorageSizeLimit(this);
        try {
            if (limit == -1) {
                this.repository.insert(message);
            }
            else {
                this.repository.insert(message, getStorageSizeLimit(this));
            }

            this.startService(new Intent(this, ForwardService.class));
        } finally {
            this.repository.close();
        }
    }
}
