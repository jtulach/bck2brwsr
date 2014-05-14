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
package org.apidesign.bck2brwsr.emul.zip;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class ZipArchive {
    private Entry first;

    public static ZipArchive createZip(InputStream is) throws IOException {
        ZipArchive a = new ZipArchive();
        readZip(is, a);
        return a;
    }

    public static ZipArchive createReal(InputStream is) throws IOException {
        ZipArchive a = new ZipArchive();
        realZip(is, a);
        return a;
    }

    /**
     * Registers entry name and data
     */
    final void register(String entry, InputStream is) throws IOException {
        byte[] arr = new byte[12 * 4096];
        for (int i = 0; i < arr.length; i++) {
            int ch = is.read();
            if (ch == -1) {
                byte[] tmp = new byte[i];
                FastJar.arraycopy(arr, 0, tmp, 0, i);
                arr = tmp;
                break;
            }
            arr[i] = (byte) ch;
        }
        first = new Entry (entry, arr, first);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Entry e = first;
        while (e != null) {
            String string = e.name;
            byte[] bs = e.arr;
            sb.append(string).append(" = ").append(toString(bs)).append("\n");
            e = e.next;
        }
        return sb.toString();
    }

    public void assertEquals(ZipArchive zip, String msg) {
        boolean ok = true;
        StringBuilder sb = new StringBuilder();
        sb.append(msg);
        Entry e = first;
        while (e != null) {
            String string = e.name;
            byte[] bs = e.arr;
            byte[] other = zip.find(string);
            e = e.next;
            
            sb.append("\n");
            if (other == null) {
                sb.append("EXTRA ").append(string).append(" = ").append(toString(bs));
                ok = false;
                continue;
            }
            if (equals(bs, other)) {
                sb.append("OK    ").append(string);
                continue;
            } else {
                sb.append("DIFF  ").append(string).append(" = ").append(toString(bs)).append("\n");
                sb.append("    TO").append(string).append(" = ").append(toString(other)).append("\n");
                ok = false;
                continue;
            }
        }
        e = zip.first;
        while (e != null) {
            String string = e.name;
            if (find(string) == null) {
                sb.append("MISS  ").append(string).append(" = ").append(toString(e.arr));
                ok = false;
            }
            e = e.next;
        }
        if (!ok) {
            assert false : sb.toString();
        }
    }

    public static void readZip(InputStream is, ZipArchive data) throws IOException {
        ZipInputStream zip = new org.apidesign.bck2brwsr.emul.zip.ZipInputStream(is);
        for (;;) {
            ZipEntry en = zip.getNextEntry();
            if (en == null) {
                return;
            }
            data.register(en.getName(), zip);
        }
    }

    public static void realZip(InputStream is, ZipArchive data) throws IOException {
        java.util.zip.ZipInputStream zip = new java.util.zip.ZipInputStream(is);
        for (;;) {
            ZipEntry en = zip.getNextEntry();
            if (en == null) {
                return;
            }
            data.register(en.getName(), zip);
        }
    }

    private byte[] find(String name) {
        Entry e = first;
        while (e != null) {
            if (e.name.equals(name)) {
                return e.arr;
            }
            e = e.next;
        }
        return null;
    }

    private boolean equals(byte[] bs, byte[] other) {
        if (bs.length != other.length) {
            return false;
        }
        for (int i = 0; i < bs.length; i++) {
            if (bs[i] != other[i]) {
                return false;
            }
        }
        return true;
    }

    private Object toString(byte[] arr) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        String sep = "";
        for (int i = 0; i < arr.length; i++) {
            sb.append(sep).append(arr[i]);
            sep = ", ";
        }
        sb.append("]");
        return sb.toString();
    }

    private static final class Entry {
        final String name;
        final byte[] arr;
        final Entry next;

        public Entry(String name, byte[] arr, Entry next) {
            this.name = name;
            this.arr = arr;
            this.next = next;
        }
    }
}
