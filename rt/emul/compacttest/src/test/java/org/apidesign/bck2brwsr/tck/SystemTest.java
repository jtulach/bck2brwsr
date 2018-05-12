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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.apidesign.bck2brwsr.core.ExtraJavaScript;
import org.apidesign.bck2brwsr.core.JavaScriptBody;
import org.apidesign.bck2brwsr.vmtest.Compare;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@ExtraJavaScript(resource = "/org/apidesign/bck2brwsr/tck/console.js")
public class SystemTest {
    @Compare public boolean nonNullOSName() {
        return System.getProperty("os.name") != null;
    }

    @Compare public String captureStdOut() throws Exception {
        Object capture = initCapture();
        System.out.println("Ahoj");
        return textCapture(capture);
    }
    
    @JavaScriptBody(args = {}, body = ""
        + "var lines = [];"
        + "console.log = function(l) { lines.push(l); };"
        + "return lines;")
    Object initCapture() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        
        System.setOut(ps);
        return os;
    }
    
    @JavaScriptBody(args = { "o" }, body = "return o.join('');")
    String textCapture(Object o) throws java.io.IOException {
        ByteArrayOutputStream b = (ByteArrayOutputStream) o;
        String raw = new String(b.toByteArray(), "UTF-8");
        raw = raw.replace('\n', ' ').replace('\r', ' ').trim();
        return raw;
    }
    
    @Factory public static Object[] create() {
        return VMTest.create(SystemTest.class);
    }
}
