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
package org.apidesign.bck2brwsr.emul.lang;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import org.apidesign.bck2brwsr.core.Exported;
import org.apidesign.bck2brwsr.core.JavaScriptBody;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@Exported
public class System {
    private static OutputStream stdErr;

    private System() {
    }

    @JavaScriptBody(args = { "value", "srcBegin", "dst", "dstBegin", "count" }, body =
        "if (srcBegin < dstBegin) {\n" +
        "    while (count-- > 0) {\n" +
        "        dst[dstBegin + count] = value[srcBegin + count];\n" +
        "    }\n" +
        "} else {\n" +
        "    while (count-- > 0) {\n" +
        "        dst[dstBegin++] = value[srcBegin++];\n" +
        "    }\n" +
        "}"
    )
    public static void arraycopy(Object src, int srcBegin, Object dst, int dstBegin, int count) {
        try {
            Class<?> system = Class.forName("java.lang.System");
            Method m = system.getMethod("arraycopy", Object.class, int.class, Object.class, int.class, int.class);
            m.invoke(null, src, srcBegin, dst, dstBegin, count);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    @JavaScriptBody(args = { "arr", "expectedSize" }, body =
        "while (expectedSize > arr.length) { arr.push(0); }; return arr;"
    )
    public static native byte[] expandArray(byte[] arr, int expectedSize);

    @JavaScriptBody(args = { "arr", "expectedSize" }, body =
        "while (expectedSize > arr.length) { arr.push(0); }; return arr;"
    )
    public static native char[] expandArray(char[] arr, int expectedSize);

    @JavaScriptBody(args = {}, body = "return new Date().getTime();")
    private static native double currentTimeMillisDouble();

    public static long currentTimeMillis() {
        return (long) currentTimeMillisDouble();
    }

    public static long nanoTime() {
        return 1000000L * currentTimeMillis();
    }
    @JavaScriptBody(args = { "obj" }, body="return vm.java_lang_Object(false).hashCode__I.call(obj);")
    public static native int identityHashCode(Object obj);

    public static Closeable activate() {
        return DUMMY;
    }
    private static final Closeable DUMMY = new Closeable() {
        @Override
        public void close() throws IOException {
        }
    };
    @JavaScriptBody(args = { "fn", "p" }, body = "return fn(p);")
    private static native Object invoke(Object fn, Object p);

    @Exported
    private static Object convArray(Object o, Object convToJS) {
        if (o instanceof Object[]) {
            Object[] arr = (Object[]) o;
            final int l = arr.length;
            Object[] ret = new Object[l];
            for (int i = 0; i < l; i++) {
                ret[i] = invoke(convToJS, arr[i]);
            }
            return ret;
        }
        return o;
    }

    public static void registerStdErr(OutputStream os) {
        stdErr = os;
    }

    public static void printStackTrace(String trace) {
        if (stdErr != null) {
            try {
                stdErr.write(trace.getBytes("UTF-8"));
            } catch (IOException ex) {
                warn(trace);
            }
        } else {
            warn(trace);
        }
    }

    @JavaScriptBody(args = { "msg" }, body = "if (console) console.warn(msg.toString());")
    private static native void warn(String msg);
}
