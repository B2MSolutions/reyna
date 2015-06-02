package com.b2msolutions.reyna.services;

import android.content.Intent;

import com.b2msolutions.reyna.Dispatcher;
import com.b2msolutions.reyna.Message;
import com.b2msolutions.reyna.ReynaTestRunner;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;

@RunWith(ReynaTestRunner.class)
public class DispatcherServiceTest {

    @Mock Dispatcher dispatcher;
    @Mock Message message;
    protected DispatcherService dispatcherService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        this.dispatcherService = Robolectric.setupService(DispatcherService.class);
        this.dispatcherService.dispatcher = this.dispatcher;
    }

    @Test
    public void WhenConstructedShouldSetDispatcher(){
        assertNotNull(this.dispatcherService.dispatcher);
    }

    @Test
    public void WhenConstructedShouldSetContext(){
        assertNotNull(this.dispatcherService.context);
        assertEquals(this.dispatcherService, this.dispatcherService.context);
    }

    @Test
    public void WhenCallingSendMessageShouldCallDispatcherSendMessage(){
        this.dispatcherService.sendMessage(this.message);

        verify(this.dispatcher).sendMessage(this.dispatcherService, this.message);
    }

    @Test
    public void WhenCallingOnDestroyShouldSetContextToNull(){
        this.dispatcherService.onDestroy();
        assertNull(this.dispatcherService.context);
    }

    @Test
    public void WhenCallingOnBindShouldReturnLocalBinder(){
        BindableDispatcherService.LocalBinder binder = (BindableDispatcherService.LocalBinder)this.dispatcherService.onBind(new Intent());
        Assert.assertNotNull(binder);
        Assert.assertEquals(this.dispatcherService, binder.getService());
    }
}
