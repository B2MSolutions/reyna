package com.b2msolutions.reyna.messageProvider;

import android.content.Context;
import com.b2msolutions.reyna.*;
import com.b2msolutions.reyna.system.*;
import com.google.gson.Gson;

import java.net.URI;
import java.net.URISyntaxException;

public class BatchProvider implements IMessageProvider {

    private static final String TAG = "com.b2msolutions.reyna.messageProvider.BatchProvider";

    private static final String PeriodicBackoutCheckTAG = "BatchProvider";

    private boolean batchDeleted = false;

    private boolean exitAsLastBatchWasNotFull = false;

    protected BatchConfiguration batchConfiguration;

    protected Repository repository;

    protected PeriodicBackoutCheck periodicBackoutCheck;

    public BatchProvider(Context context, Repository repository) {
        Logger.v(BatchProvider.TAG, "MessageProvider");

        this.repository = repository;
        this.batchConfiguration = new BatchConfiguration(context);
        this.periodicBackoutCheck = new PeriodicBackoutCheck(context);
        this.batchDeleted = false;
    }

    public Message getNext() throws URISyntaxException {
        Logger.v(BatchProvider.TAG, "getNext");

        if (exitAsLastBatchWasNotFull) {
            Logger.v(TAG, "getNext exit as there is no enough messages to form a batch");
            return null;
        }

        Message message = this.repository.getNext();
        Batch batch = new Batch();

        Header[] headers = null;
        int count = 0;
        long size= 0;
        long maxMessagesCount = this.batchConfiguration.getBatchMessageCount();
        long maxBatchSize = this.batchConfiguration.getBatchMessagesSize();

        while (message != null && count <  maxMessagesCount && size < maxBatchSize) {
            Logger.v(TAG, "adding message " + message.getId());
            headers = message.getHeaders();
            batch.add(message);

            size = this.getSize(batch);
            Logger.v(TAG, "batch size " + size);
            count++;
            message = this.repository.getNextMessageAfter(message.getId());
        }

        Logger.v(TAG, "adding message size " + size + " and maxBatchSize " + maxBatchSize);
        this.exitAsLastBatchWasNotFull = count != maxMessagesCount;
        if (size > maxBatchSize) {
            Logger.v(TAG, "removeLastMessage ");
            batch.removeLastMessage();
            this.exitAsLastBatchWasNotFull = false;
        }

        if (batch.getEvents().size() > 0) {
            Logger.v(TAG, "batch has messages ");
            BatchMessage batchMessage = batch.getEvents().get(batch.getEvents().size() - 1);

            Logger.v(TAG, "batch message id " + batchMessage.getReynaId());

            URI uri = URI.create(batchMessage.getUrl());

            String body = new Gson().toJson(batch, Batch.class);

            return new Message(batchMessage.getReynaId(), this.getBatchUploadUrl(uri), body, headers);
        }

        return null;
    }

    public void delete(Message message) {
        Logger.v(BatchProvider.TAG, "delete messages from id " + message.getId());

        this.repository.deleteMessagesFrom(message.getId());
        this.batchDeleted = true;
    }

    @Override
    public void close() {
        Logger.v(BatchProvider.TAG, "close");

        if (batchDeleted) {
            this.periodicBackoutCheck.record(PeriodicBackoutCheckTAG);
        }

        batchDeleted = false;
        this.repository.close();
    }

    @Override
    public boolean canSend() {
        Logger.v(BatchProvider.TAG, "canSend");

        long interval = (long)(this.batchConfiguration.getSubmitInterval() * 0.9);
        if(this.periodicBackoutCheck.timeElapsed(PeriodicBackoutCheckTAG, interval)) {
            Logger.v(BatchProvider.TAG, "canSend true, timeElapsed");
            return true;
        }

        return this.repository.getAvailableMessagesCount() >= this.batchConfiguration.getBatchMessageCount();
    }

    private URI getBatchUploadUrl(URI uri) throws URISyntaxException {
        if(this.batchConfiguration.getBatchUrl() != null) {
            return this.batchConfiguration.getBatchUrl();
        }

        return this.getUploadUrlFromMessageUrl(uri);
    }

    private URI getUploadUrlFromMessageUrl(URI uri) throws URISyntaxException {
        return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), "/api/1/batch", uri.getQuery(), uri.getFragment());

    }

    private long getSize(Batch batch) {
        String body = new Gson().toJson(batch, Batch.class);
        return body.getBytes().length;
    }
}
