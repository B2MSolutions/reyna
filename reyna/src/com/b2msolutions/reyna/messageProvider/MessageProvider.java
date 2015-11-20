package com.b2msolutions.reyna.messageProvider;

import android.content.Context;
import com.b2msolutions.reyna.system.Header;
import com.b2msolutions.reyna.system.Logger;
import com.b2msolutions.reyna.system.Message;
import com.b2msolutions.reyna.Repository;

import java.net.URISyntaxException;

public class MessageProvider implements IMessageProvider {

    private static final String TAG = "com.b2msolutions.reyna.messageProvider.MessageProvider";

    protected Repository repository;

    public MessageProvider(Context context) {
        Logger.v(MessageProvider.TAG, "MessageProvider");

        this.repository = new Repository(context);
    }

    public Message getNext() throws URISyntaxException {
        Logger.v(MessageProvider.TAG, "getNext");

        Message message = this.repository.getNext();

        if (message == null) {
            Logger.v(MessageProvider.TAG, "getNext, null message");
            return null;
        }

        this.addReynaSpecificHeaders(message);
        return message;
    }

    public void delete(Message message) {
        Logger.v(MessageProvider.TAG, "delete");

        this.repository.delete(message);
    }

    @Override
    public void close() {
        Logger.v(MessageProvider.TAG, "close");

        this.repository.close();
    }

    @Override
    public boolean canSend() {
        return true;
    }

    private void addReynaSpecificHeaders(Message message) {
        Logger.v(MessageProvider.TAG, "addReynaSpecificHeaders");
        Header header = new Header("reyna-id", message.getId().toString());
        message.addHeader(header);
    }
}
