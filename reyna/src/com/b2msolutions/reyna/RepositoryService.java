package com.b2msolutions.reyna;

import android.app.IntentService;

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
