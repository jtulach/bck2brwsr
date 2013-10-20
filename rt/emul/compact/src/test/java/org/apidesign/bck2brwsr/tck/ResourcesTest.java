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
package org.apidesign.bck2brwsr.tck;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import org.apidesign.bck2brwsr.vmtest.Compare;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ResourcesTest {
    @Compare public String allManifests() throws Exception {
        Enumeration<URL> en = ClassLoader.getSystemResources("META-INF/MANIFEST.MF");
        assert en.hasMoreElements() : "Should have at least one manifest";
        String first = readString(en.nextElement().openStream());
        boolean different = false;
        int cnt = 1;
        while (en.hasMoreElements()) {
            URL url = en.nextElement();
            String now = readString(url.openStream());
            if (!first.equals(now)) {
                different = true;
            }
            cnt++;
            if (cnt > 500) {
                throw new IllegalStateException(
                    "Giving up. First manifest:\n" + first + 
                    "\nLast manifest:\n" + now
                );
            }
        }
        assert different : "Not all manifests should look like first one:\n" + first;
        return "" + cnt;
    }
    
    @Compare public String readResourceAsStream() throws Exception {
        InputStream is = getClass().getResourceAsStream("Resources.txt");
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
        InputStream is = getClass().getClassLoader().getResourceAsStream("org/apidesign/bck2brwsr/tck/Resources.txt");
        return readString(is);
    }
    
    @Compare public String toURIFromURL() throws Exception {
        URL u = new URL("http://apidesign.org");
        return u.toURI().toString();
    }
    
    @Factory public static Object[] create() {
        return VMTest.create(ResourcesTest.class);
    }
}
