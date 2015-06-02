package com.b2msolutions.reyna.services;

import android.content.Context;
import android.content.Intent;

import com.b2msolutions.reyna.Header;
import com.b2msolutions.reyna.Message;
import com.b2msolutions.reyna.Preferences;
import com.b2msolutions.reyna.Repository;
import com.b2msolutions.reyna.ReynaTestRunner;
import com.b2msolutions.reyna.Thread;
import com.b2msolutions.reyna.http.Result;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;

import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(ReynaTestRunner.class)
public class ForwardServiceTest {

	private ForwardService forwardService;

	@Mock IDispatcherService dispatcherService;

	@Mock Repository repository;

    @Mock Thread thread;

	@Mock Preferences preferences;

    private Context context;

	private final long TEMPORARY_ERROR_MILLISECONDS = 345;

    @Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
        this.context = Robolectric.getShadowApplication().getApplicationContext();
		this.forwardService = Robolectric.setupService(ForwardService.class);
		this.forwardService.mService = dispatcherService;
		this.forwardService.repository = repository;
        this.forwardService.thread = thread;
		this.forwardService.preferences = preferences;

		when(this.preferences.getTemporaryErrorTimeout()).thenReturn(TEMPORARY_ERROR_MILLISECONDS);
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
		when(this.dispatcherService.sendMessage(message)).thenReturn(Result.OK);

		this.forwardService.onHandleIntent(null);

        InOrder inorder = inOrder(this.thread, this.dispatcherService, this.repository);

        inorder.verify(this.thread).sleep(ForwardService.SLEEP_MILLISECONDS);
        inorder.verify(this.dispatcherService).sendMessage(message);
        inorder.verify(this.repository).delete(message);

