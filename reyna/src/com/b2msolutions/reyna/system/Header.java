package com.b2msolutions.reyna.system;

import java.io.Serializable;

public class Header implements Serializable {

    private static final long serialVersionUID = 6139998515568441139L;

    private final Long id;

    private final String key;

    private final String value;

    public Header(String key, String value) {
        this(null, key, value);
    }

    public Header(Long id, String key, String value) {
        this.id = id;
        this.key = key;
        this.value = value;
    }

    public Long getId() {
        return this.id;
    }

    public String getKey() {
        return this.key;
    }

    public String getValue() {
        return this.value;
    }
}
