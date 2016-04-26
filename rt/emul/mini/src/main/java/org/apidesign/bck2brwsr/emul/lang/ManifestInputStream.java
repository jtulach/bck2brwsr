/*
 * Copyright (c) 1997, 2008, Oracle and/or its affiliates. All rights reserved.
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
package org.apidesign.bck2brwsr.emul.lang;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apidesign.bck2brwsr.core.Exported;

/*
 * A fast buffered input stream for parsing manifest files.
 * 
 * Taken from java.util.jar.Manifest.FastInputStream and modified to be
 * independent of other Manifest functionality.
 */
@Exported
public abstract class ManifestInputStream extends FilterInputStream {
    private byte[] buf;
    private int count = 0;
    private int pos = 0;

    protected ManifestInputStream(InputStream in) {
        this(in, 8192);
    }

    protected ManifestInputStream(InputStream in, int size) {
        super(in);
        buf = new byte[size];
    }

    public int read() throws IOException {
        if (pos >= count) {
            fill();
            if (pos >= count) {
                return -1;
            }
        }
        return buf[pos++] & 0xff;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        int avail = count - pos;
        if (avail <= 0) {
            if (len >= buf.length) {
                return in.read(b, off, len);
            }
            fill();
            avail = count - pos;
            if (avail <= 0) {
                return -1;
            }
        }
        if (len > avail) {
            len = avail;
        }
        System.arraycopy(buf, pos, b, off, len);
        pos += len;
        return len;
    }

    /*
     * Reads 'len' bytes from the input stream, or until an end-of-line
     * is reached. Returns the number of bytes read.
     */
    public int readLine(byte[] b, int off, int len) throws IOException {
        byte[] tbuf = this.buf;
        int total = 0;
        while (total < len) {
            int avail = count - pos;
            if (avail <= 0) {
                fill();
                avail = count - pos;
                if (avail <= 0) {
                    return -1;
                }
            }
            int n = len - total;
            if (n > avail) {
                n = avail;
            }
            int tpos = pos;
            int maxpos = tpos + n;
            while (tpos < maxpos && tbuf[tpos++] != '\n') {
                ;
            }
            n = tpos - pos;
            System.arraycopy(tbuf, pos, b, off, n);
            off += n;
            total += n;
            pos = tpos;
            if (tbuf[tpos - 1] == '\n') {
                break;
            }
        }
        return total;
    }

    public byte peek() throws IOException {
        if (pos == count) {
            fill();
        }
        if (pos == count) {
            return -1; // nothing left in buffer
        }
        return buf[pos];
    }

    public int readLine(byte[] b) throws IOException {
        return readLine(b, 0, b.length);
    }

    public long skip(long n) throws IOException {
        if (n <= 0) {
            return 0;
        }
        long avail = count - pos;
        if (avail <= 0) {
            return in.skip(n);
        }
        if (n > avail) {
            n = avail;
        }
        pos += n;
        return n;
    }

    public int available() throws IOException {
        return (count - pos) + in.available();
    }

    public void close() throws IOException {
        if (in != null) {
            in.close();
            in = null;
            buf = null;
        }
    }

    private void fill() throws IOException {
        count = pos = 0;
        int n = in.read(buf, 0, buf.length);
        if (n > 0) {
            count = n;
        }
    }
    
    protected abstract String putValue(String key, String value);

    public void readAttributes(byte[] lbuf) throws IOException {
        ManifestInputStream is = this;

        String name = null;
        String value = null;
        byte[] lastline = null;
        int len;
        while ((len = is.readLine(lbuf)) != -1) {
            boolean lineContinued = false;
            if (lbuf[--len] != '\n') {
                throw new IOException("line too long");
            }
            if (len > 0 && lbuf[len - 1] == '\r') {
                --len;
            }
            if (len == 0) {
                break;
            }
            int i = 0;
            if (lbuf[0] == ' ') {
                if (name == null) {
                    throw new IOException("misplaced continuation line");
                }
                lineContinued = true;
                byte[] buf = new byte[lastline.length + len - 1];
                System.arraycopy(lastline, 0, buf, 0, lastline.length);
                System.arraycopy(lbuf, 1, buf, lastline.length, len - 1);
                if (is.peek() == ' ') {
                    lastline = buf;
                    continue;
                }
                value = new String(buf, 0, buf.length, "UTF8");
                lastline = null;
            } else {
                while (lbuf[i++] != ':') {
                    if (i >= len) {
                        throw new IOException("invalid header field");
                    }
                }
                if (lbuf[i++] != ' ') {
                    throw new IOException("invalid header field");
                }
                name = new String(lbuf, 0, 0, i - 2);
                if (is.peek() == ' ') {
                    lastline = new byte[len - i];
                    System.arraycopy(lbuf, i, lastline, 0, len - i);
                    continue;
                }
                value = new String(lbuf, i, len - i, "UTF8");
            }
            try {
                if ((putValue(name, value) != null) && (!lineContinued)) {
                    throw new IOException("Duplicate name in Manifest: " + name + ".\n" + "Ensure that the manifest does not " + "have duplicate entries, and\n" + "that blank lines separate " + "individual sections in both your\n" + "manifest and in the META-INF/MANIFEST.MF " + "entry in the jar file.");
                }
            } catch (IllegalArgumentException e) {
                throw new IOException("invalid header field name: " + name);
            }
        }
    }
}
