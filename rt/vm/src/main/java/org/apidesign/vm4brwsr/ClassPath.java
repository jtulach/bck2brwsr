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
import java.net.URL;
import org.apidesign.bck2brwsr.core.JavaScriptBody;

/** Conversion from classpath to load function.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class ClassPath {
    private ClassPath() {
    }
    
    public static void init() {
    }
    @JavaScriptBody(args = { "arr" }, body = "return arr.length;")
    private static native int length(Object arr);
    @JavaScriptBody(args = { "arr", "index" }, body = "return arr[index];")
    private static native Object at(Object arr, int index);
    @JavaScriptBody(args = { "arr", "index", "value" }, body = "arr[index] = value; return value;")
    private static native Object set(Object arr, int index, Object value);
    
    public static byte[] loadFromCp(Object classpath, String res, int skip) 
    throws IOException, ClassNotFoundException {
        for (int i = 0; i < length(classpath); i++) {
            Object c = at(classpath, i);
            if (c instanceof String) {
                try {
                    String url = (String)c;
                    final ZipHandler z = ZipHandler.toZip(url);
                    c = set(classpath, i, z);
                    final byte[] man = z.findRes("META-INF/MANIFEST.MF");
                    if (man != null) {
                        String mainClass = processClassPathAttr(man, url, classpath);
//                        if (mainClass != null) {
//                            Class.forName(mainClass);
//                        }
                    }
                } catch (IOException ex) {
                    set(classpath, i, ex);
                    log("Cannot load " + c + " - " + ex.getClass().getName() + ":" + ex.getMessage());
                }
            }
            if (res != null) {
                byte[] checkRes;
                if (c instanceof ZipHandler) {
                    checkRes = ((ZipHandler)c).findRes(res);
                    if (checkRes != null && --skip < 0) {
                        return checkRes;
                    }
                } else {
                    checkRes = callFunction(c, res, skip);
                    if (checkRes != null) {
                        return checkRes;
                    }
                }
            }
        }
        return null;
    }
    
    @JavaScriptBody(args = { "fn", "res", "skip" }, body = 
        "if (typeof fn === 'function') return fn(res, skip);\n"
      + "return null;"
    )
    private static native byte[] callFunction(Object fn, String res, int skip);
    
    @JavaScriptBody(args = { "msg" }, body = "if (typeof console !== 'undefined') console.log(msg.toString());")
    private static native void log(String msg);

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
}