        verify(this.thread, never()).sleep(TEMPORARY_ERROR_MILLISECONDS);
	}

    @Test
    public void whenSingleMessageShouldAddMessageIdToHeaders() throws URISyntaxException, InterruptedException {
        Message message = mock(Message.class);
        when(message.getId()).thenReturn(42L);
        when(this.repository.getNext()).thenReturn(message).thenReturn(null);
        when(this.dispatcherService.sendMessage(message)).thenReturn(Result.OK);
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

		when(this.dispatcherService.sendMessage(message1)).thenReturn(Result.OK);
		when(this.dispatcherService.sendMessage(message2)).thenReturn(Result.OK);

		this.forwardService.onHandleIntent(null);
		InOrder inorder = inOrder(this.thread, this.dispatcherService, this.repository);

        inorder.verify(this.thread).sleep(ForwardService.SLEEP_MILLISECONDS);
		inorder.verify(this.dispatcherService).sendMessage(message1);
		inorder.verify(this.repository).delete(message1);
        inorder.verify(this.thread).sleep(ForwardService.SLEEP_MILLISECONDS);
		inorder.verify(this.dispatcherService).sendMessage(message2);
		inorder.verify(this.repository).delete(message2);

        verify(this.thread, never()).sleep(TEMPORARY_ERROR_MILLISECONDS);
	}

	@Test
	public void whenSingleMessageAndDispatchReturnsTemporaryErrorShouldNotDeleteMessage() throws URISyntaxException, InterruptedException {
		Message message = mock(Message.class);
		when(this.repository.getNext()).thenReturn(message).thenReturn(null);
		when(this.dispatcherService.sendMessage(message)).thenReturn(Result.TEMPORARY_ERROR);

		this.forwardService.onHandleIntent(null);
		verify(this.dispatcherService).sendMessage(message);
		verify(this.repository, never()).delete(message);

        verify(this.thread).sleep(TEMPORARY_ERROR_MILLISECONDS);
    }

    @Test
    public void whenSingleMessageAndDispatchReturnsBlackoutShouldNotDeleteMessage() throws URISyntaxException, InterruptedException {
        Message message = mock(Message.class);
        when(this.repository.getNext()).thenReturn(message).thenReturn(null);
        when(this.dispatcherService.sendMessage(message)).thenReturn(Result.BLACKOUT);

        this.forwardService.onHandleIntent(null);
        verify(this.dispatcherService).sendMessage(message);
        verify(this.repository, never()).delete(message);

        verify(this.thread, never()).sleep(TEMPORARY_ERROR_MILLISECONDS);
    }

    @Test
    public void whenSingleMessageAndDispatchReturnsNotConnectedShouldNotDeleteMessage() throws URISyntaxException, InterruptedException {
        Message message = mock(Message.class);
        when(this.repository.getNext()).thenReturn(message).thenReturn(null);
        when(this.dispatcherService.sendMessage(message)).thenReturn(Result.NOTCONNECTED);

        this.forwardService.onHandleIntent(null);
        verify(this.dispatcherService).sendMessage(message);
        verify(this.repository, never()).delete(message);

        verify(this.thread, never()).sleep(TEMPORARY_ERROR_MILLISECONDS);
    }

	@Test
	public void whenTwoMessagesAndFirstDispatchReturnsTemporaryErrorShouldNotDeleteMessages() throws URISyntaxException, InterruptedException {
		Message message1 = mock(Message.class);
		Message message2 = mock(Message.class);
		when(this.repository.getNext())
			.thenReturn(message1)
			.thenReturn(message2)
			.thenReturn(null);

		when(this.dispatcherService.sendMessage(message1)).thenReturn(Result.TEMPORARY_ERROR);

		this.forwardService.onHandleIntent(null);
		InOrder inorder = inOrder(this.dispatcherService, this.repository);

		inorder.verify(this.dispatcherService).sendMessage(message1);
		inorder.verify(this.repository, never()).delete(message1);
		inorder.verify(this.dispatcherService, never()).sendMessage(message2);
		inorder.verify(this.repository, never()).delete(message2);

        verify(this.thread).sleep(TEMPORARY_ERROR_MILLISECONDS);
    }

    @Test
    public void whenTwoMessagesAndFirstDispatchReturnsBlackoutShouldNotDeleteMessages() throws URISyntaxException, InterruptedException {
        Message message1 = mock(Message.class);
        Message message2 = mock(Message.class);
        when(this.repository.getNext())
                .thenReturn(message1)
                .thenReturn(message2)
                .thenReturn(null);

        when(this.dispatcherService.sendMessage(message1)).thenReturn(Result.BLACKOUT);

        this.forwardService.onHandleIntent(null);
        InOrder inorder = inOrder(this.dispatcherService, this.repository);

        inorder.verify(this.dispatcherService).sendMessage(message1);
        inorder.verify(this.repository, never()).delete(message1);
        inorder.verify(this.dispatcherService, never()).sendMessage(message2);
        inorder.verify(this.repository, never()).delete(message2);

        verify(this.thread, never()).sleep(TEMPORARY_ERROR_MILLISECONDS);
    }

	@Test
	public void whenTwoMessagesAndFirstDispatchReturnsPermanentErrorShouldDeleteMessages() throws URISyntaxException, InterruptedException {
		Message message1 = mock(Message.class);
		Message message2 = mock(Message.class);
		when(this.repository.getNext())
			.thenReturn(message1)
			.thenReturn(message2)
			.thenReturn(null);

		when(this.dispatcherService.sendMessage(message1)).thenReturn(Result.PERMANENT_ERROR);
		when(this.dispatcherService.sendMessage(message2)).thenReturn(Result.OK);

		this.forwardService.onHandleIntent(null);
		InOrder inorder = inOrder(this.dispatcherService, this.repository);

		inorder.verify(this.dispatcherService).sendMessage(message1);
		inorder.verify(this.repository).delete(message1);
		inorder.verify(this.dispatcherService).sendMessage(message2);
		inorder.verify(this.repository).delete(message2);

        verify(this.thread, never()).sleep(TEMPORARY_ERROR_MILLISECONDS);
    }

    @Test
    public void whenCallingOnHandleIntentShouldBindDispatcherService(){
        this.forwardService.onHandleIntent(new Intent());

        Intent intent = shadowOf(this.forwardService).getNextStartedService();
        assertNotNull(intent);
        assertEquals("com.b2msolutions.reyna.BindDispatcher", intent.getAction());
    }

    @Test
    public void whenCallingOnHandleIntentShouldUnbindDispatcherService(){
        this.forwardService.onHandleIntent(new Intent());

        assertNull(this.forwardService.mService);
    }

    @Test
    public void whenCallingOnServiceConnectedShouldSetDispatcherService(){
        BindableDispatcherService.LocalBinder binder = mock(BindableDispatcherService.LocalBinder.class);
        when(binder.getService()).thenReturn(this.dispatcherService);
        this.forwardService.mService = null;

        this.forwardService.mConnection.onServiceConnected(null, binder);

        assertNotNull(this.forwardService.mService);
        assertEquals(this.dispatcherService, this.forwardService.mService);
    }

    @Test
    public void whenCallingOnServiceConnectedAndBinderIsNullShouldNotThrow(){
        this.forwardService.mConnection.onServiceConnected(null, null);
    }

    @Test
    public void whenCallingOnServiceDisconnectedShouldSetDispatcherServiceToNull(){
        this.forwardService.mConnection.onServiceDisconnected(null);

        assertNull(this.forwardService.mService);
    }
}
