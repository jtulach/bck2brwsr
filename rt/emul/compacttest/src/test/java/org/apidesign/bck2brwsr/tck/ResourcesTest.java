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
package org.apidesign.bck2brwsr.tck;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import org.apidesign.bck2brwsr.vmtest.BrwsrTest;
import org.apidesign.bck2brwsr.vmtest.Compare;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ResourcesTest {
    @Compare public String allManifests() throws Exception {
        Enumeration<URL> en = ClassLoader.getSystemResources("META-INF/MANIFEST.MF");
        assert en.hasMoreElements() : "Should have at least one manifest";
        String first = readString(en.nextElement().openStream());
        boolean different = false;
        int cnt = 1;
        while (en.hasMoreElements()) {
            URL url = en.nextElement();
            String now = readString(url.openStream());
            if (!first.equals(now)) {
                different = true;
            }
            cnt++;
            if (cnt > 500) {
                throw new IllegalStateException(
                    "Giving up. First manifest:\n" + first + 
                    "\nLast manifest:\n" + now
                );
            }
        }
        assert different : "Not all manifests should look like first one:\n" + first;
        if (cnt > 30 && cnt < 50) {
            return "OK";
        } else {
            return "" + cnt;
        }
    }
    
    @Compare public String readResourceAsStream() throws Exception {
        InputStream is = getClass().getResourceAsStream("Resources.txt");
        return readString(is);
    }
    
    @Compare public String readResourceViaXMLHttpRequest() throws Exception {
        return readResourceViaXHR("Resources.txt", null);
    }
    
    @BrwsrTest public void xhrTestedInBrowser() throws Exception {
        boolean[] run = { false };
        readResourceViaXHR("Resources.txt", run);
        assert run[0] : "XHR really used in browser";
    }

    @Compare public String readBinaryResourceViaXMLHttpRequest() throws Exception {
        return readResourceViaXHR("0xfe", null);
    }

    private String readResourceViaXHR(final String res, boolean[] exec) throws IOException {
        URL url = getClass().getResource(res);
        URLConnection conn = url.openConnection();
        String java = readBytes(url.openStream());
        String java2 = readBytes(conn.getInputStream());
        assert java.equals(java2) : "Java:\n" + java + "\nConn:\n" + java2;
        
        URL url2 = conn.getURL();
        String java3 = readBytes(url.openStream());
        assert java.equals(java3) : "Java:\n" + java + "\nConnURL:\n" + java3;
        
        
        byte[] xhr = readXHR(url2.toExternalForm());
        if (xhr != null) {
            if (exec != null) {
                exec[0] = true;
            }
            String s = readBytes(new ByteArrayInputStream(xhr));
            assert java.equals(s) : "Java:\n" + java + "\nXHR:\n" + s;
            
            assert conn instanceof Closeable : "Can be closed";
            
            Closeable c = (Closeable) conn;
            c.close();
            
            byte[] xhr2 = null;
            try {
                xhr2 = readXHR(url2.toExternalForm());
            } catch (Throwable t) {
                // OK, expecting error
            }
            assert xhr2 == null : "Cannot read the URL anymore";
        }
        return java;
    }

    @org.apidesign.bck2brwsr.core.JavaScriptBody(args = { "url" }, body =
        "if (typeof XMLHttpRequest === 'undefined') return null;\n" +
        "var xhr = new XMLHttpRequest();\n" +
        "xhr.overrideMimeType('text\\/plain; charset=x-user-defined');\n" +
        "xhr.open('GET', url, false);\n" +
        "xhr.send();\n" +
        "if (xhr.status >= 300) throw 'Status: ' + xhr.status + ' ' + xhr.statusText;\n" +
        "var ret = []\n" +
        "for (var i = 0; i < xhr.responseText.length; i++) {\n" +
        "  var b = xhr.responseText.charCodeAt(i) & 0xff;\n" +
        "  if (b > 127) b -= 256;\n" +
        "  ret.push(b);\n" +
        "}\n" +
        "return ret;\n"
    )
    private static byte[] readXHR(String url) {
        return null;
    }
    
    @Compare public String readResourceViaConnection() throws Exception {
        final URL url = getClass().getResource("Resources.txt");
        String str = url.toExternalForm();
        int idx = str.indexOf("org/apidesign/bck2brwsr/tck");
        assert idx >= 0 : "Package name found in the URL name: " + str;
        InputStream is = url.openConnection().getInputStream();
        return readString(is);
    }

    private String readString(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        byte[] b = new byte[512];
        for (;;) { 
            int len = is.read(b);
            if (len == -1) {
                return sb.toString();
            }
            for (int i = 0; i < len; i++) {
                sb.append((char)b[i]);
            }
        }
    }

    private String readBytes(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        byte[] b = new byte[512];
        for (;;) { 
            int len = is.read(b);
            if (len == -1) {
                return sb.toString();
            }
            for (int i = 0; i < len; i++) {
                sb.append((int)b[i]).append(" ");
            }
        }
    }

    @Compare public String readResourceAsStreamFromClassLoader() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("org/apidesign/bck2brwsr/tck/Resources.txt");
        return readString(is);
    }
    
    @Compare public String toURIFromURL() throws Exception {
        URL u = new URL("http://apidesign.org");
        return u.toURI().toString();
    }
    
    @Factory public static Object[] create() {
        return VMTest.create(ResourcesTest.class);
    }
}
