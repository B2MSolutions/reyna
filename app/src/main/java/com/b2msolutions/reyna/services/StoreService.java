package com.b2msolutions.reyna.services;

import android.content.Context;
import android.content.Intent;

import com.b2msolutions.reyna.Logger;
import com.b2msolutions.reyna.Message;
import com.b2msolutions.reyna.Preferences;
import com.b2msolutions.reyna.TimeRange;

public class StoreService extends RepositoryService {

    private static final String TAG = "StoreService";

    public static final String MESSAGE = "com.b2msolutions.reyna.MESSAGE";

    public static final String ACTION_MOVE = "com.b2msolutions.reyna.ACTION_MOVE";

    public static final String ACTION_STORE_MESSAGE = "com.b2msolutions.reyna.ACTION_STORE_MESSAGE";

    public static final String LOCATION = "com.b2msolutions.reyna.LOCATION";

    private boolean restartService = false;

    public StoreService() {
        super(StoreService.class.getName());

        Logger.v(TAG, "StoreService()");
    }

    public static void start(Context context, Message message) {
        Logger.v(TAG, "start");

        Intent service = new Intent(context, StoreService.class);
        service.setAction(StoreService.ACTION_STORE_MESSAGE);
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

    public static void changeDatabaseLocation(Context context, String location) {
        if (location.endsWith("/")) {
            location = location.substring(0, location.length() - 1);
        }
        Intent intent = new Intent(context, StoreService.class);
        intent.setAction(StoreService.ACTION_MOVE);
        intent.putExtra(StoreService.LOCATION, location + "/reyna.db");
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Logger.v(TAG, "onHandleIntent");

        if (intent.getAction() == null) return;

        if (intent.getAction().equals(ACTION_MOVE)) {
            String location = intent.getStringExtra(LOCATION);
            if(location.isEmpty()){
                return;
            }
            try {
                this.repository.moveDatabase(location);
            } catch (Exception e) {
                Logger.e(TAG, "onHandleIntent", e);
            }
        }

        if (intent.getAction().equals(ACTION_STORE_MESSAGE)) {
            Message message = (Message) intent.getSerializableExtra(MESSAGE);
            if (message == null) {
                return;
            }
            this.insert(message);
        }
    }

    private void insert(Message message) {
        Logger.v(TAG, "insert");

        this.repository.insert(message);
        this.startService(new Intent(this, ForwardService.class));
    }

    public void onDestroy() {
        if (this.restartService) {
            startService(new Intent(this, StoreService.class));
        }
    }
}
