package com.b2msolutions.reyna.messageProvider;

import android.app.AlarmManager;
import android.content.Context;
import com.b2msolutions.reyna.system.Preferences;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.net.URI;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;

@RunWith(RobolectricTestRunner.class)
public class BatchConfigurationTest {

    private BatchConfiguration batchConfiguration;

    private Context context;

    @Mock
    Preferences preferences;
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.context = RuntimeEnvironment.application.getApplicationContext();

        this.batchConfiguration = new BatchConfiguration(this.context);
        this.batchConfiguration.preferences = this.preferences;
    }

    @Test
    public void construction() {
        this.batchConfiguration = new BatchConfiguration(this.context);

        assertNotNull(this.batchConfiguration);
        assertNotNull(this.batchConfiguration.preferences);
    }

    @Test
    public void shouldHaveCorrectValues() {
        assertEquals(AlarmManager.INTERVAL_DAY, this.batchConfiguration.getSubmitInterval());
        assertEquals(100, this.batchConfiguration.getBatchMessageCount());
        assertEquals(300 * 1024, this.batchConfiguration.getBatchMessagesSize());
    }

    @Test
    public void shouldReturnExpectedUrl() {
        doReturn(URI.create("www.post.com")).when(this.preferences).getBatchUploadUrl();

        assertEquals("www.post.com", this.batchConfiguration.getBatchUrl().toString());
    }

    @Test
    public void shouldReturnExpectedCheckTime() {
        doReturn(120L).when(this.preferences).getBatchUploadCheckInterval();

        assertEquals(120L, this.batchConfiguration.getCheckInterval());
    }
}
