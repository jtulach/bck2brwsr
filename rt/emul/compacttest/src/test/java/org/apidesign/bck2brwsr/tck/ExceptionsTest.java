/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012-2015 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import org.apidesign.bck2brwsr.vmtest.Compare;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ExceptionsTest {
    @Compare public String firstLineIsTheSame() throws UnsupportedEncodingException {
        MyException ex = new MyException("Hello");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(out);
        ex.printStackTrace(ps);
        ps.flush();
        
        String s = new String(out.toByteArray(), "UTF-8");
        int newLine = s.indexOf('\n');
        return s.substring(0, newLine);
    }

    @Compare public String firstLineIsTheSameWithWriter() throws UnsupportedEncodingException {
        MyException ex = new MyException("Hello");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        pw.flush();
        
        String s = sw.toString();
        int newLine = s.indexOf('\n');
        return s.substring(0, newLine);
    }
    
    static class MyException extends Exception {
        public MyException(String message) {
            super(message);
        }
    }
    
    
    @Factory public static Object[] create() {
        return VMTest.create(ExceptionsTest.class);
    }
}
