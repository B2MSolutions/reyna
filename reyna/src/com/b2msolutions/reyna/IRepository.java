package com.b2msolutions.reyna;

public interface IRepository {
	
	public void insert(Message message);
	
	public Message getNext();
	
	public void delete(Message message);
}
