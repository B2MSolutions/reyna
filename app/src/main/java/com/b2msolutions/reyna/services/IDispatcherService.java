package com.b2msolutions.reyna.services;

import com.b2msolutions.reyna.Message;
import com.b2msolutions.reyna.http.Result;

public interface IDispatcherService{
    Result sendMessage(Message message);
}
