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
import org.apidesign.bck2brwsr.core.JavaScriptBody;
import org.apidesign.bck2brwsr.emul.zip.FastJar;

/** Conversion from classpath to load function.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class Zips {
    private final FastJar fj;

    private Zips(String path, byte[] zipData) throws IOException {
        long bef = timeNow();
        fj = new FastJar(zipData);
        for (FastJar.Entry e : fj.list()) {
            putRes(e.name, e);
        }
        log("Iterating thru " + path + " took " + (timeNow() - bef) + "ms");
    }
    
    public static void init() {
    }
    @JavaScriptBody(args = { "arr" }, body = "return arr.length;")
    private static native int length(Object arr);
    @JavaScriptBody(args = { "arr", "index" }, body = "return arr[index];")
    private static native Object at(Object arr, int index);
    @JavaScriptBody(args = { "arr", "index", "value" }, body = "arr[index] = value; return value;")
    private static native Object set(Object arr, int index, Object value);
    
    public static byte[] loadFromCp(Object classpath, String res) 
    throws IOException, ClassNotFoundException {
        for (int i = 0; i < length(classpath); i++) {
            Object c = at(classpath, i);
            if (c instanceof String) {
                try {
                    String url = (String)c;
                    final Zips z = toZip(url);
                    c = set(classpath, i, z);
                    final byte[] man = z.findRes("META-INF/MANIFEST.MF");
                    if (man != null) {
                        String mainClass = processClassPathAttr(man, url, classpath);
                        if (mainClass != null) {
                            Class.forName(mainClass);
                        }
                    }
                } catch (IOException | ClassNotFoundException ex) {
                    set(classpath, i, ex);
                    throw ex;
                }
            }
            if (res != null) {
                byte[] checkRes;
                if (c instanceof Zips) {
                    checkRes = ((Zips)c).findRes(res);
                } else {
                    checkRes = callFunction(c, res);
                }
                if (checkRes != null) {
                    return checkRes;
                }
            }
        }
        return null;
    }
    
    @JavaScriptBody(args = { "fn", "res" }, body = 
        "if (typeof fn === 'function') return fn(res);\n"
      + "return null;"
    )
    private static native byte[] callFunction(Object fn, String res);
    
    @JavaScriptBody(args = { "msg" }, body = "console.log(msg.toString());")
    private static native void log(String msg);

    private byte[] findRes(String res) throws IOException {
        Object arr = findResImpl(res);
        if (arr instanceof FastJar.Entry) {
            long bef = timeNow();
            InputStream zip = fj.getInputStream((FastJar.Entry)arr);
            arr = readFully(new byte[512], zip);
            putRes(res, arr);
            log("Reading " + res + " took " + (timeNow() - bef) + "ms");
        }
        return (byte[]) arr;
    }

    @JavaScriptBody(args = { "res" }, body = "var r = this[res]; return r ? r : null;")
    private native Object findResImpl(String res);

    @JavaScriptBody(args = { "res", "arr" }, body = "this[res] = arr;")
    private native void putRes(String res, Object arr);
    
    private static Zips toZip(String path) throws IOException {
        URL u = new URL(path);
        byte[] zipData = (byte[]) u.getContent(new Class[] { byte[].class });
        return new Zips(path, zipData);
    }

    private static String processClassPathAttr(final byte[] man, String url, Object classpath) throws IOException {
        try (ParseMan is = new ParseMan(new ByteArrayInputStream(man))) {
            String cp = is.toString();
            if (cp != null) {
                cp = cp.trim();
                for (int p = 0; p < cp.length();) {
                    int n = cp.indexOf(' ', p);
                    if (n == -1) {
                        n = cp.length();
                    }
                    String el = cp.substring(p, n);
                    URL u = new URL(new URL(url), el);
                    classpath = addToArray(classpath, u.toString());
                    p = n + 1;
                }
            }
            return is.getMainClass();
        }
    }

    private static Object addToArray(Object arr, String value) {
        final int last = length(arr);
        Object ret = enlargeArray(arr, last + 1);
        set(ret, last, value);
        return ret;
    }

    @JavaScriptBody(args = { "arr", "len" }, body = "while (arr.length < len) arr.push(null); return arr;")
    private static native Object enlargeArray(Object arr, int len);
    @JavaScriptBody(args = { "arr", "len" }, body = "while (arr.length < len) arr.push(0);")
    private static native void enlargeBytes(byte[] arr, int len);

    @JavaScriptBody(args = { "arr", "len" }, body = "arr.splice(len, arr.length - len);")
    private static native void sliceArray(byte[] arr, int len);

    private static Object readFully(byte[] arr, InputStream zip) throws IOException {
        int offset = 0;
        for (;;) {
            int len = zip.read(arr, offset, arr.length - offset);
            if (len == -1) {
                break;
            }
            offset += len;
            if (offset == arr.length) {
                enlargeBytes(arr, arr.length + 4096);
            }
        }
        sliceArray(arr, offset);
        return arr;
    }

    private static long timeNow() {
        double time = m();
        if (time >= 0) {
            return (long)time;
        }
        return org.apidesign.bck2brwsr.emul.lang.System.currentTimeMillis();
    }
    @JavaScriptBody(args = {}, body = 
        "if (typeof window.performance === 'undefined') return -1;\n"
      + "if (typeof window.performance.now === 'undefined') return -1;\n"
      + "return window.performance.now();"
    )
    private static native double m();
    
    
}
