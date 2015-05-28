package com.b2msolutions.reyna.services;

import android.content.Context;
import android.content.Intent;
import com.b2msolutions.reyna.*;
import com.b2msolutions.reyna.Dispatcher.Result;
import com.b2msolutions.reyna.Thread;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URISyntaxException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(ReynaTestRunner.class)
public class ForwardServiceTest {
	
	private ForwardService forwardService;

	@Mock Dispatcher dispatcher;
	
	@Mock Repository repository;

    @Mock Thread thread;

    private Context context;

    @Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
        this.context = Robolectric.getShadowApplication().getApplicationContext();
		this.forwardService = new ForwardService();
		this.forwardService.dispatcher = dispatcher;
		this.forwardService.repository = repository;
        this.forwardService.thread = thread;
	}

    @Test
    public void sleepTimeoutShouldBeCorrect() {
      assertEquals(1000, ForwardService.SLEEP_MILLISECONDS);
    };

    @Test
    public void temporaryErrorTimeoutShouldBeCorrect() {
        assertEquals(300000, ForwardService.TEMPORARY_ERROR_MILLISECONDS);
    };

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
    public void whenThereAreNoMessagesShouldNotSleep() throws InterruptedException {
        this.forwardService.onHandleIntent(null);
        verify(this.thread, never()).sleep(anyLong());
    }

	@Test
	public void whenMoveNextThrowsShouldNotThrow() throws URISyntaxException {
		when(this.repository.getNext()).thenThrow(new URISyntaxException("", ""));
		this.forwardService.onHandleIntent(null);
	}
		
	@Test
	public void whenSingleMessageAndDispatchReturnsOKShouldDeleteMessage() throws URISyntaxException, InterruptedException {
		Message message = mock(Message.class);
		when(this.repository.getNext()).thenReturn(message).thenReturn(null);
		when(this.dispatcher.sendMessage(this.forwardService, message)).thenReturn(Result.OK);
		
		this.forwardService.onHandleIntent(null);

        InOrder inorder = inOrder(this.thread, this.dispatcher, this.repository);

        inorder.verify(this.thread).sleep(ForwardService.SLEEP_MILLISECONDS);
        inorder.verify(this.dispatcher).sendMessage(this.forwardService, message);
        inorder.verify(this.repository).delete(message);

        verify(this.thread, never()).sleep(ForwardService.TEMPORARY_ERROR_MILLISECONDS);
	}

    @Test
    public void whenSingleMessageShouldAddMessageIdToHeaders() throws URISyntaxException, InterruptedException {
        Message message = mock(Message.class);
        when(message.getId()).thenReturn(42L);
        when(this.repository.getNext()).thenReturn(message).thenReturn(null);
        when(this.dispatcher.sendMessage(this.forwardService, message)).thenReturn(Result.OK);
        ArgumentCaptor<Header> header = ArgumentCaptor.forClass(Header.class);

        this.forwardService.onHandleIntent(null);

        verify(message).addHeader(header.capture());
        assertEquals("42", header.getValue().getValue());
        assertEquals("reyna-id", header.getValue().getKey());
    }

	@Test
	public void whenTwoMessagesAndDispatchReturnsOKShouldDeleteMessages() throws URISyntaxException, InterruptedException {
		Message message1 = mock(Message.class);
		Message message2 = mock(Message.class);
		when(this.repository.getNext())
			.thenReturn(message1)
			.thenReturn(message2)
			.thenReturn(null);
		
		when(this.dispatcher.sendMessage(this.forwardService, message1)).thenReturn(Result.OK);
		when(this.dispatcher.sendMessage(this.forwardService, message2)).thenReturn(Result.OK);
		
		this.forwardService.onHandleIntent(null);
		InOrder inorder = inOrder(this.thread, this.dispatcher, this.repository);

        inorder.verify(this.thread).sleep(ForwardService.SLEEP_MILLISECONDS);
		inorder.verify(this.dispatcher).sendMessage(this.forwardService, message1);
		inorder.verify(this.repository).delete(message1);
        inorder.verify(this.thread).sleep(ForwardService.SLEEP_MILLISECONDS);
		inorder.verify(this.dispatcher).sendMessage(this.forwardService, message2);
		inorder.verify(this.repository).delete(message2);

        verify(this.thread, never()).sleep(ForwardService.TEMPORARY_ERROR_MILLISECONDS);
	}
	
	@Test
	public void whenSingleMessageAndDispatchReturnsTemporaryErrorShouldNotDeleteMessage() throws URISyntaxException, InterruptedException {
		Message message = mock(Message.class);
		when(this.repository.getNext()).thenReturn(message).thenReturn(null);
		when(this.dispatcher.sendMessage(this.forwardService, message)).thenReturn(Result.TEMPORARY_ERROR);
		
		this.forwardService.onHandleIntent(null);
		verify(this.dispatcher).sendMessage(this.forwardService, message);
		verify(this.repository, never()).delete(message);

        verify(this.thread).sleep(ForwardService.TEMPORARY_ERROR_MILLISECONDS);
    }

    @Test
    public void whenSingleMessageAndDispatchReturnsBlackoutShouldNotDeleteMessage() throws URISyntaxException, InterruptedException {
        Message message = mock(Message.class);
        when(this.repository.getNext()).thenReturn(message).thenReturn(null);
        when(this.dispatcher.sendMessage(this.forwardService, message)).thenReturn(Result.BLACKOUT);

        this.forwardService.onHandleIntent(null);
        verify(this.dispatcher).sendMessage(this.forwardService, message);
        verify(this.repository, never()).delete(message);

        verify(this.thread, never()).sleep(ForwardService.TEMPORARY_ERROR_MILLISECONDS);
    }

    @Test
    public void whenSingleMessageAndDispatchReturnsNotConnectedShouldNotDeleteMessage() throws URISyntaxException, InterruptedException {
        Message message = mock(Message.class);
        when(this.repository.getNext()).thenReturn(message).thenReturn(null);
        when(this.dispatcher.sendMessage(this.forwardService, message)).thenReturn(Result.NOTCONNECTED);

        this.forwardService.onHandleIntent(null);
        verify(this.dispatcher).sendMessage(this.forwardService, message);
        verify(this.repository, never()).delete(message);

        verify(this.thread, never()).sleep(ForwardService.TEMPORARY_ERROR_MILLISECONDS);
    }

	@Test
	public void whenTwoMessagesAndFirstDispatchReturnsTemporaryErrorShouldNotDeleteMessages() throws URISyntaxException, InterruptedException {
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

        verify(this.thread).sleep(ForwardService.TEMPORARY_ERROR_MILLISECONDS);
    }

    @Test
    public void whenTwoMessagesAndFirstDispatchReturnsBlackoutShouldNotDeleteMessages() throws URISyntaxException, InterruptedException {
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

        verify(this.thread, never()).sleep(ForwardService.TEMPORARY_ERROR_MILLISECONDS);
    }

	@Test
	public void whenTwoMessagesAndFirstDispatchReturnsPermanentErrorShouldDeleteMessages() throws URISyntaxException, InterruptedException {
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

        verify(this.thread, never()).sleep(ForwardService.TEMPORARY_ERROR_MILLISECONDS);
    }
}
