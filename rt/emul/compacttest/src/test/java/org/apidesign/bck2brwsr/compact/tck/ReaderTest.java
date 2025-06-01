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
package org.apidesign.bck2brwsr.compact.tck;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import org.apidesign.bck2brwsr.vmtest.Compare;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach
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
        InputStreamReader r = new InputStreamReader(is, "UTF-8");
        return readReader(r);        
    }

    private String readReader(InputStreamReader r) throws IOException {
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
    @Compare public String stringToBytes() throws UnsupportedEncodingException {
        return Arrays.toString(YellowHorse.getBytes("UTF-8"));
    }
    private final String YellowHorse = "\u017dlu\u0165ou\u010dk\u00fd k\u016f\u0148";
    
    @Compare public String readAndWrite() throws Exception {
        ByteArrayOutputStream arr = new ByteArrayOutputStream();
        OutputStreamWriter w = new OutputStreamWriter(arr);
        w.write(YellowHorse);
        w.close();
        
        ByteArrayInputStream is = new ByteArrayInputStream(arr.toByteArray());
        InputStreamReader r = new InputStreamReader(is, "UTF-8");
        return readReader(r);
    }
    
    @Factory public static Object[] create() {
        return VMTest.create(ReaderTest.class);
    }
}
