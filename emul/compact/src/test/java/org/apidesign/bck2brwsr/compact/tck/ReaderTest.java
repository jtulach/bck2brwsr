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
package org.apidesign.bck2brwsr.compact.tck;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import org.apidesign.bck2brwsr.vmtest.Compare;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ReaderTest {
    @Compare public String readUTFString() throws IOException {
        byte[] arr = { 
            (byte)-59, (byte)-67, (byte)108, (byte)117, (byte)-59, (byte)-91, 
            (byte)111, (byte)117, (byte)-60, (byte)-115, (byte)107, (byte)-61, 
            (byte)-67, (byte)32, (byte)107, (byte)-59, (byte)-81, (byte)-59, 
            (byte)-120 
        };
        ByteArrayInputStream is = new ByteArrayInputStream(arr);
        InputStreamReader r = new InputStreamReader(is);
        
        StringBuilder sb = new StringBuilder();
        for (;;) {
            int ch = r.read();
            if (ch == -1) {
                break;
            }
            sb.append((char)ch);
        }
        return sb.toString().toString();
    }
    @Compare public String stringToBytes() {
        return Arrays.toString("\u017dlu\u0165ou\u010dk\u00fd k\u016f\u0148".getBytes());
    }
    
    @Factory public static Object[] create() {
        return VMTest.create(ReaderTest.class);
    }
}
