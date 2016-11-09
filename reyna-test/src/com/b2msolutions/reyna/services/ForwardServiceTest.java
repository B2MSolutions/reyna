package com.b2msolutions.reyna.services;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.b2msolutions.reyna.*;
import com.b2msolutions.reyna.Dispatcher.Result;
import com.b2msolutions.reyna.system.Message;
import com.b2msolutions.reyna.system.PeriodicBackoutCheck;
import com.b2msolutions.reyna.system.Preferences;
import com.b2msolutions.reyna.system.Thread;
import com.b2msolutions.reyna.messageProvider.BatchProvider;
import com.b2msolutions.reyna.messageProvider.IMessageProvider;
import com.b2msolutions.reyna.messageProvider.MessageProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowConnectivityManager;

import java.io.IOException;
import java.net.URISyntaxException;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;
import static org.robolectric.Shadows.shadowOf;
import static test.Assert.assertServiceStarted;

@Config(sdk = 18)
@RunWith(RobolectricTestRunner.class)
public class ForwardServiceTest {

    private ForwardService forwardService;

    @Mock Dispatcher dispatcher;

    @Mock
    Repository repository;

    @Mock Thread thread;

    @Mock
    NetworkInfo networkInfo;

    @Mock
    PeriodicBackoutCheck periodicBackoutCheck;

    private Context context;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        this.context = RuntimeEnvironment.application.getApplicationContext();
        this.forwardService = Robolectric.setupService(ForwardService.class);
        this.forwardService.dispatcher = dispatcher;
        this.forwardService.repository = repository;
        this.forwardService.thread = thread;
        this.forwardService.periodicBackoutCheck = this.periodicBackoutCheck;

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        ShadowConnectivityManager shadowConnectivityManager = shadowOf(connectivityManager);
        shadowConnectivityManager.setActiveNetworkInfo(networkInfo);
        when(networkInfo.getType()).thenReturn(ConnectivityManager.TYPE_WIFI);
        when(networkInfo.isConnectedOrConnecting()).thenReturn(true);

