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
package java.lang;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;
import org.apidesign.bck2brwsr.core.JavaScriptBody;

/** Poor man's re-implementation of most important System methods.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class System {
    private System() {
    }
    
    public static void arraycopy(Object value, int srcBegin, Object dst, int dstBegin, int count) {
        org.apidesign.bck2brwsr.emul.lang.System.arraycopy(value, srcBegin, dst, dstBegin, count);
    }
    
    public static long currentTimeMillis() {
        return org.apidesign.bck2brwsr.emul.lang.System.currentTimeMillis();
    }
    
    public static int identityHashCode(Object obj) {
        return obj.defaultHashCode();
    }

    public static String getProperty(String name) {
        if ("os.name".equals(name)) {
            return userAgent();
        }
        return null;
    }
    
    @JavaScriptBody(args = {}, body = "return (typeof navigator !== 'undefined') ? navigator.userAgent : 'unknown';")
    private static native String userAgent();
    
    public static String getProperty(String key, String def) {
        return def;
    }
    
    public static Properties getProperties() {
        throw new SecurityException();
    }
    
    public static void setProperties(Properties p) {
        throw new SecurityException();
    }
    
    /**
     * Returns the system-dependent line separator string.  It always
     * returns the same value - the initial value of the {@linkplain
     * #getProperty(String) system property} {@code line.separator}.
     *
     * <p>On UNIX systems, it returns {@code "\n"}; on Microsoft
     * Windows systems it returns {@code "\r\n"}.
     */
    public static String lineSeparator() {
        return "\n";
    }

    @JavaScriptBody(args = { "exitCode" }, body = "window.close();")
    public static void exit(int exitCode) {
    }
    
    public final static InputStream in;

    public final static PrintStream out;

    public final static PrintStream err;
    
    static {
        in = new ByteArrayInputStream(new byte[0]);
        out = err = new PrintStream(new ByteArrayOutputStream());
    }
    
}
