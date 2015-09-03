package com.b2msolutions.reyna.services;

import android.app.IntentService;
import com.b2msolutions.reyna.Logger;
import com.b2msolutions.reyna.Repository;

public abstract class RepositoryService extends IntentService {

    private static final String TAG = "RepositoryService";

    protected Repository repository;

    public RepositoryService(String name) {
        super(name);

        Logger.v(TAG, "RepositoryService()");

        this.repository = new Repository(this);
    }
}