        doReturn(true).when(this.periodicBackoutCheck).timeElapsed("ForwardService_Backout_Temporary_Error", 300000);
    }

    @Test
    public void sleepTimeoutShouldBeCorrect() {
      assertEquals(1000, ForwardService.SLEEP_MILLISECONDS);
    }

    @Test
    public void temporaryErrorTimeoutShouldBeCorrect() {
        assertEquals(300000, ForwardService.TEMPORARY_ERROR_MILLISECONDS);
    }

    @Test
    public void testConstruction() {
        this.forwardService = new ForwardService();

        assertNotNull(this.forwardService);
        assertNotNull(this.forwardService.periodicBackoutCheck);
        assertNotNull(this.forwardService.repository);
    }

    @Test
    public void whenBatchModeEnabledMessageProviderShouldBeBatchProvider() {
        new Preferences(this.forwardService).saveBatchUpload(true);
        IMessageProvider messageProvider = this.forwardService.getMessageProvider();

        assertNotNull(messageProvider);
        assertEquals(BatchProvider.class, messageProvider.getClass());
    }

    @Test
    public void whenBatchModeDisabledMessageProviderShouldBeMessageProvider() {
        new Preferences(this.forwardService).saveBatchUpload(false);
        IMessageProvider messageProvider = this.forwardService.getMessageProvider();

        assertNotNull(messageProvider);
        assertEquals(MessageProvider.class, messageProvider.getClass());
    }

    @Test
    public void whenNotNullIntentShouldNotThrow() {
        this.forwardService.onHandleIntent(null);
    }

    @Test
    public void whenThereAreNoMessagesShouldNotThrow() {
        this.forwardService.onHandleIntent(null);
    }

    @Test
    public void whenThereAreNoMessagesShouldNotSleep() throws InterruptedException {
        this.forwardService.onHandleIntent(new Intent());
        verify(this.thread, never()).sleep(anyLong());
    }

    @Test
    public void whenMoveNextThrowsShouldNotThrow() throws URISyntaxException {
        when(this.repository.getNext()).thenThrow(new URISyntaxException("", ""));
        this.forwardService.onHandleIntent(new Intent());
    }

    @Test
    public void whenSingleMessageAndDispatchReturnsOKShouldDeleteMessage() throws URISyntaxException, InterruptedException {
        Message message = mock(Message.class);
        when(this.repository.getNext()).thenReturn(message).thenReturn(null);
        when(this.dispatcher.sendMessage(this.forwardService, message)).thenReturn(Result.OK);

        this.forwardService.onHandleIntent(new Intent());

        InOrder inorder = inOrder(this.thread, this.dispatcher, this.repository);

        inorder.verify(this.thread).sleep(ForwardService.SLEEP_MILLISECONDS);
        inorder.verify(this.dispatcher).sendMessage(this.forwardService, message);
        inorder.verify(this.repository).delete(message);

        verify(this.periodicBackoutCheck, never()).record("ForwardService_Backout_Temporary_Error");
    }

    @Test
    public void whenCallingOnHandleIntentAndMessageProviderThrowsShouldNotThrow() throws URISyntaxException, InterruptedException {
        doThrow(IOException.class).when(this.repository).getNext();

        this.forwardService.onHandleIntent(new Intent());

        verify(this.repository).close();
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

        this.forwardService.onHandleIntent(new Intent());
        InOrder inorder = inOrder(this.thread, this.dispatcher, this.repository);

        inorder.verify(this.thread).sleep(ForwardService.SLEEP_MILLISECONDS);
        inorder.verify(this.dispatcher).sendMessage(this.forwardService, message1);
        inorder.verify(this.repository).delete(message1);
        inorder.verify(this.thread).sleep(ForwardService.SLEEP_MILLISECONDS);
        inorder.verify(this.dispatcher).sendMessage(this.forwardService, message2);
        inorder.verify(this.repository).delete(message2);

        verify(this.periodicBackoutCheck, never()).record("ForwardService_Backout_Temporary_Error");
    }

    @Test
    public void whenSingleMessageAndDispatchReturnsTemporaryErrorShouldNotDeleteMessage() throws URISyntaxException, InterruptedException {
        Message message = mock(Message.class);
        when(this.repository.getNext()).thenReturn(message).thenReturn(null);
        when(this.dispatcher.sendMessage(this.forwardService, message)).thenReturn(Result.TEMPORARY_ERROR);

        this.forwardService.onHandleIntent(new Intent());
        verify(this.dispatcher).sendMessage(this.forwardService, message);
        verify(this.repository, never()).delete(message);
        verify(this.repository, never()).deleteMessagesFrom(anyLong());

        verify(this.periodicBackoutCheck).record("ForwardService_Backout_Temporary_Error");
    }

    @Test
    public void whenSingleMessageAndDispatchReturnsBlackoutShouldNotDeleteMessage() throws URISyntaxException, InterruptedException {
        Message message = mock(Message.class);
        when(this.repository.getNext()).thenReturn(message).thenReturn(null);
        when(this.dispatcher.sendMessage(this.forwardService, message)).thenReturn(Result.BLACKOUT);

        this.forwardService.onHandleIntent(new Intent());
        verify(this.dispatcher).sendMessage(this.forwardService, message);
        verify(this.repository, never()).delete(message);
        verify(this.repository, never()).deleteMessagesFrom(anyLong());

        verify(this.periodicBackoutCheck, never()).record("ForwardService_Backout_Temporary_Error");
    }

    @Test
    public void whenSingleMessageAndDispatchReturnsNotConnectedShouldNotDeleteMessage() throws URISyntaxException, InterruptedException {
        Message message = mock(Message.class);
        when(this.repository.getNext()).thenReturn(message).thenReturn(null);
        when(this.dispatcher.sendMessage(this.forwardService, message)).thenReturn(Result.NOTCONNECTED);

        this.forwardService.onHandleIntent(new Intent());
        verify(this.dispatcher).sendMessage(this.forwardService, message);
        verify(this.repository, never()).delete(message);
        verify(this.repository, never()).deleteMessagesFrom(anyLong());

        verify(this.periodicBackoutCheck, never()).record("ForwardService_Backout_Temporary_Error");
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

        this.forwardService.onHandleIntent(new Intent());
        InOrder inorder = inOrder(this.dispatcher, this.repository);

        inorder.verify(this.dispatcher).sendMessage(this.forwardService, message1);
        inorder.verify(this.repository, never()).delete(message1);
        inorder.verify(this.dispatcher, never()).sendMessage(this.forwardService, message2);
        inorder.verify(this.repository, never()).delete(message2);
        inorder.verify(this.repository, never()).deleteMessagesFrom(anyLong());
        verify(this.periodicBackoutCheck).record("ForwardService_Backout_Temporary_Error");
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

        this.forwardService.onHandleIntent(new Intent());
        InOrder inorder = inOrder(this.dispatcher, this.repository);

        inorder.verify(this.dispatcher).sendMessage(this.forwardService, message1);
        inorder.verify(this.repository, never()).delete(message1);
        inorder.verify(this.dispatcher, never()).sendMessage(this.forwardService, message2);
        inorder.verify(this.repository, never()).delete(message2);
        inorder.verify(this.repository, never()).deleteMessagesFrom(anyLong());

        verify(this.periodicBackoutCheck, never()).record("ForwardService_Backout_Temporary_Error");
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

        this.forwardService.onHandleIntent(new Intent());
        InOrder inorder = inOrder(this.dispatcher, this.repository);

        inorder.verify(this.dispatcher).sendMessage(this.forwardService, message1);
        inorder.verify(this.repository).delete(message1);
        inorder.verify(this.dispatcher).sendMessage(this.forwardService, message2);
        inorder.verify(this.repository).delete(message2);

        verify(this.periodicBackoutCheck, never()).record("ForwardService_Backout_Temporary_Error");
    }

    @Test
    public void whenSendMessageShouldCheckForCanSendFirstNotGetNextMessageWhenCannotSend() throws URISyntaxException, InterruptedException {
        when(this.networkInfo.isConnectedOrConnecting()).thenReturn(false);

        this.forwardService.onHandleIntent(new Intent());
        verify(this.repository, never()).getNext();
    }

    @Test
    public void whenCallingStartShouldStartService() {
        ForwardService.start(this.context);

        assertServiceStarted(ForwardService.class);
    }

    @Test
    public void whenMessageProviderCannotSendShouldDoNothing() throws URISyntaxException, InterruptedException {
        PeriodicBackoutCheck periodicBackoutCheck1 = new PeriodicBackoutCheck(this.context);
        periodicBackoutCheck1.record("BatchProvider");
        new Preferences(this.context).saveBatchUpload(true);

        this.forwardService.onHandleIntent(new Intent());

        verify(this.repository, never()).getNext();
        verify(this.repository, never()).delete(any(Message.class));
        verify(this.repository).close();
        verify(this.periodicBackoutCheck, never()).record("ForwardService_Backout_Temporary_Error");
    }

    @Test
    public void whenPreviousMessagesFailedWithTemporaryErrorShouldNotTryToSendAnyMessageWithinFiveMinutes() throws URISyntaxException, InterruptedException {
        doReturn(false).when(this.periodicBackoutCheck).timeElapsed("ForwardService_Backout_Temporary_Error", 300000);
        this.forwardService.onHandleIntent(new Intent());

        verify(this.repository, never()).getNext();
        verify(this.repository, never()).delete(any(Message.class));
        verify(this.repository).close();
        verify(this.periodicBackoutCheck, never()).record("ForwardService_Backout_Temporary_Error");
    }

    @Test
    public void whenCallingStartShouldAcquirePowerLock() {
        ForwardService.start(RuntimeEnvironment.application.getApplicationContext());
        Intent intent = assertServiceStarted(ForwardService.class);
        int lockId = intent.getIntExtra("android.support.content.wakelockid", -1);
        assertTrue(lockId != -1);
    }
}
