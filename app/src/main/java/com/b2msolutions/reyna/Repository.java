package com.b2msolutions.reyna;

import android.content.Context;

import com.b2msolutions.reyna.services.FileManager;

import java.net.URISyntaxException;

public class Repository {

	protected static ReynaSqlHelper reynaSqlHelper;
	protected Context context;
    protected FileManager fileManager;
    protected Preferences preferences;

	public Repository(Context context){
		this.context = context;
        this.fileManager = new FileManager();
        this.preferences = new Preferences(context);
	}

    protected synchronized static ReynaSqlHelper getSqlHelper(Context context){
		if(reynaSqlHelper == null){
			reynaSqlHelper = new ReynaSqlHelper(context.getApplicationContext());
		}
		return reynaSqlHelper;
	}

	public void insert(Message message) {
		getSqlHelper(this.context).insert(message);
	}

	public Message getNext()  {
        try {
            return getSqlHelper(this.context).getNext();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

	public void delete(Message message) {
		getSqlHelper(this.context).delete(message);
	}

    public void moveDatabase(String path) throws Exception{
        String originalPath = getSqlHelper(this.context).getReadableDatabase().getPath();

        if(path.equals(originalPath)){
            return;
        }

        if(!this.fileManager.getFile(path).exists()){
            this.fileManager.copy(originalPath, path);
            this.preferences.saveDbFile(path);
            Repository.reynaSqlHelper = null;
            this.fileManager.deleteDatabase(originalPath);
        } else {
            this.preferences.saveDbFile(path);
            Repository.reynaSqlHelper = null;
        }
    }
}
