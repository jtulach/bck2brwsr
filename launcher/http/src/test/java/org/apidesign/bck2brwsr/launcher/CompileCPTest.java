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
package org.apidesign.bck2brwsr.launcher;

import java.io.File;
import java.net.URL;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach
 */
public class CompileCPTest {
    public CompileCPTest() {
    }

    @Test public void compileClassPath() throws Exception {
        URL u = CompileCPTest.class.getResource("/" + CompileCPTest.class.getName().replace('.', '/') + ".class");
        assertNotNull(u, "URL found");
        assertEquals(u.getProtocol(), "file", "It comes from a disk");
        
        String resources = CompileCP.compileFromClassPath(u, null);
        assertNotNull(resources, "something compiled");
    }
}
