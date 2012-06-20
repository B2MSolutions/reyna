package com.b2msolutions.reyna.services;

import android.app.IntentService;

import com.b2msolutions.reyna.R;
import com.b2msolutions.reyna.Repository;

public abstract class RepositoryService extends IntentService {
	
	protected Repository repository;

	public RepositoryService(String name) {
		super(name);
		this.repository = new Repository(this);
	}	
	
	protected String getLibraryName() {
		return this.getApplicationContext().getString(R.string.library_name);
	}
}
