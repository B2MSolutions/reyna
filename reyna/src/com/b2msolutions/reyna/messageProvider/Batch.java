package com.b2msolutions.reyna.messageProvider;

import com.b2msolutions.reyna.system.Logger;
import com.b2msolutions.reyna.system.Message;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

public class Batch {

    private static String TAG = "com.b2msolutions.reyna.messageProvider.Batch";

    private final List<BatchMessage> events;

    public Batch() {
        this.events = new ArrayList<BatchMessage>();
    }

    public void add(Message message) {
        Logger.v(Batch.TAG, "add");

        BatchMessage batchMessage = new BatchMessage(message.getId(), message.getUrl(), message.getBody());
        this.events.add(batchMessage);
    }

    public List<BatchMessage> getEvents() {
        return events;
    }

    public void removeLastMessage() {
        Logger.v(Batch.TAG, "removeLastMessage");

        int size = this.events.size();
        if (size > 1) {
            this.events.remove(size -1);
        }
    }

    public String ToJson()
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append("{");
        buffer.append("\"events\"");
        buffer.append(":[");

        if (this.events.size() > 0)
        {
            for (BatchMessage item : this.events)
            {
                buffer.append(item.ToJson());
                buffer.append(",");
            }

            buffer = buffer.deleteCharAt(buffer.length() - 1);
        }

        buffer.append("]}");
        return buffer.toString();
    }
}
