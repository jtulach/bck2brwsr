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
package org.apidesign.bck2brwsr.brwsrtest;

import java.io.PrintWriter;
import java.io.StringWriter;
import net.java.html.js.JavaScriptBody;
import org.apidesign.bck2brwsr.vmtest.Compare;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ExceptionStackTest {
    @Compare
    public String verifyFormatOfStackTrace() {
        IllegalStateException ex;
        try {
            throw new IllegalStateException();
        } catch (IllegalStateException caught) {
            ex = caught;
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        pw.close();

        String trace = sw.toString();
        if (userAgent() != null && userAgent().contains("Safari")) {
            return "Skipping test on safari";
        }
        if (trace.contains("ExceptionStackTest") && trace.contains("verifyFormatOfStackTrace")) {
            return "Stack trace includes ExceptionStackTest and verifyFormatOfStackTrace";
        }
        return trace;
    }
    
    @JavaScriptBody(args = {}, body = "return navigator.userAgent;")
    private static String userAgent() {
        String os = System.getProperty("os.name");
        return os == null || os.contains("Mac") ? "Safari" : "any";
    }

    @Factory public static Object[] create() {
        return VMTest.create(ExceptionStackTest.class);
    }
}
