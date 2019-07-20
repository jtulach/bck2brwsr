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

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    public static long nanoTime() {
        return org.apidesign.bck2brwsr.emul.lang.System.nanoTime();

    }

    public static int identityHashCode(Object obj) {
        return Class.defaultHashCode(obj);
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

    @JavaScriptBody(args = { "exitCode" }, body = ""
        + "var xhttp = new XMLHttpRequest();\n"
        + "xhttp.open('GET', '/?exit=' + exitCode, true);\n"
        + "xhttp.onreadystatechange = function () {\n"
        + "  window.close();\n"
        + "};\n"
        + "xhttp.send();\n"
    )
    public static void exit(int exitCode) {
    }

    public final static InputStream in;

    public final static PrintStream out;

    public final static PrintStream err;

    public static void setOut(PrintStream out) {
        throw new SecurityException();
    }

    public static void setIn(InputStream in) {
        throw new SecurityException();
    }

    public static void setErr(PrintStream err) {
        throw new SecurityException();
    }

    static {
        in = new ByteArrayInputStream(new byte[0]);
        PrintStream log;
        PrintStream warn;
        final SystemStream rawWarn;
        try {
            log = new PrintStream(new BufferedOutputStream(new SystemStream("log")));
            rawWarn = new SystemStream("warn");
            org.apidesign.bck2brwsr.emul.lang.System.registerStdErr(rawWarn);
            warn = new PrintStream(new BufferedOutputStream(rawWarn));
        } catch (Exception ex) {
            log = null;
            warn = null;
        }
        out = log;
        err = warn;
    }

    private static final class SystemStream extends OutputStream {
        private final String method;
        private final StringBuilder pending = new StringBuilder();
        private Runnable sendOK = new Runnable() {
            @Override
            public void run() {
                sendOK = null;
            }
        };

        public SystemStream(String method) {
            this.method = method;
        }

        @Override
        public void write(byte b[], int off, int len) throws IOException {
            String line = new String(b, off, len, "UTF-8");
            pending.append(line);
            int lastLine = pending.lastIndexOf("\n");
            if (lastLine >= 0) {
                doWrite(pending.substring(0, lastLine));
                pending.delete(0, lastLine + 1);
            }
        }

        @Override
        public void close() throws IOException {
            doWrite(pending.toString());
            pending.setLength(0);
        }

        void doWrite(String line) {
            write(method, line);
            writeRemote(method, line, sendOK);
        }

        @JavaScriptBody(args = { "method", "b" }, body = ""
          + "if (typeof console !== 'undefined') console[method](b.toString());"
        )
        private static native void write(String method, String b);

        @JavaScriptBody(args = { "method", "msg", "onFail" }, body = ""
            + "if (!onFail) {\n"
            + "   return;\n"
            + "}\n"
            + "if (typeof XMLHttpRequest === 'undefined') {\n"
            + "  onFail.run__V();\n"
            + "  return;\n"
            + "}\n"
            + "var xhttp = new XMLHttpRequest();\n"
            + "var safeMsg = encodeURIComponent(msg);\n"
            + "xhttp.open('GET', '/console/' + method + '/?msg=' + safeMsg, true);\n"
            + "xhttp.onreadystatechange = function () {\n"
            + "  if (xhttp.status === 0 || xhttp.status > 400) {\n"
            + "    onFail.run__V();\n"
            + "  }\n"
            + "};\n"
            + "xhttp.send();\n"
        )
        private static native void writeRemote(String method, String msg, Runnable onFail);

        @Override
        public void write(int b) throws IOException {
            write(new byte[] { (byte)b });
        }
    } // end of SystemStream
}
