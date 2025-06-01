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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import org.apidesign.bck2brwsr.vmtest.Compare;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach
 */
public class ResourcesInBrwsrTest {
    
    @Compare public String readResourceAsStream() throws Exception {
        InputStream is = getClass().getResourceAsStream("Resources.txt");
        return readString(is);
    }
    
    @Compare public String readResourceViaConnection() throws Exception {
        InputStream is = getClass().getResource("Resources.txt").openConnection().getInputStream();
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

    @Compare public String readResourceAsStreamFromClassLoader() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("org/apidesign/bck2brwsr/brwsrtest/Resources.txt");
        return readString(is);
    }
    
    @Compare public String toURIFromURL() throws Exception {
        URL u = new URL("http://apidesign.org");
        return u.toURI().toString();
    }
    
    @Factory public static Object[] create() {
        return VMTest.create(ResourcesInBrwsrTest.class);
    }
}
