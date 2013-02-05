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
package org.apidesign.vm4brwsr;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apidesign.bck2brwsr.core.JavaScriptBody;

/** Conversion from classpath to load function.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class Zips {
    private Zips() {
    }
    
    public static void init() {
    }
    
    public static byte[] loadFromCp(Object[] classpath, String res) {
        for (int i = 0; i < classpath.length; i++) {
            Object c = classpath[i];
            if (c instanceof String) {
                try {
                    String url = (String)c;
                    final Zips z = toZip(url);
                    c = classpath[i] = z;
                    final byte[] man = z.findRes("META-INF/MANIFEST.MF");
                    if (man != null) {
                        processClassPathAttr(man, url, classpath);
                    }
                } catch (IOException ex) {
                    classpath[i] = ex;
                }
            }
            if (c instanceof Zips) {
                Object checkRes = ((Zips)c).findRes(res);
                if (checkRes instanceof byte[]) {
                    return (byte[])checkRes;
                }
            }
        }
        return null;
    }

    @JavaScriptBody(args = { "res" }, body = "var r = this[res]; return r ? r : null;")
    private native byte[] findRes(String res);

    @JavaScriptBody(args = { "res", "arr" }, body = "this[res] = arr;")
    private native void putRes(String res, byte[] arr);
    
    private static Zips toZip(String path) throws IOException {
        URL u = new URL(path);
        ZipInputStream zip = new ZipInputStream(u.openStream());
        Zips z = new Zips();
        for (;;) {
            ZipEntry entry = zip.getNextEntry();
            if (entry == null) {
                break;
            }
            byte[] arr = new byte[4096];
            int offset = 0;
            for (;;) {
                int len = zip.read(arr, offset, arr.length - offset);
                if (len == -1) {
                    break;
                }
                offset += len;
                if (offset == arr.length) {
                    enlargeArray(arr, arr.length + 4096);
                }
            }
            sliceArray(arr, offset);
            z.putRes(entry.getName(), arr);
        }
        return z;
    }

    private static void processClassPathAttr(final byte[] man, String url, Object[] classpath) throws IOException {
        try (InputStream is = initIS(new ByteArrayInputStream(man))) {
            String cp = is.toString();
            if (cp == null) {
                return;
            }
            for (int p = 0; p < cp.length();) {
                int n = cp.indexOf(':', p);
                if (n == -1) {
                    n = cp.length();
                }
                String el = cp.substring(p, n);
                URL u = new URL(new URL(url), el);
                classpath = addToArray(classpath, u.toString());
                p = n;
            }
        }
    }

    private static InputStream initIS(InputStream is) throws IOException {
        return new ParseCPAttr(is);
    }
    
    private static Object[] addToArray(Object[] arr, String value) {
        final int last = arr.length;
        Object[] ret = enlargeArray(arr, last + 1);
        ret[last] = value;
        return ret;
    }

    @JavaScriptBody(args = { "arr", "len" }, body = "while (arr.length < len) arr.push(null); return arr;throw('Arr: ' + arr);")
    private static native Object[] enlargeArray(Object[] arr, int len);
    @JavaScriptBody(args = { "arr", "len" }, body = "while (arr.length < len) arr.push(0);")
    private static native void enlargeArray(byte[] arr, int len);

    @JavaScriptBody(args = { "arr", "len" }, body = "arr.splice(len, arr.length - len);")
    private static native void sliceArray(byte[] arr, int len);
    
    
}
