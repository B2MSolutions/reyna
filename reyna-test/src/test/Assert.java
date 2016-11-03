package test;

import android.content.Intent;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.robolectric.Shadows.shadowOf;


public class Assert {
    public static Intent assertServiceStarted(java.lang.Class<?> expected) {
        Intent intent = shadowOf(org.robolectric.RuntimeEnvironment.application).getNextStartedService();
        assertNotNull(intent);
        org.robolectric.shadows.ShadowIntent shadowIntent = shadowOf(intent);
        assertEquals(expected, shadowIntent.getIntentClass());
        return intent;
    }

    public static void assertServiceNotStarted(java.lang.Class<?> expected) {
        Intent intent = shadowOf(org.robolectric.RuntimeEnvironment.application).getNextStartedService();
        if(intent != null) {
            org.robolectric.shadows.ShadowIntent shadowIntent = shadowOf(intent);
            assertNotSame(expected, shadowIntent.getIntentClass());
        }
    }
}
