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
package org.apidesign.bck2brwsr.vmtest.impl;

import java.io.IOException;
import java.io.InputStream;
import org.apidesign.bck2brwsr.emul.zip.FastJar;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@GenerateZip(name = "five.zip", contents = {
    "1.txt", "one",
    "2.txt", "duo",
    "3.txt", "three",
    "4.txt", "four",
    "5.txt", "five"
})
public class ZipEntryTest {
    @Test
    public void readEntriesEffectively() throws IOException {
        InputStream is = ZipEntryTest.class.getResourceAsStream("five.zip");
        byte[] arr = new byte[is.available()];
        int len = is.read(arr);
        assertEquals(len, arr.length, "Read fully");
        
        FastJar fj = new FastJar(arr);
        FastJar.Entry[] entrs = fj.list();
        
        assertEquals(5, entrs.length, "Five entries");
        
        for (int i = 1; i <= 5; i++) {
            FastJar.Entry en = entrs[i - 1];
            assertEquals(en.name, i + ".txt");
//            assertEquals(cis.cnt, 0, "Content of the file should be skipped, not read");
        }
        
        assertContent("three", fj.getInputStream(entrs[3 - 1]), "read OK");
        assertContent("five", fj.getInputStream(entrs[5 - 1]), "read OK");
    }

    private static void assertContent(String exp, InputStream is, String msg) throws IOException {
        byte[] arr = new byte[512];
        int len = is.read(arr);
        String s = new String(arr, 0, len);
        assertEquals(exp, s, msg);
    }
}
