package com.b2msolutions.reyna.messageProvider;

import com.google.gson.JsonObject;

public class BatchMessage {

    private final String url;

    private final long reynaId;

    private final JsonObject payload;

    public BatchMessage(long reynaId, String url, JsonObject payload) {

        this.reynaId = reynaId;
        this.url = url;
        this.payload = payload;
    }

    public String getUrl() {
        return this.url;
    }

    public long getReynaId() {
        return this.reynaId;
    }

    public JsonObject getPayload() {
        return this.payload;
    }
}
