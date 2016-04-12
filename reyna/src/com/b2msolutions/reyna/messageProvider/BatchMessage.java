package com.b2msolutions.reyna.messageProvider;

public class BatchMessage {

    private final String url;

    private final long reynaId;

    private final String payload;

    public BatchMessage(long reynaId, String url, String payload) {

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

    public String getPayload() {
        return this.payload;
    }

    public String ToJson()
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append("{");
        buffer.append("\"url\"");
        buffer.append(":");
        buffer.append("\"");
        buffer.append(this.url);
        buffer.append("\",");

        buffer.append("\"reynaId\"");
        buffer.append(":");
        buffer.append(this.reynaId);
        buffer.append(",");

        buffer.append("\"payload\"");
        buffer.append(":");
        buffer.append(this.payload);

        buffer.append("}");

        return buffer.toString();
    }
}
