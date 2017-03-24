/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012-2017 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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
package org.apidesign.bck2brwsr.ko2brwsr;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.testng.annotations.Test;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeMethod;
import org.testng.reporters.Files;

public class MinifiedIT {
    private File file;
    
    @BeforeMethod public void findPrecompiledLibraries() throws Exception {
        File dir = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
        for (File f : dir.listFiles()) {
            if (f.getName().endsWith("-bck2brwsr.jar")) {
                file = f;
                return;
            }
        }
        fail("Cannot find precompiled libraries in " + dir);
    }
    
    @Test public void minifiedVersionDoesNotContainFQN() throws Exception {
        JarFile jf = new JarFile(file);
        Enumeration<JarEntry> en = jf.entries();
        while (en.hasMoreElements()) {
            JarEntry e = en.nextElement();
            String content;
            try (InputStream is = jf.getInputStream(e)) {
                content = Files.readFile(is);
            }
            if (content.contains("registerResource']('org/netbeans/html/ko4j/knockout")) {
                fail("@JavaScriptResource resource should be missing: "+ e.getName() + " in " + file);
            }
        }
    }
    
}
