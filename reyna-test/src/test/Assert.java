package test;

import android.content.Intent;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;


public class Assert {
    public static Intent assertServiceStartedOrgRobolectric(java.lang.Class<?> expected) {
        Intent intent = org.robolectric.Robolectric.getShadowApplication().getNextStartedService();
        assertNotNull(intent);
        org.robolectric.shadows.ShadowIntent shadowIntent = org.robolectric.Robolectric.shadowOf(intent);
        assertEquals(expected, shadowIntent.getIntentClass());
        return intent;
    }

    public static void assertServiceNotStartedOrgRobolectric(java.lang.Class<?> expected) {
        Intent intent = org.robolectric.Robolectric.getShadowApplication().getNextStartedService();
        if(intent != null) {
            org.robolectric.shadows.ShadowIntent shadowIntent = org.robolectric.Robolectric.shadowOf(intent);
            assertNotSame(expected, shadowIntent.getIntentClass());
        }
    }
}
