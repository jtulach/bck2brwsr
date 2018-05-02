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
package org.apidesign.vm4brwsr;

import static org.testng.Assert.*;
import org.testng.annotations.Test;

public class Bck2BrwsrTest {
    @Test
    public void keepClassPathUnchanged() throws Exception {
        final Bck2Brwsr e = Bck2Brwsr.newCompiler();
        assertFalse(e.isExtension(), "it is not a library by default");
        assertNull(e.classpath(), "no classpath by default");
        Bck2Brwsr c = e.library("a", "b");
        assertEquals(c.classpath().toArray(), new String[] { "a", "b" });
        assertTrue(c.isExtension(), "it is a library");
        Bck2Brwsr d = c.library((String[]) null);
        assertEquals(d.classpath().toArray(), new String[] { "a", "b" }, "Remains unchanged");
        assertTrue(d.isExtension(), "it is a library");

        Bck2Brwsr nopath = e.library((String[])null);
        assertNull(nopath.classpath(), "No path associated");
        assertTrue(nopath.isExtension(), "it is a library");
    }
}
