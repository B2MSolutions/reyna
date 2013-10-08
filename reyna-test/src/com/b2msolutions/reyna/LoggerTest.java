package com.b2msolutions.reyna;

import android.util.Log;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.RobolectricTestRunner;
import com.xtremelabs.robolectric.internal.Implements;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
public class LoggerTest {
	
	private Logger logger;

	@Before
	public void setup() {
        Robolectric.bindShadowClass(ShadowLog.class);
        this.logger = new Logger(Log.INFO);
	}
	
	@Test
	public void constructionShouldNotThrow() {		
        assertNotNull(this.logger);
    }

    @Test
    public void LogVerboseShouldNotLogAndReturn0() {
        int actual = this.logger.v("TAG", "MSG");
        assertEquals(0, actual);
    }

    @Test
    public void LogDebugShouldNotLogAndReturn0() {
        int actual = this.logger.v("TAG", "MSG");
        assertEquals(0, actual);
    }

    @Test
    public void LogInfoShouldLog() {
        int actual = this.logger.i("TAG", "MSG");
        assertEquals(6, actual);
    }

    @Test
    public void LogWithNullMessageShouldNotThrowOrLog() {
        int actual = this.logger.i("TAG", null);
        assertEquals(0, actual);
    }

    @Test
    public void LogWithNullTagShouldNotThrowOrLog() {
        int actual = this.logger.i(null, null);
        assertEquals(0, actual);
    }

    @Test
    public void LogWithNullTagAndValidMsgShouldNotThrowOrLog() {
        int actual = this.logger.i(null, "MSG");
        assertEquals(0, actual);
    }

    @Test
    public void LogErrorShouldLog() {
        int actual = this.logger.e("TAG", "MSG");
        assertEquals(6, actual);
    }

    @Test
    public void LogVerboseForLoggerThatConstructedWithVerboseLevelShouldLog() {
        this.logger = new Logger(Log.VERBOSE);
        int actual = this.logger.v("TAG", "MSG");
        assertEquals(6, actual);
    }

    @Test
    public void LogDebugForLoggerThatConstructedWithVerboseLevelShouldLog() {
        this.logger = new Logger(Log.VERBOSE);
        int actual = this.logger.d("TAG", "MSG");
        assertEquals(6, actual);
    }

    @Test
    public void LogDebugForLoggerThatConstructedWithDebugLevelShouldLog() {
        this.logger = new Logger(Log.DEBUG);
        int actual = this.logger.d("TAG", "MSG");
        assertEquals(6, actual);
    }

    @Test
    public void LogDebugForLoggerThatConstructedWithDebugLevelWithNullMessageShouldNotLog() {
        this.logger = new Logger(Log.DEBUG);
        int actual = this.logger.d("TAG", null);
        assertEquals(0, actual);
    }

    @Implements(Log.class)
    public static class ShadowLog {
        public static int i(java.lang.String tag, java.lang.String msg) {
            System.out.println("[" + tag + "] " + msg);
            return tag.length() + msg.length();
        }

        public static int e(java.lang.String tag, java.lang.String msg) {
            System.out.println("[" + tag + "] " + msg);
            return tag.length() + msg.length();
        }

        public static int v(java.lang.String tag, java.lang.String msg) {
            System.out.println("[" + tag + "] " + msg);
            return tag.length() + msg.length();
        }

        public static int d(java.lang.String tag, java.lang.String msg) {
            System.out.println("[" + tag + "] " + msg);
            return tag.length() + msg.length();
        }
    }
}
