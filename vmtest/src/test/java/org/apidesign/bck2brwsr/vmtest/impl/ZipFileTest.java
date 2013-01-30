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
package org.apidesign.bck2brwsr.vmtest.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apidesign.bck2brwsr.vmtest.Compare;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ZipFileTest {
    
    @GenerateZip(name = "readAnEntry.zip", contents = { "my/main/file.txt", "Hello World!" })
    @Compare public String readAnEntry() throws IOException {
        InputStream is = ZipFileTest.class.getResourceAsStream("readAnEntry.zip");
        ZipInputStream zip = new ZipInputStream(is);
        ZipEntry entry = zip.getNextEntry();
        assertEquals(entry.getName(), "my/main/file.txt", "Correct entry");
        
        byte[] arr = new byte[4096];
        int len = zip.read(arr);
        
        return new String(arr, 0, len, "UTF-8");
    }

    private static void assertEquals(String real, String exp, String msg) {
        assert exp.equals(real) : msg + " exp: " + exp + " real: " + real;
    }
    
    @Factory public static Object[] create() {
        return VMTest.create(ZipFileTest.class);
    }
}
