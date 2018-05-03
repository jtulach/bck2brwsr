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
package org.apidesign.bck2brwsr.mojo;

import java.io.InputStream;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

public class AheadOfTimeGradleTest {

    @Test
    public void verifyMainJS() throws Exception {
        InputStream is = AheadOfTimeGradleTest.class.getResourceAsStream("gradle1/build/web/main.js");
        assertNotNull(is, "main.js has been generated");
        int len = is.available();
        byte[] arr = new byte[len];
        int read = is.read(arr);
        assertEquals(read, len, "Whole stream read");
        String text = new String(arr);
        assertClasspath(text, "lib/net.java.html.boot-1.5.1.js");
        assertClasspath(text, "lib/emul-1.0-SNAPSHOT-rt.js");
    }

    private void assertClasspath(String text, String imprt) {
        int cp = text.indexOf("classpath");
        assertTrue(cp > 0, "classpath found in\n" + text);
        int begin = text.indexOf("[", cp);
        int end = text.indexOf("]", cp);

        assertTrue(end > begin, "end is after begin: " + end + " > " + begin + "\n" + text);

        String section = text.substring(begin, end + 1);

        assertNotEquals(section.indexOf(imprt), -1, "found " + imprt + " in\n" + section);
    }

}
