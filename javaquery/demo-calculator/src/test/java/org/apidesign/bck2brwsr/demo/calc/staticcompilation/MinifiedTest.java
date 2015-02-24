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
package org.apidesign.bck2brwsr.demo.calc.staticcompilation;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class MinifiedTest {
    @Test public void minifiedVersionDoesNotContainFQN() throws Exception {
        final ClassLoader l = MinifiedTest.class.getClassLoader();
        Enumeration<URL> en = l.getResources("META-INF/MANIFEST.MF");
        while (en.hasMoreElements()) {
            URL u = en.nextElement();
            Manifest mf = new Manifest(u.openStream());
            for (Map.Entry<String, Attributes> entrySet : mf.getEntries().entrySet()) {
                String key = entrySet.getKey();
                if (!key.contains("-min.js")) {
                    continue;
                }
                if (!key.contains("javaquery.api")) {
                    continue;
                }
                try (BufferedReader r = new BufferedReader(new InputStreamReader(l.getResourceAsStream(key)))) {
                    for (;;) {
                        String line = r.readLine();
                        if (line == null) {
                            break;
                        }
                        if (line.contains(" org_apidesign_bck2brwsr_htmlpage_Knockout")) {
                            fail("Found FQN of non-public class. Is it minified version?: " + line + " in " + key);
                        } 
                    }
                }
                // success
                return;
            }
        }
        fail("No minified javaquery found: " + System.getProperty("java.class.path"));
    }

    @Test public void debugVersionContainsFQN() throws Exception {
        final ClassLoader l = MinifiedTest.class.getClassLoader();
        Enumeration<URL> en = l.getResources("META-INF/MANIFEST.MF");
        while (en.hasMoreElements()) {
            URL u = en.nextElement();
            Manifest mf = new Manifest(u.openStream());
            for (Map.Entry<String, Attributes> entrySet : mf.getEntries().entrySet()) {
                String key = entrySet.getKey();
                if (!key.contains("-debug.js")) {
                    continue;
                }
                if (!key.contains("javaquery.api")) {
                    continue;
                }
                try (BufferedReader r = new BufferedReader(new InputStreamReader(l.getResourceAsStream(key)))) {
                    for (;;) {
                        String line = r.readLine();
                        if (line == null) {
                            break;
                        }
                        if (line.contains(" org_apidesign_bck2brwsr_htmlpage_Knockout")) {
                            // ok, it should be there
                            return;
                        } 
                    }
                }
                fail("Found no FQN of non-public Knockout class. Is it debug version?:" + key);
            }
        }
        fail("No debug javaquery found: " + System.getProperty("java.class.path"));
    }
    
}
