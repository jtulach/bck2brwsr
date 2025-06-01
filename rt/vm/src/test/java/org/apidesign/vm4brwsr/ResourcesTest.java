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

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** Tests related to loading resources from the VM.
 *
 * @author Jaroslav Tulach
 */
public class ResourcesTest {
    @Test public void loadPrecompiledResource() throws Exception {
        String exp = Resources.loadKO();
        
        assertExec("Loading a precompiled resource:",
            Resources.class, "loadKO__Ljava_lang_String_2", 
            exp
        );
    }

    @Test public void loadBinaryPrecompiledResource() throws Exception {
        String exp = Resources.loadClazz();
        
        assertExec("Loading a precompiled resource:",
            Resources.class, "loadClazz__Ljava_lang_String_2", 
            exp
        );
    }
    
    private static TestVM code;
    
    @BeforeClass 
    public static void compileTheCode() throws Exception {
        StringBuilder sb = new StringBuilder();
        code = TestVM.compileClassAndResources(sb, null, 
            "org/apidesign/vm4brwsr/Resources", 
            "org/apidesign/vm4brwsr/ko.js",
            "org/apidesign/vm4brwsr/Bck2BrwsrToolkit.class"
        );
    }
    @AfterClass
    public static void releaseTheCode() {
        code = null;
    }

    private void assertExec(
        String msg, Class<?> clazz, String method, 
        Object ret, Object... args
    ) throws Exception {
        code.assertExec(msg, clazz, method, ret, args);
    }
}
