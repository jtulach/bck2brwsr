/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.apidesign.bck2brwsr.emul.zip;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 * @author Tomas Zezula
 */
public final class FastJar {
    private final byte[] arr;

    public FastJar(byte[] arr) {
        this.arr = arr;
    }
    
    
    private static final int GIVE_UP = 1<<16;

    public static final  class Entry {
        
        public final String name;
        final long offset;
        private final long dosTime;
        
        Entry (String name, long offset, long time) {
            assert name != null;
            this.name = name;
            this.offset = offset;
            this.dosTime = time;
        }        
/*        
        public long getTime () {
            Date d = new Date((int)(((dosTime >> 25) & 0x7f) + 80),
                    (int)(((dosTime >> 21) & 0x0f) - 1),
                    (int)((dosTime >> 16) & 0x1f),
                    (int)((dosTime >> 11) & 0x1f),
                    (int)((dosTime >> 5) & 0x3f),
                    (int)((dosTime << 1) & 0x3e));
            return d.getTime();
        }
        */
    }
    
    public InputStream getInputStream (final Entry e) throws IOException {
        return getInputStream(arr, e.offset);
    }
    
    private static InputStream getInputStream (byte[] arr, final long offset) throws IOException {
        ByteArrayInputStream is = new ByteArrayInputStream(arr);
        is.skip(offset);
        ZipInputStream in = new ZipInputStream (is);
        ZipEntry e = in.getNextEntry();
        if (e != null && e.getCrc() == 0L && e.getMethod() == ZipEntry.STORED) {
            int cp = arr.length - is.available();
            return new ByteArrayInputStream(arr, cp, (int)e.getSize());
        }
        return in;
    }
    
    public Entry[] list() throws IOException {
        final int size = arr.length;

        int at = size - ZipInputStream.ENDHDR;

        byte[] data = new byte[ZipInputStream.ENDHDR];        
        int giveup = 0;

        do {
            System.arraycopy(arr, at, data, 0, data.length);
            at--;
            giveup++;
            if (giveup > GIVE_UP) {
                throw new IOException ();
            }
        } while (getsig(data) != ZipInputStream.ENDSIG);


        final long censize = endsiz(data);
        final long cenoff  = endoff(data);
        at = (int) cenoff;                                                     

        Entry[] result = new Entry[0];
        int cenread = 0;
        data = new byte[ZipInputStream.CENHDR];
        while (cenread < censize) {
            System.arraycopy(arr, at, data, 0, data.length);
            at += data.length;
            if (getsig(data) != ZipInputStream.CENSIG) {
                throw new IOException("No central table");          //NOI18N
            }
            int cennam = cennam(data);
            int cenext = cenext(data);
            int cencom = cencom(data);
            long lhoff = cenoff(data);
            long centim = centim(data);
            String name = new String(arr, at, cennam, "UTF-8");
            at += cennam;
            int seekby = cenext+cencom;
            int cendatalen = ZipInputStream.CENHDR + cennam + seekby;
            cenread+=cendatalen;
            result = addEntry(result, new Entry(name,lhoff, centim));
            at += seekby;
        }
        return result;
    }

    private Entry[] addEntry(Entry[] result, Entry entry) {
        Entry[] e = new Entry[result.length + 1];
        e[result.length] = entry;
        System.arraycopy(result, 0, e, 0, result.length);
        return e;
    }

    private static final long getsig(final byte[] b) throws IOException {return get32(b,0);}
    private static final long endsiz(final byte[] b) throws IOException {return get32(b,ZipInputStream.ENDSIZ);}
    private static final long endoff(final byte[] b) throws IOException {return get32(b,ZipInputStream.ENDOFF);}
    private static final long  cenlen(final byte[] b) throws IOException {return get32(b,ZipInputStream.CENLEN);}
    private static final long  censiz(final byte[] b) throws IOException {return get32(b,ZipInputStream.CENSIZ);}
    private static final long centim(final byte[] b) throws IOException {return get32(b,ZipInputStream.CENTIM);}
    private static final int  cennam(final byte[] b) throws IOException {return get16(b,ZipInputStream.CENNAM);}
    private static final int  cenext(final byte[] b) throws IOException {return get16(b,ZipInputStream.CENEXT);}
    private static final int  cencom(final byte[] b) throws IOException {return get16(b,ZipInputStream.CENCOM);}
    private static final long cenoff (final byte[] b) throws IOException {return get32(b,ZipInputStream.CENOFF);}
    private static final int lochow(final byte[] b) throws IOException {return get16(b,ZipInputStream.LOCHOW);}
    private static final int locname(final byte[] b) throws IOException {return get16(b,ZipInputStream.LOCNAM);}
    private static final int locext(final byte[] b) throws IOException {return get16(b,ZipInputStream.LOCEXT);}
    private static final long locsiz(final byte[] b) throws IOException {return get32(b,ZipInputStream.LOCSIZ);}
    
    private static final int get16(final byte[] b, int off) throws IOException {        
        final int b1 = b[off];
	final int b2 = b[off+1];
        return (b1 & 0xff) | ((b2 & 0xff) << 8);
    }

    private static final long get32(final byte[] b, int off) throws IOException {
	final int s1 = get16(b, off);
	final int s2 = get16(b, off+2);
        return s1 | ((long)s2 << 16);
    }

}
