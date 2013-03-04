/*
 * Copyright (c) 1996, 2009, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package java.util.zip;

import java.io.InputStream;
import java.io.IOException;

/**
 * This class implements an input stream filter for reading files in the
 * ZIP file format. Includes support for both compressed and uncompressed
 * entries.
 *
 * @author      David Connelly
 */
public
class ZipInputStream extends InflaterInputStream implements ZipConstants {
    private final org.apidesign.bck2brwsr.emul.zip.ZipInputStream impl;

    /**
     * Creates a new ZIP input stream.
     *
     * <p>The UTF-8 {@link java.nio.charset.Charset charset} is used to
     * decode the entry names.
     *
     * @param in the actual input stream
     */
    public ZipInputStream(InputStream in) {
        super(in);
        impl = new org.apidesign.bck2brwsr.emul.zip.ZipInputStream(in);
    }

    /**
     * Creates a new ZIP input stream.
     *
     * @param in the actual input stream
     *
     * @param charset
     *        The {@linkplain java.nio.charset.Charset charset} to be
     *        used to decode the ZIP entry name (ignored if the
     *        <a href="package-summary.html#lang_encoding"> language
     *        encoding bit</a> of the ZIP entry's general purpose bit
     *        flag is set).
     *
     * @since 1.7
     *
    public ZipInputStream(InputStream in, Charset charset) {
        super(new PushbackInputStream(in, 512), new Inflater(true), 512);
        usesDefaultInflater = true;
        if(in == null) {
            throw new NullPointerException("in is null");
        }
        if (charset == null)
            throw new NullPointerException("charset is null");
        this.zc = ZipCoder.get(charset);
    }
    */

    /**
     * Reads the next ZIP file entry and positions the stream at the
     * beginning of the entry data.
     * @return the next ZIP file entry, or null if there are no more entries
     * @exception ZipException if a ZIP file error has occurred
     * @exception IOException if an I/O error has occurred
     */
    public ZipEntry getNextEntry() throws IOException {
        return impl.getNextEntry();
    }

    /**
     * Closes the current ZIP entry and positions the stream for reading the
     * next entry.
     * @exception ZipException if a ZIP file error has occurred
     * @exception IOException if an I/O error has occurred
     */
    public void closeEntry() throws IOException {
        impl.closeEntry();
    }

    /**
     * Returns 0 after EOF has reached for the current entry data,
     * otherwise always return 1.
     * <p>
     * Programs should not count on this method to return the actual number
     * of bytes that could be read without blocking.
     *
     * @return     1 before EOF and 0 after EOF has reached for current entry.
     * @exception  IOException  if an I/O error occurs.
     *
     */
    public int available() throws IOException {
        return impl.available();
    }

    /**
     * Reads from the current ZIP entry into an array of bytes.
     * If <code>len</code> is not zero, the method
     * blocks until some input is available; otherwise, no
     * bytes are read and <code>0</code> is returned.
     * @param b the buffer into which the data is read
     * @param off the start offset in the destination array <code>b</code>
     * @param len the maximum number of bytes read
     * @return the actual number of bytes read, or -1 if the end of the
     *         entry is reached
     * @exception  NullPointerException if <code>b</code> is <code>null</code>.
     * @exception  IndexOutOfBoundsException if <code>off</code> is negative,
     * <code>len</code> is negative, or <code>len</code> is greater than
     * <code>b.length - off</code>
     * @exception ZipException if a ZIP file error has occurred
     * @exception IOException if an I/O error has occurred
     */
    public int read(byte[] b, int off, int len) throws IOException {
        return impl.read(b, off, len);
    }

    /**
     * Skips specified number of bytes in the current ZIP entry.
     * @param n the number of bytes to skip
     * @return the actual number of bytes skipped
     * @exception ZipException if a ZIP file error has occurred
     * @exception IOException if an I/O error has occurred
     * @exception IllegalArgumentException if n < 0
     */
    public long skip(long n) throws IOException {
        return impl.skip(n);
    }

    /**
     * Closes this input stream and releases any system resources associated
     * with the stream.
     * @exception IOException if an I/O error has occurred
     */
    public void close() throws IOException {
        impl.close();
    }

    /**
     * Creates a new <code>ZipEntry</code> object for the specified
     * entry name.
     *
     * @param name the ZIP file entry name
     * @return the ZipEntry just created
     */
    protected ZipEntry createZipEntry(String name) {
        return new ZipEntry(name);
    }

    @Override
    public int read() throws IOException {
        return impl.read();
    }

    @Override
    public boolean markSupported() {
        return impl.markSupported();
    }

    @Override
    public void mark(int readlimit) {
        impl.mark(readlimit);
    }

    @Override
    public void reset() throws IOException {
        impl.reset();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return impl.read(b);
    }
}
