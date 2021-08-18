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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Set;
import org.apidesign.bck2brwsr.vm11.NestMates;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

public class UtilAsmJdk11Test {
    @Test
    public void tryToFindMainOnJdk11() throws URISyntaxException, IOException {
        URL utilAsm = NestMates.class.getProtectionDomain().getCodeSource().getLocation();
        File testJar = new File(utilAsm.toURI());
        assertTrue(testJar.exists(), "Jar exists: " + testJar);
        Set<String> res = UtilAsm.findMainClass(testJar);
        assertEquals(res.size(), 1, "One main class found: " + res);
    }

    @Test
    public void tryToFindMainInMojo() throws URISyntaxException, IOException {
        URL utilAsm = UtilAsm.class.getProtectionDomain().getCodeSource().getLocation();
        File utilAsmJar = new File(utilAsm.toURI());
        assertTrue(utilAsmJar.exists(), "Jar exists: " + utilAsmJar);
        Set<String> res = UtilAsm.findMainClass(utilAsmJar);
        assertTrue(res.size() > 10, "At least ten main classes found: " + res);
        assertTrue(res.contains("org.apidesign.bck2brwsr.mojo.ShowMain"), "ShowMain is there: " + res);
    }
}
