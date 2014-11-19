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
package org.apidesign.bck2brwsr.vmtest.impl;

import net.java.html.js.JavaScriptBody;
import net.java.html.js.JavaScriptResource;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@JavaScriptResource("htmlannotations.js")
public class HtmlAnnotations {
    private Object callback;
    
    
    @JavaScriptBody(args = {}, body = "return 42;")
    public static int fourtyTwo() {
        return -1;
    }
    
    @JavaScriptBody(args = { "x", "y" }, body = "return mul(x, y);")
    public static native int useExternalMul(int x, int y);
    
    public static int callback() {
        final int[] arr = { 0 };
        callback(new Runnable() {
            @Override
            public void run() {
                arr[0]++;
            }
        });
        return arr[0];
    }
    
    @JavaScriptBody(args = { "r" }, javacall=true, body = "r.@java.lang.Runnable::run()()")
    private static native void callback(Runnable r);

    @JavaScriptBody(args = {  }, javacall = true, body = "return @org.apidesign.bck2brwsr.vmtest.impl.HtmlAnnotations::callback()();")
    public static native int staticCallback();

    @JavaScriptBody(args = {  }, wait4js = false, body = "/* do nothing */")
    public static native void empty();
    
    
    protected long chooseLong(boolean takeFirst, boolean takeSecond, long first, long second) {
        long l = 0;
        if (takeFirst) l += first;
        if (takeSecond) l += second;
        return l;
    }
    
    protected void onError(Object obj) throws Exception {
        callback = obj;
    }
    
    Object getError() {
        return callback;
    }
    
    public static Object create() {
        return new HtmlAnnotations();
    }
    @JavaScriptBody(args = { "impl", "a", "b" }, javacall = true, body = 
        "return impl.@org.apidesign.bck2brwsr.vmtest.impl.HtmlAnnotations::chooseLong(ZZJJ)(true, false, a, b);"
    )
    public static native long first(Object impl, long a, long b);
    
    @JavaScriptBody(args = { "impl", "d" }, javacall = true, body = 
        "impl.@org.apidesign.bck2brwsr.vmtest.impl.HtmlAnnotations::onError(Ljava/lang/Object;)(d);" +
        "return impl.@org.apidesign.bck2brwsr.vmtest.impl.HtmlAnnotations::getError()();"
    )
    public static native Double onError(Object impl, Double d);
}
