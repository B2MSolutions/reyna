package com.b2msolutions.reyna.messageProvider;

import android.app.AlarmManager;
import android.content.Context;
import android.os.SystemClock;
import com.b2msolutions.reyna.*;
import com.b2msolutions.reyna.system.Header;
import com.b2msolutions.reyna.system.Message;
import com.b2msolutions.reyna.system.PeriodicBackoutCheck;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.RobolectricTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;
import com.google.gson.Gson;
import org.mockito.Spy;

@RunWith(RobolectricTestRunner.class)
public class BatchProviderTest {

    private BatchProvider messageProvider;

    @Mock
    Repository repository;

    @Mock
    BatchConfiguration batchConfiguration;

    @Mock
    PeriodicBackoutCheck periodicBackoutCheck;

    private Context context;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        this.context = Robolectric.getShadowApplication().getApplicationContext();
        this.messageProvider = new BatchProvider(this.context, this.repository);
        this.messageProvider.batchConfiguration = this.batchConfiguration;
        this.messageProvider.periodicBackoutCheck = this.periodicBackoutCheck;

        doReturn(3).when(this.batchConfiguration).getBatchMessageCount();
        doReturn(1000L).when(this.batchConfiguration).getBatchMessagesSize();
        doReturn(URI.create("www.post.com/api/batch")).when(this.batchConfiguration).getBatchUrl();
        doReturn(AlarmManager.INTERVAL_DAY).when(this.batchConfiguration).getSubmitInterval();
    }

    @Test
    public void testConstruction() {
        this.messageProvider = new BatchProvider(this.context, this.repository);

        assertNotNull(this.messageProvider);
        assertNotNull(this.messageProvider.repository);
        assertNotNull(this.messageProvider.batchConfiguration);
        assertNotNull(this.messageProvider.periodicBackoutCheck);
    }

    @Test
    public void whenCallingCloseShouldCloseRepository() throws URISyntaxException {
        this.messageProvider.close();

        verify(this.repository).close();
    }

    @Test
    public void whenCallingCloseAndNeverSendSuccessfulBatchShouldNotRecord() throws URISyntaxException {
        this.messageProvider.close();

        verify(this.repository).close();
        verify(this.periodicBackoutCheck,never()).record("BatchProvider");
    }

    @Test
    public void whenCallingCloseAndSuccessfullySentBatchShouldRecord() throws URISyntaxException {
        Message message = mock(Message.class);
        when(message.getId()).thenReturn(42L);

        this.messageProvider.delete(message);
        this.messageProvider.close();

        verify(this.repository).close();
        verify(this.periodicBackoutCheck).record("BatchProvider");
    }

    @Test
    public void whenCallingDeleteShouldDeleteFromRepository() throws URISyntaxException {
        Message message = mock(Message.class);
        when(message.getId()).thenReturn(42L);

        this.messageProvider.delete(message);

        verify(this.repository).deleteMessagesFrom(message.getId());
    }

    @Test
    public void whenCallingGetNextAndNoMessageShouldReturnNull() throws URISyntaxException {
        Message actual = this.messageProvider.getNext();

        assertNull(actual);
    }

    @Test
    public void whenCallingGetNextShouldReturnCorrectFormat() throws URISyntaxException {
        ArrayList<Message> messages = this.getTestMessages();
        when(this.repository.getNext()).thenReturn(messages.get(0));
        when(this.repository.getNextMessageAfter(1L)).thenReturn(messages.get(1));
        when(this.repository.getNextMessageAfter(2L)).thenReturn(messages.get(2));
        when(this.repository.getNextMessageAfter(3L)).thenReturn(null);

        Message actual = this.messageProvider.getNext();

        assertNotNull(actual);

        assertEquals("www.post.com/api/batch", actual.getUrl());
        assertEquals("{\"events\":[" +
                "{\"url\":\"http://google.com\",\"reynaId\":1,\"payload\":{\"key01\":\"value01\",\"key02\":11}}," +
                "{\"url\":\"http://google2.com\",\"reynaId\":2,\"payload\":{\"key11\":\"value11\",\"key12\":12}}," +
                "{\"url\":\"http://google3.com\",\"reynaId\":3,\"payload\":{\"key21\":\"value21\",\"key22\":22}}" +
                "]}", actual.getBody());

        this.assertHeaders(actual);
    }

    @Test
    public void whenCallingGetNextAndThereIsCorruptedMessageShouldPostIt() throws URISyntaxException {
        Message message = new Message(2L, URI.create("http://google2.com"), "{\"key11\":", getTestMessageHeaders());

        ArrayList<Message> messages = this.getTestMessages();
        messages.remove(1);
        messages.add(1,message);

        when(this.repository.getNext()).thenReturn(messages.get(0));
        when(this.repository.getNextMessageAfter(1L)).thenReturn(messages.get(1));
        when(this.repository.getNextMessageAfter(2L)).thenReturn(messages.get(2));
        when(this.repository.getNextMessageAfter(3L)).thenReturn(null);

        Message actual = this.messageProvider.getNext();

        assertNotNull(actual);

        assertEquals("www.post.com/api/batch", actual.getUrl());
        assertEquals("{\"events\":[" +
                "{\"url\":\"http://google.com\",\"reynaId\":1,\"payload\":{\"key01\":\"value01\",\"key02\":11}}," +
                "{\"url\":\"http://google2.com\",\"reynaId\":2,\"payload\":{\"key11\":}," +
                "{\"url\":\"http://google3.com\",\"reynaId\":3,\"payload\":{\"key21\":\"value21\",\"key22\":22}}" +
                "]}", actual.getBody());

        assertEquals(3L, actual.getId().longValue());

        this.assertHeaders(actual);
    }

    @Test
    public void whenCallingGetNextShouldReturnMessagesRelatedToMaximumLimit() throws URISyntaxException {
        doReturn(2).when(this.batchConfiguration).getBatchMessageCount();
        ArrayList<Message> messages = this.getTestMessages();

        when(this.repository.getNext()).thenReturn(messages.get(0));
        when(this.repository.getNextMessageAfter(1L)).thenReturn(messages.get(1));
        when(this.repository.getNextMessageAfter(2L)).thenReturn(messages.get(2));
        when(this.repository.getNextMessageAfter(3L)).thenReturn(null);

        Message actual = this.messageProvider.getNext();

        assertNotNull(actual);

        assertEquals("www.post.com/api/batch", actual.getUrl());
        assertEquals("{\"events\":[" +
                "{\"url\":\"http://google.com\",\"reynaId\":1,\"payload\":{\"key01\":\"value01\",\"key02\":11}}," +
                "{\"url\":\"http://google2.com\",\"reynaId\":2,\"payload\":{\"key11\":\"value11\",\"key12\":12}}" +
                "]}", actual.getBody());

        this.assertHeaders(actual);
    }

    @Test
    public void whenCallingGetNextAndThereIsStringMessageShouldPostIt() throws URISyntaxException {
        Message message = new Message(2L, URI.create("http://google2.com"), "Message body", getTestMessageHeaders());

        ArrayList<Message> messages = this.getTestMessages();
        messages.remove(1);
        messages.add(1,message);

        when(this.repository.getNext()).thenReturn(messages.get(0));
        when(this.repository.getNextMessageAfter(1L)).thenReturn(messages.get(1));
        when(this.repository.getNextMessageAfter(2L)).thenReturn(messages.get(2));
        when(this.repository.getNextMessageAfter(3L)).thenReturn(null);

        Message actual = this.messageProvider.getNext();

        assertNotNull(actual);

        assertEquals("www.post.com/api/batch", actual.getUrl());
        assertEquals("{\"events\":[" +
                "{\"url\":\"http://google.com\",\"reynaId\":1,\"payload\":{\"key01\":\"value01\",\"key02\":11}}," +
                "{\"url\":\"http://google2.com\",\"reynaId\":2,\"payload\":Message body}," +
                "{\"url\":\"http://google3.com\",\"reynaId\":3,\"payload\":{\"key21\":\"value21\",\"key22\":22}}" +
                "]}", actual.getBody());

        this.assertHeaders(actual);
    }

    @Test
    public void whenCallingGetNextShouldReturnMessagesRelatedToMaximumSize() throws URISyntaxException {
        doReturn(95L).when(this.batchConfiguration).getBatchMessagesSize();
        ArrayList<Message> messages = this.getTestMessages();

        when(this.repository.getNext()).thenReturn(messages.get(0));
        when(this.repository.getNextMessageAfter(1L)).thenReturn(messages.get(1));
        when(this.repository.getNextMessageAfter(2L)).thenReturn(messages.get(2));
        when(this.repository.getNextMessageAfter(3L)).thenReturn(null);

        Message actual = this.messageProvider.getNext();

        assertNotNull(actual);
        assertEquals("www.post.com/api/batch", actual.getUrl());
        assertEquals("{\"events\":[" +
                "{\"url\":\"http://google.com\",\"reynaId\":1,\"payload\":{\"key01\":\"value01\",\"key02\":11}}" +
                "]}", actual.getBody());

        this.assertHeaders(actual);
    }

    @Test
    public void whenCallingGetNextAndNoUrlConfiguredShouldReturnUrlWithBatchAppended() throws URISyntaxException {
        doReturn(95L).when(this.batchConfiguration).getBatchMessagesSize();
        doReturn(null).when(this.batchConfiguration).getBatchUrl();

        Message message1 = new Message(1L, URI.create("http://www.post.com"), "{\"key01\":\"value01\",\"key02\":11}", getTestMessageHeaders());
        Message message2 = new Message(2L, URI.create("http://www.post.com"), "{\"key11\":\"value11\",\"key12\":12}", getTestMessageHeaders());
        Message message3 = new Message(3L, URI.create("http://www.post.com"), "{\"key21\":\"value21\",\"key22\":22}", getTestMessageHeaders());

        when(this.repository.getNext()).thenReturn(message1);
        when(this.repository.getNextMessageAfter(1L)).thenReturn(message2);
        when(this.repository.getNextMessageAfter(2L)).thenReturn(message3);
        when(this.repository.getNextMessageAfter(3L)).thenReturn(null);

        Message actual = this.messageProvider.getNext();

        assertNotNull(actual);

        assertEquals("http://www.post.com/api/1/batch", actual.getUrl());
        assertEquals("{\"events\":[" +
                "{\"url\":\"http://www.post.com\",\"reynaId\":1,\"payload\":{\"key01\":\"value01\",\"key02\":11}}" +
                "]}", actual.getBody());
    }

    @Test
    public void testingDifferentMessagesFormat() throws URISyntaxException {
        doReturn(10).when(this.batchConfiguration).getBatchMessageCount();
        doReturn(null).when(this.batchConfiguration).getBatchUrl();

        Message message1 = new Message(1L, URI.create("http://www.post.com"), "{\"buckets\":[{\"total\":1129.0,\"hour\":\"2016041105\"},{\"total\":601.0,\"hour\":\"2016041107\"}],\"utc\":1460373149543}", getTestMessageHeaders());
        Message message2 = new Message(2L, URI.create("http://www.post.com"), "{\"coreBytes\":246270,\"mobileBytes\":2217672,\"totalBytes\":2217672,\"utc\":1460356950584}", getTestMessageHeaders());
        Message message3 = new Message(3L, URI.create("http://www.post.com"), "{\"utc\":1460241205631}", getTestMessageHeaders());
        Message message4 = new Message(4L, URI.create("http://www.post.com"), "{\"source\":\"source\",\"level\":92,\"utc\":1460360349303}", getTestMessageHeaders());

        when(this.repository.getNext()).thenReturn(message1);
        when(this.repository.getNextMessageAfter(1L)).thenReturn(message2);
        when(this.repository.getNextMessageAfter(2L)).thenReturn(message3);
        when(this.repository.getNextMessageAfter(3L)).thenReturn(message4);
        when(this.repository.getNextMessageAfter(4L)).thenReturn(null);

        Message actual = this.messageProvider.getNext();

        assertNotNull(actual);

        assertEquals(4L, actual.getId().longValue());
        assertEquals("http://www.post.com/api/1/batch", actual.getUrl());
        assertEquals("{\"events\":[" +
                "{\"url\":\"http://www.post.com\",\"reynaId\":1,\"payload\":{\"buckets\":[{\"total\":1129.0,\"hour\":\"2016041105\"},{\"total\":601.0,\"hour\":\"2016041107\"}],\"utc\":1460373149543}}," +
                "{\"url\":\"http://www.post.com\",\"reynaId\":2,\"payload\":{\"coreBytes\":246270,\"mobileBytes\":2217672,\"totalBytes\":2217672,\"utc\":1460356950584}}," +
                "{\"url\":\"http://www.post.com\",\"reynaId\":3,\"payload\":{\"utc\":1460241205631}}," +
                "{\"url\":\"http://www.post.com\",\"reynaId\":4,\"payload\":{\"source\":\"source\",\"level\":92,\"utc\":1460360349303}}" +
                "]}", actual.getBody());
    }

    @Test
    public void whenCallingGetNextAndNoUrlConfiguredAndHTTPSShouldReturnUrlWithBatchAppended() throws URISyntaxException {
        doReturn(95L).when(this.batchConfiguration).getBatchMessagesSize();
        doReturn(null).when(this.batchConfiguration).getBatchUrl();

        Message message1 = new Message(1L, URI.create("https://www.post.com/1/2/req"), "{\"key01\":\"value01\", \"key02\": 11}", getTestMessageHeaders());
        Message message2 = new Message(2L, URI.create("https://www.post.com/1/2/req"), "{\"key11\":\"value11\", \"key12\": 12}", getTestMessageHeaders());
        Message message3 = new Message(3L, URI.create("https://www.post.com/1/2/req"), "{\"key21\":\"value21\", \"key22\": 22}", getTestMessageHeaders());

        when(this.repository.getNext()).thenReturn(message1).thenReturn(message2).thenReturn(message3).thenReturn(null);

        Message actual = this.messageProvider.getNext();

        assertNotNull(actual);

        assertEquals("https://www.post.com/api/1/batch", actual.getUrl());
    }

    @Test
    public void whenCallingCanSendAndTimeNotElapsedShouldReturnFalse() throws URISyntaxException {
        long interval = (long)(AlarmManager.INTERVAL_DAY * 0.9);
        doReturn(false).when(this.periodicBackoutCheck).timeElapsed("BatchProvider", interval);

        boolean actual = this.messageProvider.canSend();

        assertFalse(actual);
    }

    @Test
    public void whenCallingCanSendAndTimeElapsedShouldReturnTrue() throws URISyntaxException {
        long interval = (long)(AlarmManager.INTERVAL_DAY * 0.9);
        doReturn(true).when(this.periodicBackoutCheck).timeElapsed("BatchProvider", interval);

        boolean actual = this.messageProvider.canSend();

        assertTrue(actual);
    }

    @Test
    public void whenCallingCanSendThereAreMoreMessagesThanMaxMessagesCountShouldSend() throws URISyntaxException {
        doReturn(false).when(this.periodicBackoutCheck).timeElapsed("BatchProvider", AlarmManager.INTERVAL_DAY);
        doReturn(100).when(this.batchConfiguration).getBatchMessageCount();
        doReturn(100L).when(this.repository).getAvailableMessagesCount();

        boolean actual = this.messageProvider.canSend();

        assertTrue(actual);
    }

    @Test
    public void whenCallingCanSendThereAreLessMessagesThanMaxMessagesCountShouldSend() throws URISyntaxException {
        doReturn(false).when(this.periodicBackoutCheck).timeElapsed("BatchProvider", AlarmManager.INTERVAL_DAY);
        doReturn(100).when(this.batchConfiguration).getBatchMessageCount();
        doReturn(99L).when(this.repository).getAvailableMessagesCount();

        boolean actual = this.messageProvider.canSend();

        assertFalse(actual);
    }

    @Test
    public void whenCallingGetNextAndPreviousBatchWasLessThanMaximumessagesCountShouldReturnNull() throws URISyntaxException {
        doReturn(2).when(this.batchConfiguration).getBatchMessageCount();
        ArrayList<Message> messages = this.getTestMessages();
        when(this.repository.getNext()).thenReturn(messages.get(0));
        when(this.repository.getNextMessageAfter(1L)).thenReturn(messages.get(1));
        when(this.repository.getNextMessageAfter(2L)).thenReturn(messages.get(2));
        when(this.repository.getNextMessageAfter(3L)).thenReturn(null);

        Message actual = this.messageProvider.getNext();

        assertNotNull(actual);

        assertEquals("www.post.com/api/batch", actual.getUrl());
        assertEquals("{\"events\":[" +
                "{\"url\":\"http://google.com\",\"reynaId\":1,\"payload\":{\"key01\":\"value01\",\"key02\":11}}," +
                "{\"url\":\"http://google2.com\",\"reynaId\":2,\"payload\":{\"key11\":\"value11\",\"key12\":12}}" +
                "]}", actual.getBody());

        when(this.repository.getNext()).thenReturn(messages.get(2));
        when(this.repository.getNextMessageAfter(3L)).thenReturn(null);

        actual = this.messageProvider.getNext();
        assertNotNull(actual);

        assertEquals("www.post.com/api/batch", actual.getUrl());
        assertEquals("{\"events\":[" +
                "{\"url\":\"http://google3.com\",\"reynaId\":3,\"payload\":{\"key21\":\"value21\",\"key22\":22}}" +
                "]}", actual.getBody());

        when(this.repository.getNext()).thenReturn(messages.get(0));
        when(this.repository.getNextMessageAfter(1L)).thenReturn(messages.get(1));
        when(this.repository.getNextMessageAfter(2L)).thenReturn(messages.get(2));
        when(this.repository.getNextMessageAfter(3L)).thenReturn(null);
        actual = this.messageProvider.getNext();

        assertNull(actual);
    }

    @Test
    public void whenCallingGetNextAndPreviousBatchWasLessThanMaximumMessagesButSizeIsReachedShouldReturnBatch() throws URISyntaxException {
        doReturn(95L).when(this.batchConfiguration).getBatchMessagesSize();
        doReturn(3).when(this.batchConfiguration).getBatchMessageCount();
        ArrayList<Message> messages = this.getTestMessages();
        when(this.repository.getNext()).thenReturn(messages.get(0));
        when(this.repository.getNextMessageAfter(1L)).thenReturn(messages.get(1));
        when(this.repository.getNextMessageAfter(2L)).thenReturn(messages.get(2));
        when(this.repository.getNextMessageAfter(3L)).thenReturn(null);

        /*batch due to size limit*/
        Message actual = this.messageProvider.getNext();
        assertNotNull(actual);
        assertEquals("www.post.com/api/batch", actual.getUrl());
        assertEquals("{\"events\":[" +
                "{\"url\":\"http://google.com\",\"reynaId\":1,\"payload\":{\"key01\":\"value01\",\"key02\":11}}" +
                "]}", actual.getBody());

        /*batch due to number of max messages limit*/
        doReturn(10000L).when(this.batchConfiguration).getBatchMessagesSize();
        doReturn(2).when(this.batchConfiguration).getBatchMessageCount();
        when(this.repository.getNext()).thenReturn(messages.get(0));
        when(this.repository.getNextMessageAfter(1L)).thenReturn(messages.get(1));
        when(this.repository.getNextMessageAfter(2L)).thenReturn(messages.get(2));
        when(this.repository.getNextMessageAfter(3L)).thenReturn(null);
        actual = this.messageProvider.getNext();
        assertNotNull(actual);

        assertEquals("{\"events\":[" +
                "{\"url\":\"http://google.com\",\"reynaId\":1,\"payload\":{\"key01\":\"value01\",\"key02\":11}}," +
                "{\"url\":\"http://google2.com\",\"reynaId\":2,\"payload\":{\"key11\":\"value11\",\"key12\":12}}" +
                "]}", actual.getBody());


        /*batch include rest of the messages*/
        when(this.repository.getNext()).thenReturn(messages.get(2));
        when(this.repository.getNextMessageAfter(3L)).thenReturn(null);
        actual = this.messageProvider.getNext();
        assertNotNull(actual);
        assertEquals("www.post.com/api/batch", actual.getUrl());
        assertEquals("{\"events\":[" +
                "{\"url\":\"http://google3.com\",\"reynaId\":3,\"payload\":{\"key21\":\"value21\",\"key22\":22}}" +
                "]}", actual.getBody());

        /*should return null as last batch has only 1 message*/
        when(this.repository.getNext()).thenReturn(messages.get(0));
        when(this.repository.getNextMessageAfter(1L)).thenReturn(messages.get(1));
        when(this.repository.getNextMessageAfter(2L)).thenReturn(messages.get(2));
        when(this.repository.getNextMessageAfter(3L)).thenReturn(null);
        actual = this.messageProvider.getNext();

        assertNull(actual);
    }

    private ArrayList<Message> getTestMessages() {
        Message message1 = new Message(1L, URI.create("http://google.com"), "{\"key01\":\"value01\",\"key02\":11}", getTestMessageHeaders());
        Message message2 = new Message(2L, URI.create("http://google2.com"), "{\"key11\":\"value11\",\"key12\":12}", getTestMessageHeaders());
        Message message3 = new Message(3L, URI.create("http://google3.com"), "{\"key21\":\"value21\",\"key22\":22}", getTestMessageHeaders());

        ArrayList<Message> messages = new ArrayList<Message>(3);
        messages.add(message1);
        messages.add(message2);
        messages.add(message3);

        return messages;
    }

    private void assertHeaders(Message actual) {
        assertEquals(3, actual.getHeaders().length);
        assertEquals("key1", actual.getHeaders()[0].getKey());
        assertEquals("value1", actual.getHeaders()[0].getValue());
        assertEquals("key2", actual.getHeaders()[1].getKey());
        assertEquals("value2", actual.getHeaders()[1].getValue());
        assertEquals("key4", actual.getHeaders()[2].getKey());
        assertEquals("value4", actual.getHeaders()[2].getValue());
    }

    private Header[] getTestMessageHeaders() {
        ArrayList<Header> headers = new ArrayList<Header>();
        headers.add(new Header("key1", "value1"));
        headers.add(new Header("key2", "value2"));
        headers.add(new Header("key4", "value4"));

        Header[] headersForMessage = new Header[headers.size()];
        return headers.toArray(headersForMessage);
    }
}
