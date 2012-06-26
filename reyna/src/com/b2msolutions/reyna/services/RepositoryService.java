package com.b2msolutions.reyna.services;

import android.app.IntentService;
import android.util.Log;

import com.b2msolutions.reyna.R;
import com.b2msolutions.reyna.Repository;

public abstract class RepositoryService extends IntentService {
	
	private static final String TAG = "RepositoryService";
	
	protected Repository repository;

	public RepositoryService(String name) {
		super(name);
		
		Log.v(TAG, "RepositoryService()");
		this.repository = new Repository(this);
	}	
	
	protected String getLibraryName() {
		Log.v(TAG, "getLibraryName()");
		
		return this.getApplicationContext().getString(R.string.reyna_library_name);
	}
}
