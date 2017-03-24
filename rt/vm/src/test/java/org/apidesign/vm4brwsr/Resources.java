/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012-2017 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import net.java.html.js.JavaScriptBody;
import net.java.html.js.JavaScriptResource;
import org.apidesign.bck2brwsr.core.ExtraJavaScript;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@ExtraJavaScript(resource = "org/apidesign/vm4brwsr/var.js", processByteCode = true)
@JavaScriptResource("obj.js")
public class Resources {
    @JavaScriptBody(args = {}, body = "return obj;")
    static Object retObj() {
        return null;
    }
    
    public static boolean isObj() {
        return retObj() != null;
    }
    public static boolean isResource(String name) {
        return Resources.class.getResource(name) != null;
    }
    
    public static String loadKO() throws IOException {
        InputStream is = Resources.class.getResourceAsStream("ko.js");
        return readIS(is, false);
    }
    
    static String loadClazz() throws IOException {
        Object o = new Resources();
        InputStream is = o.getClass().getResourceAsStream("Bck2BrwsrToolkit.class");
        return readIS(is, false);
    }

    private static String readIS(InputStream is, boolean asString) throws IOException {
        if (is == null) {
            return "No resource found!";
        }
        byte[] arr = new byte[4092];
        int len = is.read(arr);
        if (len < 5) {
            return "No data read! Len: " + len;
        }
        
        if (asString) {
            return new String(arr, 0, len, "UTF-8").toString().toString();
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < len; i++) {
            sb.append(arr[i]).append(", ");
        }
        
        return sb.toString().toString();
    }
    static long bytesToLong(byte b1, byte b2, int shift) {
        return (((long)b1 << 56) +
                ((long)b2 & 255) << 48) >> shift;
    }

    public static String loadHello() throws IOException {
        Enumeration<URL> en;
        try {
            en = Resources.class.getClassLoader().getResources("META-INF/ahoj");
        } catch (SecurityException ex) {
            return "SecurityException";
        }
        StringBuilder sb = new StringBuilder();
        while (en.hasMoreElements()) {
            URL url = en.nextElement();
            sb.append(readIS(url.openStream(), true));
        }
        String s = sb.toString();
        s = s + s.hashCode();
        return s.toString();
    }
    public static String loadJustHello() throws IOException {
        URL url = Resources.class.getResource("/META-INF/ahoj");
        StringBuilder sb = new StringBuilder();
        sb.append(readIS(url.openStream(), true));
        return sb.toString().toString();
    }
}
