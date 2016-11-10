package com.b2msolutions.reyna.http;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

public class OutputStreamFactory {
    public OutputStream createGzipOutputStream(OutputStream newStreamConstructorArg) throws IOException {
        return new GZIPOutputStream(newStreamConstructorArg);
    }
}
