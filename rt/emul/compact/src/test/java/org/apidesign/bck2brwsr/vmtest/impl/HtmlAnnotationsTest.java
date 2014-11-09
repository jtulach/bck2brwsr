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
package org.apidesign.bck2brwsr.vmtest.impl;

import org.apidesign.bck2brwsr.core.JavaScriptBody;
import org.apidesign.bck2brwsr.vmtest.BrwsrTest;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.testng.annotations.Factory;

/** Verify cooperation with net.java.html.js annotations.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class HtmlAnnotationsTest {
    static int firstCheck;
    
    private void assertMulNotDefinedForTheFirstTime() {
        if (firstCheck++ == 0) {
            Object mul = windowMul();
            assert mul == null : "htmlannotations.js should not be processed before first call to HtmlAnnotations class";
        }
    }
    
    @BrwsrTest public void fourtyTwo() throws Exception {
        assertMulNotDefinedForTheFirstTime();
        assertEquals(HtmlAnnotations.fourtyTwo(), 42);
    }
    
    @BrwsrTest public void externalMul() throws Exception {
        assertMulNotDefinedForTheFirstTime();
        assertEquals(HtmlAnnotations.useExternalMul(7, 6), 42);
    }

    @BrwsrTest public void callRunnableFromJS() throws Exception {
        assertMulNotDefinedForTheFirstTime();
        assertEquals(HtmlAnnotations.callback(), 1);
    }

    @BrwsrTest public void callStaticMethodFromJS() throws Exception {
        assertMulNotDefinedForTheFirstTime();
        assertEquals(HtmlAnnotations.staticCallback(), 1);
    }

    @BrwsrTest public void callbackWithFourParamsAndReturnType() throws Exception {
        assertMulNotDefinedForTheFirstTime();
        Object instance = HtmlAnnotations.create();
        assertNotNull(instance, "Instance created");
        assertEquals(HtmlAnnotations.first(instance, 42, 31), 42);
    }

    @BrwsrTest public void callbackWithObjectParamsAndReturnType() throws Exception {
        assertMulNotDefinedForTheFirstTime();
        Object instance = HtmlAnnotations.create();
        assertNotNull(instance, "Instance created");
        assertEquals(HtmlAnnotations.onError(instance, 42.0), 42.0);
    }
    
    private static void assertEquals(double real, double exp) {
        if (real - exp < 0.01) {
            return;
        }
        assert false : "Expecting " + exp + " but was " + real;
    }

    private static void assertNotNull(Object obj, String msg) {
        assert obj != null : msg;
    }
    
    @JavaScriptBody(args = {}, body = "return window.mul ? window.mul : null;")
    private static native Object windowMul();
    
    @Factory public static Object[] create() {
        return VMTest.create(HtmlAnnotationsTest.class);
    }
}
