package com.b2msolutions.reyna.http;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import static junit.framework.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class OutputStreamFactoryTest {

    @Test
    public void whenConstructingShouldNotThrow(){
        assertNotNull(new OutputStreamFactory());
    }

    @Test
    public void whenCreateGzipOutputStreamShouldReturnRightType() throws IOException {
        OutputStreamFactory factory = new OutputStreamFactory();
        OutputStream outputStream = factory.createGzipOutputStream(new ByteArrayOutputStream());
        assertTrue(outputStream instanceof GZIPOutputStream);
    }

}
