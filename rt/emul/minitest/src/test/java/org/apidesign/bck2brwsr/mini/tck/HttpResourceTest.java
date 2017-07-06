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
package org.apidesign.bck2brwsr.mini.tck;

import java.io.InputStream;
import java.net.URL;
import org.apidesign.bck2brwsr.core.JavaScriptBody;
import org.apidesign.bck2brwsr.vmtest.BrwsrTest;
import org.apidesign.bck2brwsr.vmtest.Http;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class HttpResourceTest {
    
    @Http({
        @Http.Resource(path = "/xhr", content = "Hello Brwsr!", mimeType = "text/plain")
    })
    @BrwsrTest
    
    
    public String testReadContentViaXHR() throws Exception {
        String msg = read("/xhr");
        assert "Hello Brwsr!".equals(msg) : "The message was " + msg;
        return msg;
    }

    @Http({
        @Http.Resource(path = "/url", content = "Hello via URL!", mimeType = "text/plain")
    })
    @BrwsrTest
    public String testReadContentViaURL() throws Exception {
        URL url = new URL("http:/url");
        String msg = (String) url.getContent();
        assert "Hello via URL!".equals(msg) : "The message was " + msg;
        return msg;
    }
    @Http({
        @Http.Resource(path = "/url", content = "Hello via URL!", mimeType = "text/plain")
    })
    @BrwsrTest
    public String testReadContentViaURLWithStringParam() throws Exception {
        URL url = new URL("http:/url");
        String msg = (String) url.getContent(new Class[] { String.class });
        assert "Hello via URL!".equals(msg) : "The message was " + msg;
        return msg;
    }

    @Http({
        @Http.Resource(path = "/bytes", content = "", resource = "0xfe", mimeType = "x-application/binary")
    })
    @BrwsrTest
    public void testReadByte() throws Exception {
        URL url = new URL("http:/bytes");
        final Object res = url.getContent(new Class[] { byte[].class });
        assert res instanceof byte[] : "Expecting byte[]: " + res;
        byte[] arr = (byte[]) res;
        assert arr.length == 1 : "One byte " + arr.length;
        assert arr[0] == 0xfe : "It is 0xfe: " + Integer.toHexString(arr[0]);
    }

    @Http({
        @Http.Resource(path = "/bytes", content = "", resource = "0xfe", mimeType = "x-application/binary")
    })
    @BrwsrTest
    public void testReadByteViaInputStream() throws Exception {
        URL url = new URL("http:/bytes");
        InputStream is = url.openStream();
        byte[] arr = new byte[10];
        int len = is.read(arr);
        assert len == 1 : "One byte " + len;
        assert arr[0] == 0xfe : "It is 0xfe: " + Integer.toHexString(arr[0]);
    }
    
    @JavaScriptBody(args = { "url" }, body = 
          "var req = new XMLHttpRequest();\n"
        + "req.open('GET', url, false);\n"
        + "req.send();\n"
        + "return req.responseText;"
    )
    private static native String read(String url);
    
    
    @Factory
    public static Object[] create() {
        return VMTest.create(HttpResourceTest.class);
    }
}
