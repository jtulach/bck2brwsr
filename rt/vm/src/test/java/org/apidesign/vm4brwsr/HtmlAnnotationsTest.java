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
package org.apidesign.vm4brwsr;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** Verify cooperation with net.java.html.js annotations.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class HtmlAnnotationsTest {
    @Test public void fourtyTwo() throws Exception {
        assertExec("Annotation used", HtmlAnnotations.class, 
            "fourtyTwo__I",
            Double.valueOf(42)
        );
    }
    
    @Test public void externalMul() throws Exception {
        assertExec("mul function is loaded", HtmlAnnotations.class, 
            "useExternalMul__III",
            Double.valueOf(42),
            7, 6
        );
    }

    @Test public void callRunnableFromJS() throws Exception {
        assertExec("runnable called", HtmlAnnotations.class, 
            "callback__I",
            Double.valueOf(1)
        );
    }

    @Test public void callStaticMethodFromJS() throws Exception {
        assertExec("runnable called", HtmlAnnotations.class, 
            "staticCallback__I",
            Double.valueOf(1)
        );
    }
    
    private static TestVM code;
    
    @BeforeClass 
    public void compileTheCode() throws Exception {
        code = TestVM.compileClass("org/apidesign/vm4brwsr/HtmlAnnotations");
    }
    @AfterClass
    public static void releaseTheCode() {
        code = null;
    }
    private static void assertExec(String msg, Class clazz, String method, Object expRes, Object... args) throws Exception {
        code.assertExec(msg, clazz, method, expRes, args);
    }
    
}
