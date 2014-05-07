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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import org.apidesign.bck2brwsr.emul.zip.ZipInputStream;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class ZipArchive {
    private final Map<String, byte[]> entries = new LinkedHashMap<>();

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
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        for (;;) {
            int ch = is.read();
            if (ch == -1) {
                break;
            }
            os.write(ch);
        }
        os.close();
        entries.put(entry, os.toByteArray());
    }

    @Override
    public int hashCode() {
        return entries.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ZipArchive other = (ZipArchive) obj;
        if (!Objects.deepEquals(this.entries, other.entries)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, byte[]> en : entries.entrySet()) {
            String string = en.getKey();
            byte[] bs = en.getValue();
            sb.append(string).append(" = ").append(Arrays.toString(bs)).append("\n");
        }
        return sb.toString();
    }

    public void assertEquals(ZipArchive zip, String msg) {
        boolean ok = true;
        StringBuilder sb = new StringBuilder();
        sb.append(msg);
        for (Map.Entry<String, byte[]> en : entries.entrySet()) {
            String string = en.getKey();
            byte[] bs = en.getValue();
            byte[] other = zip.entries.get(string);
            sb.append("\n");
            if (other == null) {
                sb.append("EXTRA ").append(string).append(" = ").append(Arrays.toString(bs));
                ok = false;
                continue;
            }
            if (Arrays.equals(bs, other)) {
                sb.append("OK    ").append(string);
                continue;
            } else {
                sb.append("DIFF  ").append(string).append(" = ").append(Arrays.toString(bs)).append("\n");
                sb.append("    TO").append(string).append(" = ").append(Arrays.toString(other)).append("\n");
                ok = false;
                continue;
            }
        }
        for (Map.Entry<String, byte[]> entry : zip.entries.entrySet()) {
            String string = entry.getKey();
            if (entries.get(string) == null) {
                sb.append("MISS  ").append(string).append(" = ").append(Arrays.toString(entry.getValue()));
                ok = false;
            }
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
    
}
