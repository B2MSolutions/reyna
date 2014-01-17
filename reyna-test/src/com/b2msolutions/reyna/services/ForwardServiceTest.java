package com.b2msolutions.reyna.services;

import android.content.Context;
import android.content.Intent;
import com.b2msolutions.reyna.Dispatcher;
import com.b2msolutions.reyna.Dispatcher.Result;
import com.b2msolutions.reyna.Message;
import com.b2msolutions.reyna.Repository;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.RobolectricTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URISyntaxException;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
public class ForwardServiceTest {
	
	private ForwardService forwardService;

	@Mock Dispatcher dispatcher;
	
	@Mock Repository repository;

    private Context context;

    @Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
        this.context = Robolectric.getShadowApplication().getApplicationContext();
		this.forwardService = new ForwardService();
		this.forwardService.dispatcher = dispatcher;
		this.forwardService.repository = repository;
	}
	
	@Test
	public void testConstruction() {        
        assertNotNull(this.forwardService);
    }	

	@Test
	public void whenNotNullIntentShouldNotThrow() {
		this.forwardService.onHandleIntent(new Intent());
	}

	@Test
	public void whenThereAreNoMessagesShouldNotThrow() {
		this.forwardService.onHandleIntent(null);
	}
	
	@Test
	public void whenMoveNextThrowsShouldNotThrow() throws URISyntaxException {
		when(this.repository.getNext()).thenThrow(new URISyntaxException("", ""));
		this.forwardService.onHandleIntent(null);
	}
		
	@Test
	public void whenSingleMessageAndDispatchReturnsOKShouldDeleteMessage() throws URISyntaxException {
		Message message = mock(Message.class);
		when(this.repository.getNext()).thenReturn(message).thenReturn(null);
		when(this.dispatcher.sendMessage(this.forwardService, message)).thenReturn(Result.OK);
		
		this.forwardService.onHandleIntent(null);
		verify(this.dispatcher).sendMessage(this.forwardService, message);
		verify(this.repository).delete(message);
	}
	
	@Test
	public void whenTwoMessagesAndDispatchReturnsOKShouldDeleteMessages() throws URISyntaxException {
		Message message1 = mock(Message.class);
		Message message2 = mock(Message.class);
		when(this.repository.getNext())
			.thenReturn(message1)
			.thenReturn(message2)
			.thenReturn(null);
		
		when(this.dispatcher.sendMessage(this.forwardService, message1)).thenReturn(Result.OK);
		when(this.dispatcher.sendMessage(this.forwardService, message2)).thenReturn(Result.OK);
		
		this.forwardService.onHandleIntent(null);
		InOrder inorder = inOrder(this.dispatcher, this.repository);
				
		inorder.verify(this.dispatcher).sendMessage(this.forwardService, message1);
		inorder.verify(this.repository).delete(message1);
		inorder.verify(this.dispatcher).sendMessage(this.forwardService, message2);
		inorder.verify(this.repository).delete(message2);
	}
	
	@Test
	public void whenSingleMessageAndDispatchReturnsTemporaryErrorShouldNotDeleteMessage() throws URISyntaxException {
		Message message = mock(Message.class);
		when(this.repository.getNext()).thenReturn(message).thenReturn(null);
		when(this.dispatcher.sendMessage(this.forwardService, message)).thenReturn(Result.TEMPORARY_ERROR);
		
		this.forwardService.onHandleIntent(null);
		verify(this.dispatcher).sendMessage(this.forwardService, message);
		verify(this.repository, never()).delete(message);
	}

    @Test
    public void whenSingleMessageAndDispatchReturnsBlackoutShouldNotDeleteMessage() throws URISyntaxException {
        Message message = mock(Message.class);
        when(this.repository.getNext()).thenReturn(message).thenReturn(null);
        when(this.dispatcher.sendMessage(this.forwardService, message)).thenReturn(Result.BLACKOUT);

        this.forwardService.onHandleIntent(null);
        verify(this.dispatcher).sendMessage(this.forwardService, message);
        verify(this.repository, never()).delete(message);
    }

	@Test
	public void whenTwoMessagesAndFirstDispatchReturnsTemporaryErrorShouldNotDeleteMessages() throws URISyntaxException {
		Message message1 = mock(Message.class);
		Message message2 = mock(Message.class);
		when(this.repository.getNext())
			.thenReturn(message1)
			.thenReturn(message2)
			.thenReturn(null);
		
		when(this.dispatcher.sendMessage(this.forwardService, message1)).thenReturn(Result.TEMPORARY_ERROR);
		
		this.forwardService.onHandleIntent(null);
		InOrder inorder = inOrder(this.dispatcher, this.repository);
				
		inorder.verify(this.dispatcher).sendMessage(this.forwardService, message1);
		inorder.verify(this.repository, never()).delete(message1);
		inorder.verify(this.dispatcher, never()).sendMessage(this.forwardService, message2);
		inorder.verify(this.repository, never()).delete(message2);
	}

    @Test
    public void whenTwoMessagesAndFirstDispatchReturnsBlackoutShouldNotDeleteMessages() throws URISyntaxException {
        Message message1 = mock(Message.class);
        Message message2 = mock(Message.class);
        when(this.repository.getNext())
                .thenReturn(message1)
                .thenReturn(message2)
                .thenReturn(null);

        when(this.dispatcher.sendMessage(this.forwardService, message1)).thenReturn(Result.BLACKOUT);

        this.forwardService.onHandleIntent(null);
        InOrder inorder = inOrder(this.dispatcher, this.repository);

        inorder.verify(this.dispatcher).sendMessage(this.forwardService, message1);
        inorder.verify(this.repository, never()).delete(message1);
        inorder.verify(this.dispatcher, never()).sendMessage(this.forwardService, message2);
        inorder.verify(this.repository, never()).delete(message2);
    }

	@Test
	public void whenTwoMessagesAndFirstDispatchReturnsPermanentErrorShouldDeleteMessages() throws URISyntaxException {
		Message message1 = mock(Message.class);
		Message message2 = mock(Message.class);
		when(this.repository.getNext())
			.thenReturn(message1)
			.thenReturn(message2)
			.thenReturn(null);
		
		when(this.dispatcher.sendMessage(this.forwardService, message1)).thenReturn(Result.PERMANENT_ERROR);
		when(this.dispatcher.sendMessage(this.forwardService, message2)).thenReturn(Result.OK);
		
		this.forwardService.onHandleIntent(null);
		InOrder inorder = inOrder(this.dispatcher, this.repository);
				
		inorder.verify(this.dispatcher).sendMessage(this.forwardService, message1);
		inorder.verify(this.repository).delete(message1);
		inorder.verify(this.dispatcher).sendMessage(this.forwardService, message2);
		inorder.verify(this.repository).delete(message2);
	}
}
