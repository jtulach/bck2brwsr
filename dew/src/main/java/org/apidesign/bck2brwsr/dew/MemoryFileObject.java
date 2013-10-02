/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.apidesign.bck2brwsr.dew;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

/**
 *
 * @author Tomas Zeuzla
 */
class MemoryFileObject extends BaseFileObject {

    private byte[] content;
    private long lastModified;

    MemoryFileObject (
            String resourceName,
            Kind kind,
            byte[] content) {
        super(resourceName, kind);
        this.content = content;
        this.lastModified = this.content == null ?
            -1 :
            System.currentTimeMillis();
    }

    MemoryFileObject (
            String resourceName,
            byte[] content) {
        this(resourceName, getKind(resourceName) ,content);
    }


    @Override
    public InputStream openInputStream() throws IOException {
        if (content == null) {
            throw new IOException();
        } else {
            return new ByteArrayInputStream(content);
        }
    }

    @Override
    public OutputStream openOutputStream() throws IOException {
        return new CloseStream();
    }

    @Override
    public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
        return new InputStreamReader(openInputStream());
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        if (content == null) {
            throw new IOException();
        } else {
            return new String(content);
        }
    }

    @Override
    public Writer openWriter() throws IOException {
        return new OutputStreamWriter(openOutputStream());
    }

    @Override
    public long getLastModified() {
        return lastModified;
    }

    @Override
    public boolean delete() {
        return false;
    }

    byte[] getContent() {
        return content;
    }

    private class CloseStream extends OutputStream {

        private final ByteArrayOutputStream delegate;

        CloseStream() {
            delegate = new ByteArrayOutputStream();
        }

        @Override
        public void write(int b) throws IOException {
            delegate.write(b);
        }

        @Override
        public void write(byte[] b) throws IOException {
            delegate.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            delegate.write(b, off, len);
        }

        @Override
        public void close() throws IOException {
            delegate.close();
            content = delegate.toByteArray();
            lastModified = System.currentTimeMillis();
        }                                    

    }

}
