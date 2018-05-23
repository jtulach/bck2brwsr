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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

public class AheadOfTimeGradleTest {

    @Test
    public void verifyMainJS() throws Exception {
        URL u = AheadOfTimeGradleTest.class.getResource("gradle1/build/web/main.js");
        assertNotNull(u, "main.js has been generated");
        String text = readStream(u);
        assertClasspath(text, "lib/net.java.html.boot-[0-9\\.]*.js", u);
        assertClasspath(text, "lib/emul.mini-[0-9\\.\\-SNAPSHOT]*.js", u);
    }

    private static String readStream(URL u) throws IOException {
        InputStream is = u.openStream();
        int len = is.available();
        byte[] arr = new byte[len];
        int read = is.read(arr);
        assertEquals(read, len, "Whole stream read");
        String text = new String(arr);
        return text;
    }

    @Test
    public void verifyPagesCopied() throws Exception {
        URL u = AheadOfTimeGradleTest.class.getResource("gradle2/build/web/test.html");
        assertNotNull(u, "test.html has been generated");
        String html = readStream(u);
        assertNotEquals(html.indexOf("<h1>Testing file</h1>"), -1, "Content found:\n" + html);
    }

    private void assertClasspath(String text, String importRegexp, URL u) {
        int cp = text.indexOf("classpath");
        assertTrue(cp > 0, "classpath found in " + u + "\n" + text);
        int begin = text.indexOf("[", cp);
        int end = text.indexOf("]", cp);

        assertTrue(end > begin, "end is after begin: " + end + " > " + begin + "\n" + text);

        String section = text.substring(begin, end + 1);

        Pattern p = Pattern.compile(importRegexp);
        Matcher m = p.matcher(section);
        assertTrue(m.find(), "found " + importRegexp + " in " + u + "\n" + section);
    }

}
