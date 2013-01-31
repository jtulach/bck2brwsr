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
package org.apidesign.bck2brwsr.tck;

import java.net.URL;
import org.apidesign.bck2brwsr.core.JavaScriptBody;
import org.apidesign.bck2brwsr.vmtest.BrwsrTest;
import org.apidesign.bck2brwsr.vmtest.HttpResource;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class HttpResourceTest {
    
    @HttpResource(path = "/xhr", content = "Hello Brwsr!", mimeType = "text/plain")
    @BrwsrTest
    public String testReadContentViaXHR() throws Exception {
        String msg = read("/xhr");
        assert "Hello Brwsr!".equals(msg) : "The message was " + msg;
        return msg;
    }

    @HttpResource(path = "/url", content = "Hello via URL!", mimeType = "text/plain")
    @BrwsrTest
    public String testReadContentViaURL() throws Exception {
        URL url = new URL("http:/url");
        String msg = (String) url.getContent();
        assert "Hello via URL!".equals(msg) : "The message was " + msg;
        return msg;
    }
    @HttpResource(path = "/url", content = "Hello via URL!", mimeType = "text/plain")
    @BrwsrTest
    public String testReadContentViaURLWithStringParam() throws Exception {
        URL url = new URL("http:/url");
        String msg = (String) url.getContent(new Class[] { String.class });
        assert "Hello via URL!".equals(msg) : "The message was " + msg;
        return msg;
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
