package com.b2msolutions.reyna.messageProvider;

import com.b2msolutions.reyna.system.Header;
import com.b2msolutions.reyna.system.Message;
import com.b2msolutions.reyna.Repository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.net.URISyntaxException;

import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class MessageProviderTest {

    private MessageProvider messageProvider;

    @Mock
    Repository repository;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        this.messageProvider = new MessageProvider(this.repository);
    }

    @Test
    public void testConstruction() {
        this.messageProvider = new MessageProvider(this.repository);

        assertNotNull(this.messageProvider);
        assertNotNull(this.messageProvider.repository);
    }

    @Test
    public void whenCallingCloseShouldCloseRepository() throws URISyntaxException {
        this.messageProvider.close();

        verify(this.repository).close();
    }

    @Test
    public void whenCallingDeleteShouldDeleteFromRepository() throws URISyntaxException {
        Message message = mock(Message.class);
        when(message.getId()).thenReturn(42L);

        this.messageProvider.delete(message);

        verify(this.repository).delete(message);
    }

    @Test
    public void whenCallingGetNextAndNoMessageShouldReturnNull() throws URISyntaxException {
        when(this.messageProvider.getNext()).thenReturn(null);

        Message actual = this.messageProvider.getNext();

        assertNull(actual);
        verify(this.repository).getNext();
    }

    @Test
    public void whenCallingGetNextAndMessageAvailableShouldReturnMessageAndAddId() throws URISyntaxException {
        Message message = mock(Message.class);
        when(message.getId()).thenReturn(42L);
        when(this.messageProvider.getNext()).thenReturn(message);
        ArgumentCaptor<Header> header = ArgumentCaptor.forClass(Header.class);

        Message actual = this.messageProvider.getNext();

        assertNotNull(actual);
        verify(this.repository).getNext();
        verify(message).addHeader(header.capture());
        assertEquals("42", header.getValue().getValue());
        assertEquals("reyna-id", header.getValue().getKey());
    }

    @Test
    public void whenShouldSendShouldReturnTrue() throws URISyntaxException {
        boolean actual = this.messageProvider.canSend();

        assertTrue(actual);
    }
}
