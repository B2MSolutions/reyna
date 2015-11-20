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

        BatchMessage batchMessage = new BatchMessage(message.getId(), message.getUrl(), this.getBody(message.getBody()));
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
    private JsonObject getBody(String body) {
        Logger.v(Batch.TAG, "getBody");

        try {
            JsonParser parser = new JsonParser();
            return parser.parse(body).getAsJsonObject();
        }
        catch(Exception ignored) {
        }

        JsonObject element = new JsonObject();
        element.addProperty("body", body);
        return element;
    }
}
