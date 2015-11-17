package com.b2msolutions.reyna.messageProvider;

import com.b2msolutions.reyna.system.Message;

import java.net.URISyntaxException;

public interface IMessageProvider {
    Message getNext() throws URISyntaxException;

    void delete(Message message);

    void close();

    boolean canSend();
}
