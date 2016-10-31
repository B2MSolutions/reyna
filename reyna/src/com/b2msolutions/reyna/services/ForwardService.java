package com.b2msolutions.reyna.services;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import com.b2msolutions.reyna.*;
import com.b2msolutions.reyna.Dispatcher.Result;
import com.b2msolutions.reyna.blackout.Time;
import com.b2msolutions.reyna.system.*;
import com.b2msolutions.reyna.messageProvider.BatchProvider;
import com.b2msolutions.reyna.messageProvider.IMessageProvider;
import com.b2msolutions.reyna.messageProvider.MessageProvider;
import com.b2msolutions.reyna.system.Thread;

public class ForwardService extends WakefulService {

    private static String TAG = "com.b2msolutions.reyna.services.ForwardService";

    private static String PERIODIC_BACKOUT_TEMPORARY_ERROR = "ForwardService_Backout_Temporary_Error";

    protected static final long SLEEP_MILLISECONDS = 1000; // 1 second

    protected static final long TEMPORARY_ERROR_MILLISECONDS = 300000; // 5 minutes

    protected Dispatcher dispatcher;

    protected Thread thread;

    protected PeriodicBackoutCheck periodicBackoutCheck;

    protected Repository repository = null;

    public ForwardService() {
        super(ForwardService.class.getName());

        Logger.v(TAG, "ForwardService()");

        this.dispatcher = new Dispatcher();
        this.thread = new Thread();
        this.periodicBackoutCheck = new PeriodicBackoutCheck(this);
        this.repository = new Repository(this);
    }

    public static void start(Context context) {
        Logger.v(ForwardService.TAG, "start");

        Intent serviceIntent = new Intent();
        serviceIntent.setClass(context, ForwardService.class);
        context.startService(serviceIntent);
        WakefulBroadcastReceiver.startWakefulService(context, serviceIntent);
    }

    @Override
    protected void processIntent(Intent intent) {
        Logger.v(TAG, "onHandleIntent");

        IMessageProvider messageProvider = this.getMessageProvider();

        try {

            if(!this.periodicBackoutCheck.timeElapsed(ForwardService.PERIODIC_BACKOUT_TEMPORARY_ERROR, ForwardService.TEMPORARY_ERROR_MILLISECONDS)) {
                Logger.i(TAG, "ForwardService: temporary error, backing off...");
                return;
            }

            Result canSend = Dispatcher.canSend(this);
            if (canSend != Result.OK) {
                Logger.v(TAG, "ForwardService: cannot send " + canSend);
                return;
            }

            if (!messageProvider.canSend()) {
                Logger.v(TAG, "ForwardService: messageProvider cannot send");
                return;
            }

            Message message = messageProvider.getNext();
            while(message != null) {

                this.thread.sleep(SLEEP_MILLISECONDS);

                Logger.v(TAG, "ForwardService: processing message " + message.getId());

                Result result = dispatcher.sendMessage(this, message);

                Logger.i(TAG, "ForwardService: send message result: " + result.toString());

                if(result == Result.TEMPORARY_ERROR) {
                    Logger.i(TAG, "ForwardService: temporary error, backing off...");

                    this.periodicBackoutCheck.record(ForwardService.PERIODIC_BACKOUT_TEMPORARY_ERROR);
                    return;
                }

                if(result == Result.BLACKOUT || result == Result.NOTCONNECTED) {
                    return;
                }

                messageProvider.delete(message);
                message = messageProvider.getNext();
            }

        } catch(Exception e) {
            Logger.e(TAG, "onHandleIntent", e);
        } finally {
            messageProvider.close();
        }
    }

    protected IMessageProvider getMessageProvider() {
        if (new Preferences(this).getBatchUpload()) {
            Logger.v(TAG, "getMessageProvider BatchProvider");
            return new BatchProvider(this, repository);
        }

        Logger.v(TAG, "getMessageProvider MessageProvider");
        return new MessageProvider(repository);
    }
}
