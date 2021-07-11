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
package org.apidesign.bck2brwsr.gradletest;

import java.io.InputStream;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.testng.Assert.*;
import org.testng.SkipException;
import org.testng.annotations.Test;

public class Gradle1BuildTest {

    @Test
    public void verifyMainJS() throws Exception {
        URL u = Gradle1BuildTest.class.getResource("gradle1/build/web/main.js");
        assertNotNull(u, "main.js has been generated");
        InputStream is = u.openStream();
        assertOrAssumeNotNull(is, "main.js has been generated");
        int len = is.available();
        byte[] arr = new byte[len];
        int read = is.read(arr);
        assertEquals(read, len, "Whole stream read");
        String text = new String(arr);
        assertEquals(text.indexOf("Failed to obfuscate"), -1, "The code should be obfuscated " + u);
        assertClasspath(text, "lib/net.java.html.boot-[0-9\\.\\-SNAPSHOT]*.js", 3);
        assertClasspath(text, "lib/emul-[0-9\\.\\-SNAPSHOT]*-rt.js", 3);
    }

    private void assertClasspath(String text, String imprt, int expElements) {
        int cp = text.indexOf("classpath");
        assertTrue(cp > 0, "classpath found in\n" + text);
        int begin = text.indexOf("[", cp);
        int end = text.indexOf("]", cp);

        assertTrue(end > begin, "end is after begin: " + end + " > " + begin + "\n" + text);

        String section = text.substring(begin + 1, end);

        String[] elements = section.split(",");
        assertEquals(elements.length, expElements, "Expecting " + expElements + " classpath elements in\n" + section);

        for (String e : elements) {
            e = e.replace('"', ' ').trim();
            Pattern p = Pattern.compile(imprt);
            Matcher m = p.matcher(e);
            if (m.matches()) {
                return;
            }
        }
        fail("Not found " + imprt + " in\n" + section);
    }

    private void assertOrAssumeNotNull(InputStream is, String msg) {
        try {
            Class.forName("java.lang.Module");
            // skip the test on JDK9 and newer
            if (is == null) {
                throw new SkipException("Ignoring on JDK9: " + msg);
            }
        } catch (ClassNotFoundException ex) {
            // assert on JDK8
            assertNotNull(is, msg);
        }
    }

}
