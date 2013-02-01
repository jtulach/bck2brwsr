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

import org.apidesign.bck2brwsr.core.JavaScriptBody;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class System {
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
    public static native void arraycopy(Object value, int srcBegin, Object dst, int dstBegin, int count);

    @JavaScriptBody(args = { "arr", "expectedSize" }, body = 
        "while (expectedSize-- > arr.length) { arr.push(0); }; return arr;"
    )
    public static native byte[] expandArray(byte[] arr, int expectedSize);

    @JavaScriptBody(args = {}, body = "new Date().getMilliseconds();")
    public static native long currentTimeMillis();
    
    public static long nanoTime() {
        return 1000L * currentTimeMillis();
    }
}
