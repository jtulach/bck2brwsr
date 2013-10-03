/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://opensource.org/licenses/GPL-2.0.
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
