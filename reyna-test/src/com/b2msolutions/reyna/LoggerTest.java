package com.b2msolutions.reyna;

import android.util.Log;
import com.b2msolutions.reyna.system.Logger;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.RobolectricTestRunner;
import com.xtremelabs.robolectric.internal.Implements;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class LoggerTest {

    @Before
    public void setup() {
        Robolectric.bindShadowClass(ShadowLog.class);
    }

    @Test
    public void LogVerboseShouldNotLogAndReturn0() {
        int actual = Logger.v("TAG", "MSG");
        assertEquals(0, actual);
    }

    @Test
    public void LogDebugShouldNotLogAndReturn0() {
        int actual = Logger.v("TAG", "MSG");
        assertEquals(0, actual);
    }

    @Test
    public void LogInfoShouldLog() {
        int actual = Logger.i("TAG", "MSG");
        assertEquals(6, actual);
    }

    @Test
    public void LogWithNullMessageShouldNotThrowOrLog() {
        int actual = Logger.i("TAG", null);
        assertEquals(0, actual);
    }

    @Test
    public void LogWithNullTagShouldNotThrowOrLog() {
        int actual = Logger.i(null, null);
        assertEquals(0, actual);
    }

    @Test
    public void LogWithNullTagAndValidMsgShouldNotThrowOrLog() {
        int actual = Logger.i(null, "MSG");
        assertEquals(0, actual);
    }

    @Test
    public void LogErrorShouldLog() {
        int actual = Logger.e("TAG", "MSG");
        assertEquals(6, actual);
    }

    @Test
    public void LogVerboseAfterSetLevelVerboseLevelShouldLog() {
        Logger.setLevel(Log.VERBOSE);
        int actual = Logger.v("TAG", "MSG");
        assertEquals(6, actual);
    }

    @Test
    public void LogDebugAfterSetLevelWithVerboseLevelShouldLog() {
        Logger.setLevel(Log.VERBOSE);
        int actual = Logger.d("TAG", "MSG");
        assertEquals(6, actual);
    }

    @Test
    public void LogDebugAfterSetLevelWithDebugLevelShouldLog() {
        Logger.setLevel(Log.DEBUG);
        int actual = Logger.d("TAG", "MSG");
        assertEquals(6, actual);
    }

    @Test
    public void LogDebugAfterSetLevelWithDebugLevelAndMessageIsNullShouldNotLog() {
        Logger.setLevel(Log.DEBUG);
        int actual = Logger.d("TAG", null);
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
