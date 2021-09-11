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

import org.apidesign.bck2brwsr.aot.UtilAsm;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Set;
import org.apidesign.bck2brwsr.vm11.NestMates;
import org.apidesign.vm4brwsr.Bck2Brwsr;
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
        URL vm4j = Bck2Brwsr.class.getProtectionDomain().getCodeSource().getLocation();
        File vm4jar = new File(vm4j.toURI());
        assertTrue(vm4jar.exists(), "Jar exists: " + vm4jar);
        Set<String> res = UtilAsm.findMainClass(vm4jar);
        assertEquals(res.size(), 1, "One main class found: " + res);
        assertTrue(res.contains("org.apidesign.vm4brwsr.Main"), "vm4brwsr Main is there: " + res);
    }
}
