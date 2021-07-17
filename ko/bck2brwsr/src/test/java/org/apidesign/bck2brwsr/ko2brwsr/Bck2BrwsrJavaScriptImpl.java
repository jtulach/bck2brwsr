/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012-2018 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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
package org.apidesign.bck2brwsr.ko2brwsr;

import org.apidesign.bck2brwsr.core.JavaScriptBody;
import org.netbeans.html.json.tck.JavaScriptTCK;

public final class Bck2BrwsrJavaScriptImpl extends JavaScriptTCK {
    private static final Bck2BrwsrJavaScriptImpl TCK = new Bck2BrwsrJavaScriptImpl();

    public static void init() {
        log("Bck2BrwsrJavaScriptImpl is ready " + TCK);
    }

    private Bck2BrwsrJavaScriptImpl() {
    }

    @JavaScriptBody(args = "s", body = "if (typeof console === 'object') console.log(s);")
    private static native void log(String s);

    @JavaScriptBody(args = "script", body = "(0 || eval)(script); return true;")
    @Override
    public boolean executeNow(String script) throws Exception {
        return false;
    }

    static Class[] tests() {
        final Class<?>[] arr = testClasses();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].getSimpleName().startsWith("GC")) {
                arr[i] = Object.class;
            }
            if (arr[i].getSimpleName().equals("ExposedPropertiesTest")) {
                arr[i] = Object.class;
            }
        }
        return arr;
    }
}
