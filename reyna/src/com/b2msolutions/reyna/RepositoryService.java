package com.b2msolutions.reyna;

import android.app.IntentService;

public abstract class RepositoryService extends IntentService {
	
	protected IRepository store;

	public RepositoryService(String name) {
		super(name);
	}
	
	protected IRepository getStore() {
		if(this.store == null) {
			this.store = new Repository(this);
		}	
		
		return this.store;
	}	
}
